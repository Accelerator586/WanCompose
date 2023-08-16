package com.sundayting.wancompose.page.examplewidget.tantancard

import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sundayting.wancompose.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


/**
 * 探探用户对象实体类
 */
data class TanTanUserBean(
    val name: String,
    val picList: List<String>,
    val basicDetail: BasicDetail,
    val recentPost: RecentPost,
) {

    data class BasicDetail(
        val isMale: Boolean = false,
        val age: Int? = null,
        val tagList: List<Tag> = emptyList(),
        val location: String,
    ) {

        data class Tag(
            @DrawableRes
            val icon: Int? = null,
            val content: String,
        )

    }

    data class RecentPost(
        val picList: List<String> = emptyList(),
    )

}

/**
 * 探探Like滑动卡
 */
@Composable
fun TanTanSwipeCard(
    modifier: Modifier = Modifier,
    userList: List<TanTanUserBean>,
) {
    val scope = rememberCoroutineScope()
    val offsetAnimate = remember { Animatable(IntOffset.Zero, IntOffset.VectorConverter) }
    var dragTopHalf by remember { mutableStateOf(false) }
    val scrollThreshold = with(LocalDensity.current) { 200.dp.toPx() }
    val scrollPercentage by remember(scrollThreshold) {
        derivedStateOf {
            (offsetAnimate.value.x.toFloat() / scrollThreshold).coerceIn(-1f, 1f)
        }
    }
    val targetRotationZ by remember {
        derivedStateOf {
            lerp(
                0f,
                5f,
                scrollPercentage.absoluteValue
            ) * (if (scrollPercentage >= 0) 1f else -1f) * (if (dragTopHalf) 1f else -1f)
        }
    }
    Box(modifier) {
        userList.forEachIndexed { index, userBean ->
            TanTanSingleCard(
                Modifier
                    .matchParentSize()
                    .then(
                        if (index == userList.size - 1) {
                            Modifier
                                .offset { offsetAnimate.value }
                                .graphicsLayer {
                                    rotationZ = targetRotationZ
                                }
                                .pointerInput(Unit) {
                                    fun toInitLoc() {
                                        scope.launch {
                                            offsetAnimate.animateTo(IntOffset.Zero)
                                        }
                                    }
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            dragTopHalf = change.position.y < size.height / 2
                                            scope.launch {
                                                offsetAnimate.snapTo(
                                                    offsetAnimate.value + IntOffset(
                                                        dragAmount.x.roundToInt(),
                                                        dragAmount.y.roundToInt()
                                                    )
                                                )
                                            }
                                        },
                                        onDragCancel = { toInitLoc() },
                                        onDragEnd = { toInitLoc() }
                                    )

                                }
                        } else {
                            Modifier
                        }
                    ),
                userBean
            )
        }
    }
}

