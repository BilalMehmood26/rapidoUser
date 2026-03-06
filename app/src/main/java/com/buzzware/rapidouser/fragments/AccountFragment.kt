package com.buzzware.rapidouser.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.buzzware.rapidouser.databinding.FragmentAccountBinding
import com.buzzware.rapidouser.utils.UserSession
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class AccountFragment : Fragment() {

    private lateinit var binding : FragmentAccountBinding
    private lateinit var fragmentContext: Context

    private var imageURI: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAccountBinding.inflate(layoutInflater)

        setView()

        return binding.root
    }

    private fun setView() {
        binding.apply {
            firstNameEt.setText(UserSession.user.firstName)
            lastNameEt.setText(UserSession.user.lastName)
            emailEt.setText(UserSession.user.email)
            passwordEt.setText(UserSession.user.password)
            phoneEt.setText(UserSession.user.phoneNumber)
            if (UserSession.user.image.isNotEmpty()) {
                Glide.with(fragmentContext).load(UserSession.user.image).into(userProfileIV)
            }

            userProfileIV.setOnClickListener {
                openGalleryOrFilePicker()
            }

            saveBtn.setOnClickListener {
                saveInfo(
                    firstNameEt.text.toString(),
                    lastNameEt.text.toString(),
                    passwordEt.text.toString(),
                    emailEt.text.toString(),
                    phoneEt.text.toString()
                )
            }
        }
    }

    private fun openGalleryOrFilePicker() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
    }

    private fun saveInfo(
        firstName: String,
        lastName: String,
        password: String,
        email: String,
        phone: String
    ) {

        binding.progressBar.visibility = View.VISIBLE
        if (imageURI != null) {
            val storage =
                FirebaseStorage.getInstance().reference.child("Users/${UserSession.user.id}.jpg")

            var uploadTask = storage.putFile(imageURI!!)
            uploadTask.addOnSuccessListener {
                storage.downloadUrl.addOnSuccessListener {
                    val userUpdate = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "password" to password,
                        "email" to email,
                        "phoneNumber" to phone,
                        "image" to it.toString()
                    ) as Map<String, Any>
                    UserSession.user.image = it.toString()

                    Firebase.firestore.collection("Users").document(UserSession.user.id).update(userUpdate)
                        .addOnSuccessListener {
                            UserSession.user.firstName = firstName
                            UserSession.user.lastName = lastName
                            UserSession.user.email = email
                            UserSession.user.phoneNumber = phone
                            UserSession.user.password = password

                            if(password.isEmpty()){
                                updatePassword(password)
                            }else{
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(fragmentContext, "Profile Updated!", Toast.LENGTH_SHORT).show()
                            }

                        }.addOnFailureListener {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                fragmentContext,
                                it.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }.addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(fragmentContext, it.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            val userUpdate = hashMapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "password" to password,
                "email" to email,
                "phoneNumber" to phone
            ) as Map<String, Any>
            Firebase.firestore.collection("Users").document(UserSession.user.id).update(userUpdate)
                .addOnSuccessListener {
                    UserSession.user.firstName = firstName
                    UserSession.user.lastName = lastName
                    UserSession.user.email = email
                    UserSession.user.phoneNumber = phone
                    UserSession.user.password = password
                    binding.progressBar.visibility = View.GONE
                    if(password.isNotEmpty()){
                        updatePassword(password)
                    }else{
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(fragmentContext, "Profile Updated", Toast.LENGTH_SHORT)
                            .show()
                    }
                }.addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        fragmentContext,
                        it.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

    }

    private fun updatePassword(password: String) {
        binding.progressBar.visibility = View.VISIBLE
        val credential =
            EmailAuthProvider.getCredential(UserSession.user.email!!, UserSession.user.password!!)
        Firebase.auth.currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    Firebase.auth.currentUser!!.updatePassword(password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(fragmentContext, "Profile Updated!", Toast.LENGTH_SHORT).show()
                                Log.d("LOGGER!", "Password updated successfully.")
                            } else {
                                binding.progressBar.visibility = View.GONE
                                Log.e(
                                    "LOGGER!",
                                    "Error updating password: ${task.exception?.message}"
                                )
                                Toast.makeText(
                                    fragmentContext,
                                    "${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        fragmentContext,
                        "${reauthTask.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("LOGGER!", "Re-authentication failed: ${reauthTask.exception?.message}")
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }
}