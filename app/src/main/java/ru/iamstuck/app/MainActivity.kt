package ru.iamstuck.app

import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.toolbar.*

class MainActivity : BaseActivity(0) {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        header_text.setText(getString(R.string.help_requests))
        Log.d(TAG, "onCreate")
        setupBottomNavigation()

    }
}