package com.buzzware.rapidouser.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import com.buzzware.rapidouser.Model.User
import com.buzzware.rapidouser.R
import com.buzzware.rapidouser.databinding.ActivitySignInBinding
import com.buzzware.rapidouser.utils.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class SignInActivity : AppCompatActivity() {

    lateinit var binding : ActivitySignInBinding
    private lateinit var auth : FirebaseAuth
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setView()
        setListener()

    }

    private fun setView() {

        binding.appBar.titleTV.text = "Log in"
        binding.apply {
            var isPasswordVisible = false

            passwordToggle.setOnClickListener {
                isPasswordVisible = !isPasswordVisible

                if (isPasswordVisible) {
                    passwordET.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    passwordToggle.setImageResource(R.drawable.icon_eye_show)
                } else {
                    passwordET.transformationMethod = PasswordTransformationMethod.getInstance()
                    passwordToggle.setImageResource(R.drawable.icon_eye_hide)
                }
                passwordET.setSelection(passwordET.text.length)
            }
        }

    }

    private fun setListener() {

        binding.appBar.backIV.setOnClickListener {
            finish()
        }

        binding.signupTV.setOnClickListener {
            val intent = Intent(this, SignUpPatientInfoActivity::class.java)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        }

        binding.loginTV.setOnClickListener {
            val email = binding.emailET.text.toString()
            val password = binding.passwordET.text.toString()

            when {
                email.isEmpty() -> binding.emailET.error = "Required"
                password.isEmpty() -> binding.passwordET.error = "Required"
                else -> login(email, password)
            }
        }
    }

    private fun login(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FirebaseMessaging.getInstance().token.addOnSuccessListener {
                    updateToken(it, task.result.user!!.uid)
                }.addOnFailureListener {
                    Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                    getUserDetails(task.result.user!!.uid)
                }
            } else {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateToken(token: String, userID: String) {
        val user = hashMapOf(
            "token" to token,
            "isOnline" to true
        ) as Map<String, Any>
        db.collection("Users").document(userID).update(user)
            .addOnSuccessListener {
                getUserDetails(userID)
            }.addOnFailureListener {
                Toast.makeText(this@SignInActivity, it.message.toString(), Toast.LENGTH_SHORT)
                    .show()
                getUserDetails(userID)
            }
    }

    private fun getUserDetails(uid: String) {
        db.collection("Users").document(uid).get().addOnSuccessListener { response ->

            if (response.exists()) {
                binding.progressBar.visibility = View.GONE
                val user = response.toObject(User::class.java)
                UserSession.user = user!!
                if (user.userRole.equals("user")) {
                    val intent = Intent(this, DashBoardActivity::class.java)
                    startActivity(intent)
                    finish()
                    overridePendingTransition(
                        androidx.appcompat.R.anim.abc_fade_in,
                        androidx.appcompat.R.anim.abc_fade_out
                    )
                } else {
                    auth.signOut()
                    Toast.makeText(this, "Access not granted", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { error ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            androidx.appcompat.R.anim.abc_fade_in,
            androidx.appcompat.R.anim.abc_fade_out
        )
    }
}