package ru.iamstuck.app.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_edit_profile.*
import ru.iamstuck.app.R
import ru.iamstuck.app.models.User

class EditProfileActivity : AppCompatActivity() {
    private val TAG = this::class.java.getName()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "onCreate")

        close_image.setOnClickListener{
            finish()
        }

        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                val user = it.getValue(User::class.java)
                name_input.setText(user!!.name, TextView.BufferType.EDITABLE)
                city_input.setText(user.city, TextView.BufferType.EDITABLE)
                auto_vendor_input.setText(user.auto_vendor, TextView.BufferType.EDITABLE)
                auto_model_input.setText(user.auto_model, TextView.BufferType.EDITABLE)
                auto_number_input.setText(user.auto_number, TextView.BufferType.EDITABLE)
                phone_input.setText(user.phone.toString(), TextView.BufferType.EDITABLE)
                email_input.setText(user.email, TextView.BufferType.EDITABLE)
            })

    }
}
