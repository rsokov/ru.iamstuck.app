package ru.iamstuck.app.activities

import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_main_login_screen.*
import kotlinx.android.synthetic.main.fragment_main_login_screen.email_input
import kotlinx.android.synthetic.main.fragment_main_login_screen.password_input
import kotlinx.android.synthetic.main.fragment_register_email.*
import kotlinx.android.synthetic.main.fragment_register_email.next_btn
import kotlinx.android.synthetic.main.fragment_register_namepass.*
import kotlinx.android.synthetic.main.fragment_register_phone.*
import kotlinx.android.synthetic.main.fragment_register_verification.*
import net.yslibrary.android.keyboardvisibilityevent.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.w3c.dom.Text
import ru.iamstuck.app.R
import ru.iamstuck.app.models.User
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.MaskFormatWatcher
import java.util.concurrent.TimeUnit

class ComplexLoginActivity : AppCompatActivity(), MainLoginScreenFragment.Listener, SignUpbyEmailFragment.Listener,
    SignUpPasswordFragment.Listener, SignInByPhoneFragment.Listener, SignInByPhoneCodeVerification.Listener {
    private val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
    private val STATE_INITIALIZED = 1
    private val STATE_VERIFY_FAILED = 3
    private val STATE_VERIFY_SUCCESS = 4
    private val STATE_CODE_SENT = 2
    private val STATE_SIGNIN_FAILED = 5
    private val STATE_SIGNIN_SUCCESS = 6
    //Set logcat TAG field
    private val TAG = this::class.java.getName()
    //Set mAuth field for FirebaseAuth
    private lateinit var mAuth: FirebaseAuth
    //Set Email field
    private var mEmail: String? = null
    //Set Firebase Database fields
    private lateinit var mDatabase: DatabaseReference

    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private var isSuccesful = false
    private var correctPhone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complex_login)
        //send to logcat initial activity state
        Log.d(TAG, "onCreate")
        //Get instance from Firebase
        mAuth = FirebaseAuth.getInstance()
        //Get instance from Firebase Database
        mDatabase = FirebaseDatabase.getInstance().reference


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.frame_layout, MainLoginScreenFragment())
                .commit()
        }


    }

    //Email auth function
    override fun authByEmail(email: String, password: String) {
        if (EmailPasswordValidation(email, password)) {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    showToast("Учетная запись не найдена")

                }

            }
        }
    }

    //Email&Pass validation
    private fun EmailPasswordValidation(email: String, password: String) =
        email.isNotEmpty() && password.isNotEmpty()

    //Open Phone LoginScreen
    override fun signInByPhone() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, SignInByPhoneFragment())
            .addToBackStack(null)
            .commit()
    }

    //Open SignUp screen
    override fun signUp() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, SignUpbyEmailFragment())
            .addToBackStack(null)
            .commit()
    }

    //Go to next step -> entering password
    override fun signUpbyEmailPassword(email: String) {
        if (email.isNotEmpty() && email.contains('@')) {
            mEmail = email;
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, SignUpPasswordFragment())
                .addToBackStack(null)
                .commit()
        } else {
            showToast(getString(R.string.correctEmail))
        }
    }

    //Open SignUp by Phone screen
    override fun signUpByPhone() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, SignInByPhoneFragment())
            .addToBackStack(null)
            .commit()
    }

    //Confirm email and password registration
    override fun signUpbyEmailConfirmRegistration(fullName: String, password: String) {
        if (fullName.isNotEmpty() && password.isNotEmpty()) {
            val email = mEmail
            if (email != null) {
                if (password.length >= 6) {
                    signUpbyEmailWorker(email, password, fullName)
                } else {
                    Log.e(TAG, "onRegister: Password length less than 6 symbols")
                    showToast(getString(R.string.correctPassword))
                }
            } else {
                Log.e(TAG, "onRegister: Email is null or incorrect")
                showToast(R.string.correctEmail.toString())
                supportFragmentManager.popBackStack()
            }
        } else {
            showToast(getString(R.string.enterNameAndPassword))
        }
    }

    //Email and Pass registration Worker
    private fun signUpbyEmailWorker(email: String, password: String, fullName: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = createUserprofile(fullName, email)
                    val reference = mDatabase.child("users").child(it.result!!.user.uid)
                    reference.setValue(user).addOnCompleteListener {
                        if (it.isSuccessful) {
                            startMainActivity()
                        } else {
                            unknownRegisterError(it)
                        }
                    }
                } else {
                    unknownRegisterError(it)
                }
            }
    }

    //Create object of User profile
    private fun createUserprofile(fullName: String, email: String): User {
        return User(name = fullName, email = email)
    }

    //Get exception if something went wrong
    private fun unknownRegisterError(it: Task<*>) {
        Log.e(TAG, "failed to create user profile", it.exception)
        showToast(getString(R.string.showError))
    }

    //End of Email SignUp and start MainActivity
    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    //Phone Number Formatter
    private fun formatPhoneNumber(phone: String): String {
        var correctPhone = phone.replace("(", "")
        correctPhone = correctPhone.replace(")", "")
        correctPhone = correctPhone.replace(" ", "")
        correctPhone = correctPhone.replace("-", "")
        return correctPhone;
    }

    //Go to next step -> code verification <- in SignInByPhone process
    override fun onNextStepSignInByPhone(phone: String) {
        correctPhone = formatPhoneNumber(phone)
        if (correctPhone.length == 12) {
            if (!validatePhoneNumber()) {
                return
            }
            startPhoneNumberVerification(correctPhone)
            sendVerificationCode(correctPhone)

            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, SignInByPhoneCodeVerification())
                .addToBackStack(null)
                .commit()

        } else {
            showToast(getString(R.string.correctPhone))
        }
    }

    //
    override fun SignInByPhoneCodeVerify(code: String) {
        var codeFormat = code.replace(" ", "")
        verifyPhoneNumberWithCode(storedVerificationId, codeFormat)
    }

    //
    private fun sendVerificationCode(mobile: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            mobile,
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            mCallbacks
        );
    }

    //
    private var mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            // [START_EXCLUDE silent]
            verificationInProgress = false
            // [END_EXCLUDE]

            // [START_EXCLUDE silent]
            // Update the UI and attempt sign in with the phone credential
            updateUI(STATE_VERIFY_SUCCESS, credential)
            // [END_EXCLUDE]
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)
            // [START_EXCLUDE silent]
            verificationInProgress = false
            // [END_EXCLUDE]

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // [START_EXCLUDE]
                //fieldPhoneNumber.error = R.string.correctPhone
                // [END_EXCLUDE]
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // [START_EXCLUDE]
                Snackbar.make(
                    findViewById(android.R.id.content), "Quota exceeded.",
                    Snackbar.LENGTH_SHORT
                ).show()
                // [END_EXCLUDE]
            }

            // Show a message and update the UI
            // [START_EXCLUDE]
            updateUI(STATE_VERIFY_FAILED)
            // [END_EXCLUDE]
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token

            // [START_EXCLUDE]
            // Update UI
            updateUI(STATE_CODE_SENT)
            // [END_EXCLUDE]
        }
    }

    //
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        updateUI(currentUser)

        // [START_EXCLUDE]
        if (verificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(correctPhone)
        }

    }

    //
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
    }

    //
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    //
    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            mCallbacks
        ) // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]
        verificationInProgress = true
    }

    //
    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }

    //
