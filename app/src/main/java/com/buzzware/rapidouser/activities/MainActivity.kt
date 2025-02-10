package com.buzzware.rapidouser.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.buzzware.rapidouser.Model.User
import com.buzzware.rapidouser.databinding.ActivityMainBinding
import com.buzzware.rapidouser.utils.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    private lateinit var auth : FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        setView()
        setListener()

    }

    private fun setView() {

    }

    private fun setListener() {

        binding.loginTV.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        }

        binding.signupTV.setOnClickListener {
            val intent = Intent(this, AccountSelectionActivity::class.java)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            binding.progressBar.visibility = View.VISIBLE
            val userId = auth.currentUser!!.uid
            db.collection("Users").document(userId)
                .get().addOnSuccessListener { task ->
                    binding.progressBar.visibility = View.GONE
                    val user = task.toObject(User::class.java)
                    user!!.id = userId
                    UserSession.user = user
                    val intent = Intent(this@MainActivity, DashBoardActivity::class.java)
                    intent.putExtra("user","user")
                    startActivity(intent)
                    finish()
                    overridePendingTransition(
                        androidx.appcompat.R.anim.abc_fade_in,
                        androidx.appcompat.R.anim.abc_fade_out
                    )
                }.addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Handler().postDelayed(Runnable {
                        startActivity(Intent(this, SignInActivity::class.java))
                        overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                        finish()
                    }, 2000)
                }
        }else{
            binding.progressBar.visibility = View.GONE
        }
    }
}