package com.tengfei.github.view

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import com.tengfei.common.ext.no
import com.tengfei.common.ext.otherWise
import com.tengfei.common.ext.yes
import com.tengfei.github.R
import com.tengfei.github.entity.User
import com.tengfei.github.model.account.AccountManager
import com.tengfei.github.model.account.OnAccountStateChangeListener
import com.tengfei.github.settings.Themer
import com.tengfei.github.utils.afterClosed
import com.tengfei.github.utils.doOnLayoutAvailable
import com.tengfei.github.utils.loadWithGlide
import com.tengfei.github.utils.showFragment
import com.tengfei.github.view.config.NavViewItem
import com.tengfei.github.view.widget.ActionBarController
import com.tengfei.github.view.widget.NavigationController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.android.synthetic.main.toggle_day_night.view.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(), OnAccountStateChangeListener {

    private val navigationController by lazy {
        NavigationController(navigationView, ::onNavItemChanged)
    }

    val actionBarController by lazy {
        ActionBarController(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Themer.applyTheme(this)
        setContentView(R.layout.activity_main)
        setSupportActionBar(appBarMainToolbar)
        val toggle = ActionBarDrawerToggle(this, mainDrawerLayout, appBarMainToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        mainDrawerLayout.setDrawerListener(toggle)
        toggle.syncState()
        initNavigationView()


    }

    private fun initNavigationView() {

        AccountManager.onAccountStateChangeListeners.add(this)
        /**
         * Kotlin 中的let，当不为Null时执行let后面的代码块
         */
        AccountManager.currentUser?.let(::updateNavigationView) ?: run(::clearNavigationView)

        initNavigationViewEvent()

        navigationController.selectProperItem()

    }

    private fun initNavigationViewEvent() {
        navigationView.doOnLayoutAvailable {

            navHeaderLayout.setOnClickListener {
                AccountManager.isLogin().no {
                    //跳转到登陆界面
                    startActivity(Intent(this, LoginActivity::class.java))
                }.otherWise {
                    AccountManager.loginOut().subscribe({
                        toast("注销账号")
                    }, {
                        it.printStackTrace()
                    })
                }
            }
        }
    }

    private fun updateNavigationView(user: User) {
        navigationView.doOnLayoutAvailable {
            navHeaderUsernameView.text = user.name
            navHeaderEmailView.text = user.email ?: ""
            //使用 AvatarImageView 原因在于如果没有图片的话可以帮助我们显示一个字符充当图片
            navHeaderAvatarImageView.loadWithGlide(user.avatar_url, user.name.first())
        }
    }

    private fun clearNavigationView() {
        navigationView.doOnLayoutAvailable {
            navHeaderUsernameView.text = resources.getText(R.string.click_to_login)
            navHeaderEmailView.text = ""
            navHeaderAvatarImageView.imageResource = R.drawable.ic_github
        }
    }

    private fun onNavItemChanged(navViewItem: NavViewItem) {
        mainDrawerLayout.afterClosed {
            showFragment(R.id.fragmentContainer, navViewItem.fragmentClass)
            title = navViewItem.title
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_day_night, menu)
        menu!!.findItem(R.id.actionItemDayNight).actionView.dayNightSwitch.apply {
            isChecked = Themer.currentTheme() == Themer.ThemeModel.NIGHT

            setOnCheckedChangeListener { buttonView, isChecked ->
                Themer.toggleTheme(this@MainActivity)
            }



        }
        return true
    }


    /**
     * 回调登陆成功
     */
    override fun onLogin(user: User) = updateNavigationView(user)

    /**
     * 回调退出登陆
     */
    override fun onLoginOut() = clearNavigationView()

    override fun onBackPressed() {
        mainDrawerLayout.isDrawerOpen(GravityCompat.START).yes {
            mainDrawerLayout.closeDrawer(GravityCompat.START)
        }.otherWise {
            super.onBackPressed()
        }

    }

    override fun onDestroy() {
        AccountManager.onAccountStateChangeListeners.remove(this)
        super.onDestroy()
    }
}

