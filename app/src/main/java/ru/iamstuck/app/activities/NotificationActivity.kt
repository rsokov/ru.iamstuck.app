package ru.iamstuck.app.activities

import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.toolbar.*
import ru.iamstuck.app.R

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