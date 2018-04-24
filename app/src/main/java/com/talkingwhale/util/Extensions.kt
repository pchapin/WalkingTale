package com.talkingwhale.util

import android.app.Activity
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.talkingwhale.R
import com.talkingwhale.activities.AudioRecordActivity
import com.talkingwhale.activities.MainActivity
import com.talkingwhale.activities.MyPostsActivity
import com.talkingwhale.activities.SettingsActivity

fun Activity.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun Activity.snackbar(text: String) {
    Snackbar.make(this.window.decorView.findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show()
}

fun Fragment.toast(text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun Fragment.snackbar(text: String) {
    Snackbar.make(activity!!.window.decorView.findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show()
}

fun AppCompatActivity.navigateToFragment(fragment: Fragment?, replace: Boolean = true) {

    if (fragment?.javaClass == MainActivity::class.java) {
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        title = resources.getString(R.string.app_name)
    } else {
        supportActionBar?.setHomeAsUpIndicator(null)
    }

    when (fragment?.javaClass) {
        MyPostsActivity::class.java -> {
            title = resources.getString(R.string.title_activity_my_posts)
        }
        SettingsActivity::class.java -> {
        }
        AudioRecordActivity::class.java -> {
        }
    }

    if (replace) {
        supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, fragment)
                ?.addToBackStack(null)
                ?.commit()
    } else {
        supportFragmentManager?.beginTransaction()
                ?.add(R.id.container, fragment)
                ?.addToBackStack(null)
                ?.commit()
    }
}

fun Fragment.popBackStack() {
    activity?.supportFragmentManager?.popBackStack()
}