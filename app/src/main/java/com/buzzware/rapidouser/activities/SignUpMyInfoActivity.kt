package com.buzzware.rapidouser.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.buzzware.rapidouser.databinding.ActivitySignUpMyInfoBinding

class SignUpMyInfoActivity : AppCompatActivity() {

    lateinit var binding : ActivitySignUpMyInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpMyInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setView()
        setListener()

    }

    private fun setView() {
        binding.appBar.titleTV.text = "Sign Up"
    }

    private fun setListener() {
        binding.appBar.backIV.setOnClickListener {
            finish()
        }

        binding.nextTV.setOnClickListener {
            if (validateInputs()) {
                val fullPhoneNumber = "+${binding.ccp.selectedCountryCode}${binding.relativePhoneNumberEt.text.toString().trim()}"

                val intent = Intent(this@SignUpMyInfoActivity, SignUpPatientInfoActivity::class.java).apply {
                    putExtra("relation", binding.relationET.text.toString().trim())
                    putExtra("relativeName", binding.nameET.text.toString().trim())
                    putExtra("relativePhoneNumber", fullPhoneNumber)
                }
                startActivity(intent)
                overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
            }
        }

    }

    private fun validateInputs(): Boolean {
        val relation = binding.relationET.text.toString().trim()
        val relativeName = binding.nameET.text.toString().trim()
        val phoneNumber = binding.relativePhoneNumberEt.text.toString().trim()

        return when {
            relation.isEmpty() -> {
                binding.relationET.error = "Relation is required"
                false
            }
            relativeName.isEmpty() -> {
                binding.nameET.error = "Relative name is required"
                false
            }
            phoneNumber.isEmpty() -> {
                binding.relativePhoneNumberEt.error = "Phone number is required"
                false
            }
            phoneNumber.length < 8 -> {
                binding.relativePhoneNumberEt.error = "Enter a valid phone number"
                false
            }
            else -> true
        }
    }
}