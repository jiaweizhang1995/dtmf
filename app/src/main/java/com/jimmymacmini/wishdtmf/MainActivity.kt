package com.jimmymacmini.wishdtmf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jimmymacmini.wishdtmf.app.WishDtmfApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WishDtmfApp()
        }
    }
}
