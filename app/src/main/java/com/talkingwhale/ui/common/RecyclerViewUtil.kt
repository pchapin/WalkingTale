package com.talkingwhale.ui.common

import android.support.v7.widget.LinearSnapHelper

/**
 * Don't mess with the fling!
 * */
class BetterSnapper : LinearSnapHelper() {
    override fun onFling(velocityX: Int, velocityY: Int): Boolean {
        return false
    }
}
