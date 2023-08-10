package com.sundayting.wancompose.page.homescreen.article

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundayting.wancompose.page.homescreen.article.repo.ArticleRepository
import com.sundayting.wancompose.page.homescreen.article.repo.HomePageService2
import com.sundayting.wancompose.page.homescreen.article.ui.ArticleList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val repo: ArticleRepository,
    private val homePageService2: HomePageService2,
) : ViewModel() {

    val state = ArticleState()

    @Stable
    class ArticleState {

        val articleList = mutableStateListOf<ArticleList.ArticleUiBean>()

        val bannerList = mutableStateListOf<ArticleList.BannerUiBean>()

        var refreshing by mutableStateOf(false)
        var loadingMore by mutableStateOf(false)

    }

    private var curPage = 0

    init {
        refresh()
    }

    private fun addArticle(list: List<ArticleList.ArticleUiBean>, refreshFirst: Boolean = false) {
        if (refreshFirst) {
            state.articleList.clear()
        }
        state.articleList.addAll(list)
    }

    private fun addTopArticle(list: List<ArticleList.ArticleUiBean>) {
        state.articleList.addAll(0, list)
    }

    fun refresh() {
        load(true)
    }

    fun loadMore() {
        load(false)
    }

    private var loadJob: Job? = null

    private fun load(isRefresh: Boolean) {
        if (isRefresh.not() && loadJob?.isActive == true) {
            return
        }
        loadJob?.cancel()
        if (isRefresh) {
            curPage = 0
            state.loadingMore = true
        } else {
            state.loadingMore = true
        }
        loadJob = viewModelScope.launch {
            val result = runCatching { homePageService2.getHomePage() }
            val bean = result.getOrNull()
            if (bean != null) {
                bean.data?.let {
                    it.forEach {
                        Log.d("临时测试", "$it")
                    }
                }

            }
            Log.d("临时测试", "$bean")
//            joinAll(
//                launch {
//                    if (isRefresh) {
//                        val result = repo.fetchHomePageBanner()
//                        if (result.isNSuccess()) {
//                            result.body.data?.let { list ->
//                                state.bannerList.clear()
//                                state.bannerList.addAll(list.map { it.toBannerUiBean() })
//                            }
//                        }
//                    }
//                },
//                launch {
//                    val topArticleListDeferred = async {
//                        if (isRefresh) {
//                            val result = repo.fetchHomePageTopArticle()
//                            if (result.isNSuccess() && result.body.data != null) {
//                                result.body.data
//                            } else {
//                                null
//                            }
//                        } else {
//                            null
//                        }
//                    }
//
//                    val articleListDeferred = async {
//                        val result = repo.fetchHomePageArticle(curPage)
//                        if (result.isNSuccess()) {
//                            result.body.data?.list
//                        } else {
//                            null
//                        }
//                    }
//
//                    val topArticleList = topArticleListDeferred.await()
//                    val articleList = articleListDeferred.await()
//
//                    if (articleList != null) {
//                        addArticle(articleList.map { it.toArticleUiBean() }, isRefresh)
//                    }
//                    if (topArticleList != null) {
//                        addTopArticle(topArticleList.map { it.toArticleUiBean(true) })
//                    }
//                },
//            )
        }.apply {
            invokeOnCompletion {
                state.refreshing = false
                state.loadingMore = false
            }
        }
    }

}