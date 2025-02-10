package com.buzzware.rapidouser.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.buzzware.rapidouser.databinding.ActivitySignUpPatientInfoBinding
import com.buzzware.rapidouser.databinding.AlertScheduleLayoutBinding
import com.buzzware.rapidouser.databinding.LayoutOtpDialogBinding
import com.buzzware.rapidouser.fragments.PaymentFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignUpPatientInfoActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpPatientInfoBinding
    private var dob = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPatientInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setView()
        setListener()
    }

    private fun setView() {
        binding.appBar.titleTV.text = "Sign Up"


    }

    private fun validateAndSignIn() {
        val firstName = binding.firstNameET.text.toString().trim()
        val lastName = binding.lastNameET.text.toString().trim()
        val email = binding.emailET.text.toString().trim()
        val phoneNumber = binding.phoneEt.text.toString().trim()
        val countryCode = binding.ccpTv.selectedCountryCodeWithPlus
        val fullPhoneNumber = "$countryCode$phoneNumber"

        val password = binding.passwordET.text.toString().trim()

        when {
            firstName.isEmpty() -> showToast("First name is required")
            lastName.isEmpty() -> showToast("Last name is required")
            email.isEmpty() -> showToast("Email is required")
            phoneNumber.isEmpty() -> showToast("Phone number is required")
            password.isEmpty() -> showToast("Password is required")
            dob.isEmpty() -> showToast("Date of Birth is required")
            else -> {
                otpDialog(firstName, lastName, email, fullPhoneNumber, password)
            }
        }
    }

    private fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        password: String
    ) {
        binding.progressBar.visibility = View.VISIBLE
        Firebase.auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                userDetails(firstName, lastName, email, phoneNumber, password,Firebase.auth.currentUser!!.uid, it)
            }.addOnFailureListener {
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                userDetails(firstName, lastName, email, phoneNumber, password, Firebase.auth.currentUser!!.uid,"")
            }
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(
                this@SignUpPatientInfoActivity,
                it.message.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun userDetails(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        password: String,
        id: String,
        token: String
    ) {
        val userData = hashMapOf(
            "dob" to dob,
            "doctorid" to "",
            "driverid" to "",
            "email" to email,
            "firstName" to firstName,
            "id" to id,
            "lastName" to lastName,
            "password" to password,
            "phoneNumber" to phoneNumber,
            "relation" to "",
            "relativeName" to "",
            "relativePhoneNumber" to "",
            "token" to token,
            "userRole" to "user"
        )

        Firebase.firestore.collection("Users").document(id).set(userData).addOnCompleteListener {
            if(it.isSuccessful){
                binding.progressBar.visibility = View.GONE
                val intent = Intent(this@SignUpPatientInfoActivity, ConsentFormActivity::class.java)
                startActivity(intent)
                overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
            }else{
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@SignUpPatientInfoActivity, it.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun otpDialog(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        password: String
    ) {
        val dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = LayoutOtpDialogBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.cancelTV.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.continueTV.setOnClickListener {
            signUp(firstName, lastName, email, phoneNumber, password)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date of Birth")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.show(supportFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val selectedDate = sdf.format(Date(selection))
            binding.birthET.setText(selectedDate)
            dob = selectedDate
        }
    }
    private fun setListener() {
        binding.appBar.backIV.setOnClickListener {
            finish()
        }

        binding.apply {

            birthET.setOnClickListener {
                showDatePicker()
            }

            createAccountTV.setOnClickListener {
                validateAndSignIn()
            }
        }
    }
}