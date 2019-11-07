package ru.iamstuck.app

import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.toolbar.*

class ChatActivity : BaseActivity(1) {
    private val TAG = "ChatActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        header_text.setText(getString(R.string.chat))
        Log.d(TAG, "onCreate")
        setupBottomNavigation()
        close_app()
    }
}