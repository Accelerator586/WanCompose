package com.sundayting.wancompose.page.homescreen.mine.repo

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.room.withTransaction
import com.sundayting.wancompose.datastore.dataStore
import com.sundayting.wancompose.db.WanDatabase
import com.sundayting.wancompose.function.UserLoginFunction.CURRENT_LOGIN_ID_KEY
import com.sundayting.wancompose.function.UserLoginFunction.UserEntity
import com.sundayting.wancompose.function.UserLoginFunction.UserInfoBean
import com.sundayting.wancompose.network.okhttp.isNSuccess
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MineRepository @Inject constructor(
    private val mineService: MineService,
    private val database: WanDatabase,
    @ApplicationContext context: Context,
) {

    private val dataStore = context.dataStore

    val curUserFlow = dataStore.data
        .mapLatest { it[CURRENT_LOGIN_ID_KEY] }
        .flatMapLatest {
            database.userDao().currentUserFlow(it ?: 0)
        }

    private suspend fun login(
        username: String,
        password: String,
    ) = mineService.login(username, password)


    /**
     * 登出
     */
    suspend fun logout() = mineService.logout()

    private suspend fun fetchUserInfo() = mineService.fetchUserInfo()

    suspend fun clearLoginUser() {
        coroutineScope {
            joinAll(
                launch {
                    dataStore.edit { mp ->
                        mp[CURRENT_LOGIN_ID_KEY] = 0
                    }
                },
                launch {
                    database.userDao().clear()
                }
            )
        }
    }

    suspend fun loginAndAutoInsertData(
        username: String,
        password: String,
    ): UserInfoBean? {
        return coroutineScope {
            val loginResult = login(username, password)
            return@coroutineScope if (loginResult.isNSuccess()) {
                val userInfoResult = fetchUserInfo()
                if (userInfoResult.isNSuccess()) {
                    val userInfoBean = userInfoResult.body.data
                    if (userInfoBean != null) {
                        joinAll(
                            launch {
                                database.withTransaction {
                                    database.userDao().clear()
                                    database.userDao().insertUser(
                                        UserEntity(
                                            id = userInfoBean.userInfo.id,
                                            nick = userInfoBean.userInfo.nickname,
                                            coinCount = userInfoBean.coinInfo.coinCount,
                                            level = userInfoBean.coinInfo.level,
                                            rank = userInfoBean.coinInfo.rank
                                        )
                                    )
                                }
                            },
                            launch {
                                dataStore.edit { mp ->
                                    mp[CURRENT_LOGIN_ID_KEY] = userInfoBean.userInfo.id
                                }
                            }
                        )
                        userInfoBean
                    } else {
                        null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    suspend fun register(
        username: String,
        password: String,
        passwordAgain: String,
    ) = mineService.register(username, password, passwordAgain)


}