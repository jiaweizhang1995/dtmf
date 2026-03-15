package com.jimmymacmini.wishdtmf.feature.entry

import android.content.Context

private const val PREFS_NAME = "wish_dtmf_prefs"
private const val KEY_FIRST_LAUNCH_DONE = "first_launch_done"

class FirstLaunchPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val isFirstLaunch: Boolean
        get() = !prefs.getBoolean(KEY_FIRST_LAUNCH_DONE, false)

    fun markFirstLaunchDone() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH_DONE, true).apply()
    }
}
