package ru.iamstuck.app

import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.toolbar.*

class NotificationActivity : BaseActivity(3) {
    private val TAG = "NotificationActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        header_text.setText(getString(R.string.notifications))
        Log.d(TAG, "onCreate")
        setupBottomNavigation()
        close_app()
    }
}