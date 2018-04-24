package com.talkingwhale.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.talkingwhale.R
import com.talkingwhale.util.navigateToFragment
import kotlinx.android.synthetic.main.activity_container.*

class ContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        navigateToFragment(SplashActivity())
        navigationDrawer()
    }

    private fun isOnlyFragmentMain(): Boolean {
        return supportFragmentManager.fragments.map { it::class.java }.last() == MainActivity::class.java
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {
                    // Open the navigation drawer when the home icon is selected from the toolbar.
                    if (isOnlyFragmentMain()) {
                        drawer_layout.openDrawer(GravityCompat.START)
                        true
                    } else {
                        supportFragmentManager.popBackStackImmediate()
                        if (isOnlyFragmentMain()) {
                            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
                        }
                        false
                    }
                }
                else -> super.onOptionsItemSelected(item)
            }

    private fun navigationDrawer() {
        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_help -> {
                    navigateToFragment(HelpActivity())
                }
                R.id.action_about -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://toddcooke.github.io/walking-tale-site/")))
                }
                R.id.action_my_posts -> {
                    navigateToFragment(MyPostsActivity())
                }
                R.id.action_sign_out -> {
//                    logout()
                }
                R.id.action_settings -> {
                    navigateToFragment(SettingsActivity())
                }
            }
            drawer_layout.closeDrawer(GravityCompat.START)
            return@setNavigationItemSelectedListener true
        }
    }
}
