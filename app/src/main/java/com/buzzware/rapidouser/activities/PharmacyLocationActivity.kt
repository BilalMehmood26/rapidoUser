package com.buzzware.rapidouser.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.buzzware.rapidouser.Model.Pharmacy
import com.buzzware.rapidouser.databinding.ActivityPharmacyLocationBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class PharmacyLocationActivity : AppCompatActivity() {

    lateinit var binding: ActivityPharmacyLocationBinding

    private val pharmacyNameList: ArrayList<String> = arrayListOf()
    private val pharmacyList: ArrayList<Pharmacy> = arrayListOf()
    private lateinit var pharmacy: Pharmacy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPharmacyLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setView()
        setListener()
        getPharmacyList()

    }

    private fun setView() {

        binding.appBar.titleTV.text = "Sign Up"

    }

    private fun setListener() {

        binding.appBar.backIV.setOnClickListener {
            finish()
        }

        binding.apply {
            doneTV.setOnClickListener {
                binding.progressBar.visibility = View.VISIBLE

                val attachmentID = UUID.randomUUID().toString()
                val attachment = hashMapOf(
                    "id" to attachmentID,
                    "pharmacyName" to pharmacy.pharmacyName,
                    "pharmistID" to pharmacy.id,
                    "status" to "pending",
                    "time" to System.currentTimeMillis(),
                    "type" to "patient",
                    "userID" to Firebase.auth.currentUser!!.uid
                )
                Firebase.firestore.collection("pharmacy").document(attachmentID).set(attachment)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            binding.progressBar.visibility = View.GONE
                            val intent =
                                Intent(this@PharmacyLocationActivity, DashBoardActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(
                                androidx.appcompat.R.anim.abc_fade_in,
                                androidx.appcompat.R.anim.abc_fade_out
                            )
                        } else {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
            }

        }
    }

    private fun getPharmacyList() {

        binding.progressBar.visibility = View.VISIBLE
        Firebase.firestore.collection("pharmacy").addSnapshotListener { value, error ->

            if (error != null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            value!!.forEach {
                val pharmacy = it.toObject(Pharmacy::class.java)
                pharmacyNameList.add(pharmacy.pharmacyName)
                pharmacyList.add(pharmacy)
            }
            binding.progressBar.visibility = View.GONE
            setPharmacy()
        }
    }

    private fun setPharmacy() {
        binding.parajeID.setItems(pharmacyNameList)
        binding.parajeID.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newText ->
            pharmacy = pharmacyList[newIndex]
            binding.apply {
                pharmacyAddressET.setText(pharmacy.pharmacyAddress)
                phoneEt.setText(pharmacy.pharmacyphoneNo)
                if (pharmacy.pharmacycountrycode == "1") {
                    phoneCcp.setDefaultCountryUsingNameCode("US")
                    phoneCcp.resetToDefaultCountry()
                } else {
                    phoneCcp.setCountryForPhoneCode(pharmacy.pharmacycountrycode.toInt())
                }
            }
        }
    }
}