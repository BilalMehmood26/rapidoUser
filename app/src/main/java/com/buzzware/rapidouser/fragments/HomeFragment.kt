package com.buzzware.rapidouser.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.buzzware.rapidouser.Model.Attachment
import com.buzzware.rapidouser.Model.Pharmacy
import com.buzzware.rapidouser.Model.User
import com.buzzware.rapidouser.R
import com.buzzware.rapidouser.constants.FragmentListener
import com.buzzware.rapidouser.databinding.FragmentHomeBinding
import com.buzzware.rapidouser.utils.UserSession
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    private lateinit var addressesDialogFragment: AddressesDialogFragment
    private lateinit var fragmentContext: Context

    private val pharmacyNameList: ArrayList<String> = arrayListOf()
    private val pharmacyList: ArrayList<Pharmacy> = arrayListOf()
    private lateinit var pharmacy: Pharmacy

    private var date: Long = 0
    private var time: Long = 0

    private lateinit var latLong: LatLng

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)

        setView()
        setListener()
        getPharmacyList()

        return binding.root
    }

    private fun setView() {
        binding.apply {

            fullNameEt.setText("${UserSession.user.firstName} ${UserSession.user.lastName}")
            emailEt.setText(UserSession.user.email)
            phoneEt.setText(UserSession.user.phoneNumber)
            dateEt.setText(getCurrentDate())
            timeEt.setText(getTimeDate())

            addressTv.setOnClickListener {
                addressesDialogFragment =
                    AddressesDialogFragment { address, city, postalCode, latlng ->
                        addressTv.setText(address)
                        cityEt.setText(city)
                        postalEt.setText(postalCode)
                        latLong = latlng
                    }
                addressesDialogFragment.show(childFragmentManager, "Pickup Location")
            }

        }
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(android.icu.util.Calendar.HOUR_OF_DAY, hour)
            cal.set(android.icu.util.Calendar.MINUTE, minute)
            time = cal.timeInMillis
            binding.timeEt.setText(SimpleDateFormat("hh:mm a").format(cal.time))
        }

        TimePickerDialog(
            fragmentContext,
            timeSetListener,
            cal.get(android.icu.util.Calendar.HOUR_OF_DAY),
            cal.get(android.icu.util.Calendar.MINUTE),
            true
        ).show()
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        date = System.currentTimeMillis()
        return sdf.format(Date())
    }

    private fun getTimeDate(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        time = System.currentTimeMillis()
        return sdf.format(Date())
    }

    private fun setListener() {

        binding.orderNowTV.setOnClickListener {
            val fullName = binding.fullNameEt.text.toString().trim()
            val email = binding.emailEt.text.toString().trim()
            val phone = binding.phoneEt.text.toString().trim()
            val address = binding.addressTv.text.toString().trim()
            val city = binding.cityEt.text.toString().trim()
            val postCode = binding.postalEt.text.toString().trim()

            when {
                fullName.isEmpty() -> binding.fullNameEt.error = "Enter full name"
                email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> binding.emailEt.error = "Enter valid email"
                phone.isEmpty() || phone.length < 10 -> binding.phoneEt.error = "Enter valid phone number"
                address.isEmpty() -> showToast("Enter address")
                city.isEmpty() -> binding.cityEt.error = "Enter city"
                postCode.isEmpty() -> binding.postalEt.error = "Enter postal code"
                else -> {
                    orderNow(postCode, address, email, fullName, phone, pharmacy.id.toString(), pharmacy.pharmacyName, city, date, time, latLong)
                }
            }
        }

        binding.dateEt.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                fragmentContext,
                { _, selectedYear, selectedMonth, selectedDay ->

                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                    date = selectedCalendar.timeInMillis

                    val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    binding.dateEt.text = date

                },
                year, month, day
            )
            datePickerDialog.show()
        }

        binding.timeEt.setOnClickListener {
            showTimePicker()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(fragmentContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun getPharmacyList() {

        binding.progressBar.visibility = View.VISIBLE
        Firebase.firestore.collection("pharmacy").addSnapshotListener { value, error ->

            if (error != null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(fragmentContext, "${error.message}", Toast.LENGTH_SHORT).show()
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
        }
        getAttachment()
    }

    private fun getAttachment() {
        binding.progressBar.visibility = View.VISIBLE
        Firebase.firestore.collection("Attachment").addSnapshotListener { value, error ->

            if (error != null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(fragmentContext, "${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            value!!.forEach { queryDocumentSnapshot ->
                val attachment = queryDocumentSnapshot.toObject(Attachment::class.java)
                if (attachment.userID == UserSession.user.id) {
                    val pharmacyIndex =
                        pharmacyList.indexOfFirst { it.pharmacyName == attachment.pharmacyName }
                    if (pharmacyIndex != -1) {
                        binding.parajeID.selectItemByIndex(pharmacyIndex)
                    }
                }
                binding.progressBar.visibility = View.GONE
            }
        }
    }

        private fun orderNow(
        postCode: String,
        address: String,
        patientEmail: String,
        patientName: String,
        patientNumber: String,
        pharmacyID: String,
        pharmacyName: String,
        city: String,
        date: Long,
        time: Long,
        latLng: LatLng
    ) {
        val orderID = UUID.randomUUID().toString()
        val order = hashMapOf(
            "DeliverRequestDate" to System.currentTimeMillis(),
            "OrderID" to orderID,
            "PostCode" to postCode,
            "address" to address,
            "city" to city,
            "date" to date,
            "dob" to UserSession.user.dob,
            "latitude" to latLng.latitude,
            "latitude" to latLng.longitude,
            "patientEmail" to patientEmail,
            "patientID" to UserSession.user.id,
            "patientName" to patientName,
            "patientNumber" to patientNumber,
            "pharmacyID" to pharmacyID,
            "pharmacyName" to pharmacyName,
            "status" to "pending",
            "time" to time
        )
        binding.progressBar.visibility = View.VISIBLE
        Firebase.firestore.collection("OrderRequest").document(orderID).set(order)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    binding.progressBar.visibility = View.GONE
                    (activity as FragmentListener).loadFragment("history")
                    showToast("Order Created")
                } else {
                    binding.progressBar.visibility = View.GONE
                    showToast(it.exception!!.message.toString())
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

}