@Composable
private fun TanTanSingleCard(
    modifier: Modifier = Modifier,
    userBean: TanTanUserBean,
) {

    var yRotateTag by remember { mutableFloatStateOf(0f) }

    val yRotateAnimate = remember {
        Animatable(0f)
    }

    LaunchedEffect(Unit) {
        snapshotFlow { yRotateTag }.drop(1).collectLatest {
            yRotateAnimate.animateTo(
                0f, animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                ), initialVelocity = if (it > 0) 100f else -100f
            )
        }
    }

    ConstraintLayout(
        modifier
            .graphicsLayer {
                rotationY = yRotateAnimate.value
            }
            .clip(RoundedCornerShape(15.dp))
    ) {
        val (
            picContent,
            topIndicatorContent,
            bottomMaskContent,
            detailContent,
        ) = createRefs()

        var indicatorIndex by remember(userBean.picList.size) { mutableIntStateOf(0) }

        AsyncImage(
            modifier = Modifier
                .constrainAs(picContent) {
                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                    centerTo(parent)
                },
            model = ImageRequest
                .Builder(LocalContext.current)
                .data(userBean.picList.getOrNull(indicatorIndex))
                .crossfade(true)
                .apply {
                    if (LocalInspectionMode.current) {
                        placeholder(R.drawable.default_head_pic)
                    }
                }
                .error(R.drawable.default_head_pic)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        HalvedClickArea(Modifier.constrainAs(createRef()) {
            top.linkTo(picContent.top)
            bottom.linkTo(picContent.bottom)
            start.linkTo(picContent.start)
            end.linkTo(picContent.end)
        }, onClickStart = {
            if (indicatorIndex == 0) {
                yRotateTag = -(yRotateTag.absoluteValue + 0.1f)
            } else {
                indicatorIndex--
            }
        }, onClickEnd = {
            if (indicatorIndex == userBean.picList.size - 1) {
                yRotateTag = (yRotateTag.absoluteValue + 0.1f)
            } else {
                indicatorIndex++
            }
        })


        if (userBean.picList.isNotEmpty()) {
            PicIndicator(
                modifier = Modifier.constrainAs(topIndicatorContent) {
                    top.linkTo(parent.top, 15.dp)
                    start.linkTo(parent.start, 20.dp)
                    end.linkTo(parent.end, 20.dp)
                    width = Dimension.fillToConstraints
                }, totalNum = userBean.picList.size, curIndex = indicatorIndex
            )
        }

        BottomMask(Modifier.constrainAs(bottomMaskContent) {
            centerHorizontallyTo(parent)
            width = Dimension.fillToConstraints
            height = Dimension.wrapContent
            bottom.linkTo(parent.bottom)
        })

        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .constrainAs(detailContent) {
                    bottom.linkTo(parent.bottom, 100.dp)
                    centerHorizontallyTo(parent, 0f)
                }
        ) {
            Text(
                text = userBean.name,
                style = TextStyle(
                    fontSize = 30.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            )

            AnimatedContent(
                targetState = ((indicatorIndex > 0) to userBean),
                label = "",
                transitionSpec = {
                    //如果目标是第一张图片的情况，则左滑进场，瞬时消失离场
                    if (targetState.first.not()) {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith fadeOut(
                            animationSpec = snap()
                        )
                    }
                    //如果目标是第一张图片的情况，则右滑进场，瞬时消失离场
                    else {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith fadeOut(
                            animationSpec = snap()
                        )
                    }.using(
                        SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> snap() })
                    )
                }
            ) { paramsPair ->
                val user = paramsPair.second
                if (paramsPair.first.not()) {
                    Column {
                        Text(
                            text = userBean.basicDetail.location,
                            style = TextStyle(
                                fontSize = 15.sp,
                                color = Color.White,
                            )
                        )
                        Spacer(Modifier.height(10.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (user.basicDetail.isMale) {
                                TagContent(
                                    title = user.basicDetail.age?.toString().orEmpty(),
                                    iconRes = if (user.basicDetail.isMale) R.drawable.ic_male else R.drawable.ic_female,
                                    backgroundColor = if (user.basicDetail.isMale) Color(0xFF4396f8) else Color(
                                        0xFFEB5992
                                    )
                                )
                            }
                            user.basicDetail.tagList.forEach {
                                TagContent(title = it.content, iconRes = it.icon)
                            }
                        }
                    }

                } else {
                    Column {
                        Text(
                            text = "近期动态", style = TextStyle(
                                fontSize = 15.sp,
                                color = Color.White,
                            )
                        )
                        Spacer(Modifier.height(10.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            userBean.recentPost.picList.forEach {
                                RecentPostSingleContent(
                                    modifier = Modifier.size(40.dp),
                                    imgUrl = it
                                )
                            }
                        }
                    }
                }
            }
        }


    }

}

@Composable
private fun TagContent(
    modifier: Modifier = Modifier,
    @DrawableRes
    iconRes: Int? = null,
    title: String,
    backgroundColor: Color? = null,
) {
    Row(
        modifier
            .then(
                if (backgroundColor != null) {
                    Modifier.background(backgroundColor)
                } else {
                    Modifier.background(Color.Gray.copy(0.4f))
                }
            )
            .clip(RoundedCornerShape(4.dp))
            .padding(5.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 15.sp,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
private fun RecentPostSingleContent(
    modifier: Modifier = Modifier,
    imgUrl: String,
) {
    AsyncImage(
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, color = Color.White.copy(0.3f), shape = RoundedCornerShape(4.dp))
            .padding(1.dp),
        model = ImageRequest
            .Builder(LocalContext.current)
            .data(imgUrl)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun BottomMask(modifier: Modifier) {
    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(0.5f)
                        )
                    )
                )
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(30.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(0.5f),
                            Color.Black
                        )
                    )
                )
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(Color.Black)
        )
    }
}

@Composable
private fun HalvedClickArea(
    modifier: Modifier = Modifier,
    onClickStart: () -> Unit,
    onClickEnd: () -> Unit,
) {

    Row(modifier) {
        Box(
            Modifier
                .fillMaxSize()
                .weight(1f, false)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onClickStart()
                }
        )
        Box(
            Modifier
                .fillMaxSize()
                .weight(1f, false)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onClickEnd()
                }
        )
    }

}

private val picIndicatorSpacing = 5.dp

