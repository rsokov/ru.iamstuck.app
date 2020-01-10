package ru.iamstuck.app.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import ru.iamstuck.app.R

class MainActivity : BaseActivity(0) {
    private val TAG = "MainActivity"
    private lateinit var mAuth: FirebaseAuth;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        header_text.setText(getString(R.string.help_requests))
        Log.d(TAG, "onCreate")
        setupBottomNavigation()
        close_app()
        mAuth = FirebaseAuth.getInstance()
//        auth.signInWithEmailAndPassword("r.sokov@at-nn.ru", "Qwerty123")
//            .addOnCompleteListener {
//                if (it.isSuccessful) {
//                    Log.d(TAG, "signIn: Complete successfully")
//                } else {
//                    Log.d(TAG, "signIn: Error")
//                }
//            }
        sign_out.setOnClickListener{
            mAuth.signOut()
        }
        mAuth.addAuthStateListener {
            if (it.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser==null)
       {
          startActivity(Intent(this, LoginActivity::class.java))
            finish()
       }
    }

}