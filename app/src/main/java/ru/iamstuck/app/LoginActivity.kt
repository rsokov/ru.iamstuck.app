package ru.iamstuck.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.toolbar.*

class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        header_text.setText(getString(R.string.mutual_app))
        setContentView(R.layout.activity_login)
        Log.d(TAG, "onCreate")
    }


}