@Composable
private fun PicIndicator(
    modifier: Modifier = Modifier,
    @IntRange(from = 1) totalNum: Int,
    @IntRange(from = 0) curIndex: Int,
) {
    BoxWithConstraints(modifier) {
        val eachItemWidth = (maxWidth - picIndicatorSpacing * (totalNum - 1)) / totalNum
        val movableIndicatorOffset by animateIntAsState(
            targetValue = with(LocalDensity.current) { ((eachItemWidth + picIndicatorSpacing) * curIndex).roundToPx() },
            label = ""
        )
        Row(
            Modifier
                .fillMaxWidth()
                .height(2.dp),
            horizontalArrangement = Arrangement.spacedBy(picIndicatorSpacing)
        ) {
            repeat(totalNum) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .weight(1f, false)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(0.4f))
                )
            }
        }
        Box(
            Modifier
                .width(eachItemWidth)
                .height(2.dp)
                .offset { IntOffset(movableIndicatorOffset, 0) }
                .clip(RoundedCornerShape(50))
                .background(Color.White)
        )
    }


}

@Composable
@Preview
private fun PreviewTanTanSingleCard() {
    Box(
        Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        TanTanSingleCard(
            Modifier
                .fillMaxSize(),
            userBean = TanTanUserBean(
                name = "等待一个人",
                picList = listOf(
                    "https://5b0988e595225.cdn.sohucs.com/images/20190325/7613df5dd2094881bdf2b83115e3b3c3.jpeg",
                    "https://5b0988e595225.cdn.sohucs.com/images/20190325/94127d0f67da450e98e6f669070ad69b.jpeg",
                    "https://5b0988e595225.cdn.sohucs.com/images/20190325/881e9a9b620e44698aa4d64a8d756088.jpeg",
                    "https://5b0988e595225.cdn.sohucs.com/images/20190325/edf7266c067644c2a43346b7155703a7.jpeg"
                ),
                basicDetail = TanTanUserBean.BasicDetail(
                    isMale = false,
                    age = 14,
                    tagList = listOf(
                        TanTanUserBean.BasicDetail.Tag(
                            icon = R.drawable.ic_taxi,
                            content = "可外出"
                        ),
                        TanTanUserBean.BasicDetail.Tag(
                            icon = R.drawable.ic_find_more,
                            content = "发现更多"
                        ),
                    ),
                    location = "广州黄埔（10km）·11分钟前活跃"
                ),
                recentPost = TanTanUserBean.RecentPost(
                    picList = listOf(
                        "https://wx3.sinaimg.cn/mw690/001WN8zPly8hgvfjc0cxhj60j60cs41802.jpg",
                        "https://wx3.sinaimg.cn/mw690/001WN8zPly8hgvfjc6sxwj60j60de0vx02.jpg",
                        "https://wx4.sinaimg.cn/mw690/001WN8zPly8hgvfjciosfj60j60csgmv02.jpg"
                    )
                )
            )
        )
    }
}


@Composable
@Preview
private fun PreviewTanTanSwipeCard() {
    TanTanSwipeCard(modifier = Modifier
        .fillMaxSize()
        .padding(20.dp), userList = remember {
        (0..3).map {
            TanTanUserBean(
                name = "等待一个人",
                picList = listOf(
                    "https://5b0988e595225.cdn.sohucs.com/images/20190325/7613df5dd2094881bdf2b83115e3b3c3.jpeg",
                    "https://5b0988e595225.cdn.sohucs.com/images/20190325/94127d0f67da450e98e6f669070ad69b.jpeg",
                    "https://5b0988e595225.cdn.sohucs.com/images/20190325/881e9a9b620e44698aa4d64a8d756088.jpeg",
                    "https://5b0988e595225.cdn.sohucs.com/images/20190325/edf7266c067644c2a43346b7155703a7.jpeg"
                ),
                basicDetail = TanTanUserBean.BasicDetail(
                    isMale = false,
                    age = 14,
                    tagList = listOf(
                        TanTanUserBean.BasicDetail.Tag(
                            icon = R.drawable.ic_taxi,
                            content = "可外出"
                        ),
                        TanTanUserBean.BasicDetail.Tag(
                            icon = R.drawable.ic_find_more,
                            content = "发现更多"
                        ),
                    ),
                    location = "广州黄埔（10km）·11分钟前活跃"
                ),
                recentPost = TanTanUserBean.RecentPost(
                    picList = listOf(
                        "https://wx3.sinaimg.cn/mw690/001WN8zPly8hgvfjc0cxhj60j60cs41802.jpg",
                        "https://wx3.sinaimg.cn/mw690/001WN8zPly8hgvfjc6sxwj60j60de0vx02.jpg",
                        "https://wx4.sinaimg.cn/mw690/001WN8zPly8hgvfjciosfj60j60csgmv02.jpg"
                    )
                )
            )
        }
    })
}