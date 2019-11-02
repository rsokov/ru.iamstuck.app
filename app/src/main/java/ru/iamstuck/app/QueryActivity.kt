package ru.iamstuck.app

import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.toolbar.*

class QueryActivity : BaseActivity(2) {
    private val TAG = "QueryActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        header_text.setText(getString(R.string.help_query))
        Log.d(TAG, "onCreate")
        setupBottomNavigation()
    }
}