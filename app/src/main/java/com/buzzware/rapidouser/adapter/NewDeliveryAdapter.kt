package com.buzzware.rapidouser.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.buzzware.rapidouser.Model.Order
import com.buzzware.rapidouser.Model.User
import com.buzzware.rapidouser.activities.DashBoardActivity
import com.buzzware.rapidouser.databinding.ItemDesignNewDeliveryLayoutBinding
import com.buzzware.rapidouser.utils.UserSession
import com.buzzware.rapidouser.utils.convertDateTimestamp
import com.buzzware.rapidouser.utils.convertTimeTimestamp
import com.buzzware.rapidouser.utils.openPhoneDialer
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NewDeliveryAdapter(val context: Context, val list: ArrayList<Order>) :
    RecyclerView.Adapter<NewDeliveryAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewDeliveryAdapter.ViewHolder {
        return ViewHolder(
            ItemDesignNewDeliveryLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: NewDeliveryAdapter.ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.apply {

            if(UserSession.user.image.isNotEmpty()){
                Glide.with(context).load(UserSession.user.image).into(userProfileIV)
            }

            if(item.status.equals("Payment Approval")){
                payNowTv.visibility = View.VISIBLE
            }else{
                payNowTv.visibility = View.GONE
            }

            payNowTv.setOnClickListener{
                Firebase.firestore.collection("OrderRequest").document(item.OrderID).update("status","Paid")
            }

            statusTV.text = item.status
            nameTv.text = item.patientName
            pharmacyTv.text = item.pharmacyName
            dateTimeTv.text = "${convertDateTimestamp(item.date)} ${convertTimeTimestamp(item.time)}"
            paymentStatusTv.visibility = View.VISIBLE
            paymentStatusTv.text = item.modeofpayment
            addressTv.text = item.address
            phoneTv.text = item.patientNumber
            emailTv.text = item.patientEmail
            phoneTv.paintFlags = phoneTv.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG

            if (item.driverid.isNotEmpty()) {
                Firebase.firestore.collection("Users").document(item.driverid).get()
                    .addOnSuccessListener { response ->
                        if (response.exists()) {
                            val user = response.toObject(User::class.java)
                            driverNameTv.text = "${user?.firstName} ${user?.lastName}"
                            if(user!!.image.isNotEmpty()){
                                Glide.with(context).load(user.image).into(driverProfileIV)
                            }
                        }
                    }
            }

            phoneTv.setOnClickListener {
                openPhoneDialer(item.patientNumber, context)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val binding: ItemDesignNewDeliveryLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}