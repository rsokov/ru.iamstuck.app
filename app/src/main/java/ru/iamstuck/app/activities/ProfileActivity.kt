package ru.iamstuck.app.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.toolbar.*
import ru.iamstuck.app.R

class ProfileActivity : BaseActivity(4) {
    private val TAG = this::class.java.getName()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        header_text.setText(getString(R.string.my_profile))
        Log.d(TAG, "onCreate")
        setupBottomNavigation()
        close_app()

        edit_profile_btn.setOnClickListener{
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }
}