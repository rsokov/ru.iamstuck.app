package ru.iamstuck.app.activities

import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.toolbar.*
import ru.iamstuck.app.R

class QueryActivity : BaseActivity(2) {
    private val TAG = this::class.java.getName()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        header_text.setText(getString(R.string.help_query))
        Log.d(TAG, "onCreate")
        setupBottomNavigation()
        close_app()
    }
}