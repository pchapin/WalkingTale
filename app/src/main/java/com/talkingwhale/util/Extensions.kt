package com.talkingwhale.util

import android.app.Activity
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.widget.Toast
import com.talkingwhale.R

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

fun FragmentActivity.navigateToFragment(fragment: Fragment?) {
    this.supportFragmentManager?.beginTransaction()
            ?.add(R.id.container, fragment)
            ?.commit()
}