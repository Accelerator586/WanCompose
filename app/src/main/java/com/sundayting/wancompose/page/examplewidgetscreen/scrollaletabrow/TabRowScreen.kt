package com.sundayting.wancompose.page.examplewidgetscreen.scrollaletabrow

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sundayting.wancompose.R
import com.sundayting.wancompose.WanComposeDestination
import com.sundayting.wancompose.common.ui.tab.ApexScrollableTabRow
import com.sundayting.wancompose.common.ui.tab.ApexScrollableTabState
import com.sundayting.wancompose.common.ui.tab.rememberApexScrollableTabState
import com.sundayting.wancompose.common.ui.tab.tabIndicatorOffset
import com.sundayting.wancompose.common.ui.title.TitleBarProperties
import com.sundayting.wancompose.common.ui.title.TitleBarWithBackButtonContent
import com.sundayting.wancompose.common.ui.title.TitleBarWithContent
import com.sundayting.wancompose.page.examplewidgetscreen.ExampleCardBean
import kotlinx.coroutines.launch

object TabRowScreen : WanComposeDestination {
    override val route: String
        get() = "TabRow演示"

    val exampleCardBean = ExampleCardBean(
        name = route,
        resId = R.drawable.ic_tab_row
    )

    fun NavController.navigateToTabRowScreen() {
        navigate(route) {
            launchSingleTop = true
        }
    }

    @Composable
    fun Screen(
        modifier: Modifier = Modifier,
        onClickBackButton: () -> Unit = {},
    ) {
        val tabState = rememberApexScrollableTabState()
        val horizontalPagerState = rememberPagerState { 10 }
        val scope = rememberCoroutineScope()

        var quickSelect by remember { mutableStateOf(false) }
        val isDragged by horizontalPagerState.interactionSource.collectIsDraggedAsState()

        LaunchedEffect(Unit) {
            launch {
                snapshotFlow {
                    Triple(
                        isDragged,
                        quickSelect,
                        horizontalPagerState.currentPage
                    )
                }.collect {
                    if (!it.second) {
                        tabState.animateScrollToIndex(it.third)
                    }
                }
            }
            launch {
                snapshotFlow { isDragged }.collect { quickSelect = false }
            }
        }

        TitleBarWithContent(
            modifier,
            properties = TitleBarProperties(elevation = 0.dp),
            titleBarContent = {
                TitleBarWithBackButtonContent(
                    onClickBackButton = onClickBackButton
                ) {
                    Text(
                        "嵌套滑动",
                        style = TextStyle(
                            fontSize = 16.sp, color = Color.White
                        ),
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        ) {
            Column(
                Modifier
                    .padding(top = 20.dp)
                    .fillMaxSize()
            ) {
                ApexScrollableTabRow(
                    alignment = Alignment.CenterVertically,
                    state = tabState,
                    horizontalSpacing = 15.dp,
                    contentPaddingValues = PaddingValues(start = 20.dp, end = 30.dp),
                    indicator = {
                        Box(
                            Modifier
                                .tabIndicatorOffset(
                                    it[tabState.currentTabIndex],
                                    horizontalSpaceGetter = {
                                        10.dp
                                    })
                                .height(5.dp)
                                .background(Color.Red, shape = RoundedCornerShape(50))
                        )
                    },
                    tabs = {
                        (0..9).forEach {
                            val isSelect = tabState.currentTabIndex == it
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Blue.copy(0.2f))
                                    .height(50.dp)
                                    .clickable {
                                        scope.launch {
                                            quickSelect = true
                                            launch {
                                                tabState.animateScrollToIndex(it)
                                            }
                                            launch {
                                                horizontalPagerState.animateScrollToPage(
                                                    it, animationSpec = tween(
                                                        durationMillis = ApexScrollableTabState.ScrollableTabRowDuration,
                                                        easing = FastOutSlowInEasing
                                                    )
                                                )
                                            }

                                        }
                                    }
                                    .padding(horizontal = 10.dp + it.dp * 6),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "我是第${it}", style = TextStyle(
                                        color = if (isSelect) Color.Red.copy(0.7f) else Color.Black,
                                        fontWeight = if (isSelect) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                )

                Spacer(Modifier.height(10.dp))

                HorizontalPager(
                    state = horizontalPagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f, false)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background((if (it % 2 == 0) Color.Red else Color.Blue).copy(0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("我是第$it")
                    }
                }

            }
        }
    }
}