// [START resend_verification]
    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            mCallbacks, // OnVerificationStateChangedCallbacks
            token
        ) // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    // [START_EXCLUDE]
                    updateUI(STATE_SIGNIN_SUCCESS, user)
                    // [END_EXCLUDE]
                    isSuccesful = true
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        // [START_EXCLUDE silent]
                        showToast(getString(R.string.wrongCode))

                        // [END_EXCLUDE]
                    }
                    // [START_EXCLUDE silent]
                    // Update UI
                    updateUI(STATE_SIGNIN_FAILED)
                    // [END_EXCLUDE]
                }
            }
    }
    // [END sign_in_with_phone]

    //
    private fun signOut() {
        mAuth.signOut()
        updateUI(STATE_INITIALIZED)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user)
        } else {
            updateUI(STATE_INITIALIZED)
        }
    }

    private fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    private fun updateUI(
        uiState: Int,
        user: FirebaseUser? = mAuth.currentUser,
        cred: PhoneAuthCredential? = null
    ) {
        when (uiState) {
            STATE_INITIALIZED -> {
                // Initialized state, show only the phone number field and start button
                //enableViews(buttonStartVerification, fieldPhoneNumber)
                //disableViews(buttonVerifyPhone, buttonResend, fieldVerificationCode)
                //detail.text = null
            }
            STATE_CODE_SENT -> {
                // Code sent state, show the verification field, the
                //enableViews(buttonVerifyPhone, buttonResend, fieldPhoneNumber, fieldVerificationCode)
                //disableViews(buttonStartVerification)
                //detail.setText(R.string.status_code_sent)
            }
            STATE_VERIFY_FAILED -> {
                // Verification has failed, show all options
                //enableViews(buttonStartVerification, buttonVerifyPhone, buttonResend, fieldPhoneNumber,
                //    fieldVerificationCode)
                //detail.setText(R.string.status_verification_failed)
            }
            STATE_VERIFY_SUCCESS -> {
                // Verification has succeeded, proceed to firebase sign in
                //disableViews(buttonStartVerification, buttonVerifyPhone, buttonResend, fieldPhoneNumber,
                //    fieldVerificationCode)
                //detail.setText(R.string.status_verification_succeeded)

                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.smsCode != null) {
                        register_code_input.setText(cred.smsCode)
                    } else {
                        //fieldVerificationCode.setText(R.string.instant_validation)
                    }
                }
            }
            STATE_SIGNIN_FAILED -> {

            }
            // No-op, handled by sign-in check
            //detail.setText(R.string.status_sign_in_failed)
            STATE_SIGNIN_SUCCESS -> {
            }
        } // Np-op, handled by sign-in check

        if (user == null) {
            // Signed out
            //phoneAuthFields.visibility = View.VISIBLE
            //signedInButtons.visibility = View.GONE

            //status.setText(R.string.signed_out)
        } else {
            // Signed in
            //
            /*
            phoneAuthFields.visibility = View.GONE

            signedInButtons.visibility = View.VISIBLE

            enableViews(fieldPhoneNumber, fieldVerificationCode)
            fieldPhoneNumber.text = null
            fieldVerificationCode.text = null

            status.setText(R.string.signed_in)
            detail.text = getString(R.string.firebase_status_fmt, user.uid)

             */
        }
    }

    private fun validatePhoneNumber(): Boolean {
        val phoneNumber = correctPhone
        if (TextUtils.isEmpty(phoneNumber)) {
            showToast("Некорректный номер телефона")
            return false
        }

        return true
    }

    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }


}

    // 1 - Main Login Screen Fragment
    class MainLoginScreenFragment : Fragment(), TextWatcher {
        private lateinit var mListener: Listener

        interface Listener {
            fun authByEmail(email: String, password: String)
            fun signInByPhone()
            fun signUp()
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_main_login_screen, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


            login_btn.isEnabled = false
            email_input.addTextChangedListener(this)
            password_input.addTextChangedListener(this)
            login_btn.setOnClickListener {
                val email = email_input.text.toString()
                val password = password_input.text.toString()
                mListener.authByEmail(email, password)

            }
            phone_login_btn.setOnClickListener {
                mListener.signInByPhone()
            }
            create_account_text.setOnClickListener {
                mListener.signUp()
            }
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            mListener = context as Listener
        }


        //Login button's Inactive status checker and Activator
        override fun afterTextChanged(s: Editable?) {
            login_btn.isEnabled =
                validate(email_input.text.toString(), password_input.text.toString())
        }

        private fun validate(email: String, password: String) =
            email.isNotEmpty() && password.isNotEmpty()

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        /*override fun onVisibilityChanged(isKeyboardOpen: Boolean) {
            if (isKeyboardOpen ) {
                scroll_view.scrollTo(0, scroll_view.bottom)
                create_account_text.visibility = View.GONE
            } else {
                scroll_view.scrollTo(0, scroll_view.top)
                create_account_text.visibility = View.VISIBLE
            }

        }
         */

    }

    //2 - SignUp Screen Fragment
    class SignUpbyEmailFragment : Fragment() {
        private lateinit var mListener: Listener

        interface Listener {
            fun signUpbyEmailPassword(email: String)
            fun signUpByPhone()
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_register_email, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            next_btn.setOnClickListener {
                val email = email_input.text.toString()
                mListener.signUpbyEmailPassword(email)
            }
            phone_register_btn.setOnClickListener {
                mListener.signUpByPhone()
            }
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            mListener = context as Listener
        }

    }

    //3 - SignUp Password Fragment
    class SignUpPasswordFragment : Fragment() {
        private lateinit var mListener: Listener

        interface Listener {
            fun signUpbyEmailConfirmRegistration(fullName: String, password: String)

        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_register_namepass, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            register_btn.setOnClickListener {
                val fullName = full_name_input.text.toString()
                val password = password_input.text.toString()
                mListener.signUpbyEmailConfirmRegistration(fullName, password)
            }
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            mListener = context as Listener
        }
    }

    //4 - SignIn by Phone
    class SignInByPhoneFragment : Fragment() {
        private lateinit var mListener: Listener

        interface Listener {
            fun onNextStepSignInByPhone(phone: String)
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_register_phone, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            val mask = MaskImpl.createTerminated(PredefinedSlots.RUS_PHONE_NUMBER)
            mask.isHideHardcodedHead = false
            mask.setHideHardcodedHead(false)// default value
            val formatWatcher = MaskFormatWatcher(mask)
            formatWatcher.installOn(register_phone_input)
            next_btn.setOnClickListener {
                val phone = register_phone_input.text.toString()
                mListener.onNextStepSignInByPhone(phone)
            }
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            mListener = context as Listener
        }
    }

    //5 - Verification Code
    class SignInByPhoneCodeVerification : Fragment() {
        private lateinit var mListener: Listener

        interface Listener {
            fun SignInByPhoneCodeVerify(code: String)
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_register_verification, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            var slots = UnderscoreDigitSlotsParser().parseSlots("_ _ _ _ _ _")
            val formatWatcher = MaskFormatWatcher(MaskImpl.createTerminated(slots))
            formatWatcher.installOn(register_code_input) // install on any TextView
            next_btn.setOnClickListener {
                val code = register_code_input.text.toString()
                mListener.SignInByPhoneCodeVerify(code)
            }
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            mListener = context as Listener
        }
    }

//6 - SignUp by Phone



