package com.talkingwhale.util

import android.app.Activity
import android.support.design.widget.Snackbar
import android.widget.Toast

fun Activity.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun Activity.snackbar(text: String) {
    Snackbar.make(this.window.decorView.findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show()
}