package com.buzzware.rapidouser.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.buzzware.rapidouser.Model.User
import com.buzzware.rapidouser.R
import com.buzzware.rapidouser.activities.MainActivity
import com.buzzware.rapidouser.activities.SignInActivity
import com.buzzware.rapidouser.constants.FragmentListener
import com.buzzware.rapidouser.databinding.FragmentHomeBinding
import com.buzzware.rapidouser.databinding.FragmentProfileBinding
import com.buzzware.rapidouser.utils.UserSession
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private lateinit var fragmentContext: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        setView()
        setListener()

        return binding.root
    }

    private fun setView() {

        binding.apply {
            if(UserSession.user.image.isNotEmpty()){
                Glide.with(fragmentContext).load(UserSession.user.image).into(userProfileIV)
            }

            fullNameTv.text =  "${UserSession.user.firstName}  ${UserSession.user.lastName}"
            usernameTv.text = "@${UserSession.user.firstName}${UserSession.user.lastName}"
        }
    }

    private fun setListener() {
        binding.accountLayout.setOnClickListener {
            (activity as FragmentListener).loadFragment("profileAccount")
        }

        binding.notificationLayout.setOnClickListener {
            (activity as FragmentListener).loadFragment("profileNotification")
        }

        binding.logoutLayout.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            Handler().postDelayed(Runnable {
                Firebase.auth.signOut()
                binding.progressBar.visibility = View.GONE
                startActivity(Intent(fragmentContext, MainActivity::class.java))
                requireActivity().finish()
            }, 1000)
        }

        binding.deleteLayout.setOnClickListener {
            deleteDialog()
        }


    }

    private fun deleteDialog() {
        val builder = AlertDialog.Builder(fragmentContext)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure to delete account!")

        builder.setPositiveButton("Yes") { dialog, which ->
            binding.progressBar.visibility = View.VISIBLE
            val user = Firebase.auth.currentUser!!
            Firebase.firestore.collection("Users").document(user.uid).delete().addOnCompleteListener {
                if(it.isSuccessful){
                    user.delete().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            binding.progressBar.visibility = View.GONE
                            requireActivity().startActivity(Intent(fragmentContext, MainActivity::class.java))
                            requireActivity().finish()
                        } else {
                            binding.progressBar.visibility = View.GONE
                            Log.d("Logger", "setListener: ${task.exception!!.message}")
                            dialog.dismiss()
                            Toast.makeText(
                                fragmentContext,
                                "${task.exception!!.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }else{
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(fragmentContext, "${it.exception!!.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }

        builder.show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

}