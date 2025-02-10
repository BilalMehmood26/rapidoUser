package com.buzzware.rapidouser.adapter

import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.buzzware.rapidouser.Model.Order
import com.buzzware.rapidouser.databinding.ItemDesignNewDeliveryLayoutBinding
import com.buzzware.rapidouser.utils.UserSession
import com.buzzware.rapidouser.utils.convertDateTimestamp
import com.buzzware.rapidouser.utils.convertTimeTimestamp
import com.buzzware.rapidouser.utils.openPhoneDialer


class PendingDeliveryAdapter(val context : Context, val list: ArrayList<Order>) :
    RecyclerView.Adapter<PendingDeliveryAdapter.ViewHolder>()  {

    inner class ViewHolder(val binding : ItemDesignNewDeliveryLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return  ViewHolder(ItemDesignNewDeliveryLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
       return list.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.statusTV.text = "Pending"
         val item  = list[position]

        holder.binding.apply {

            if(UserSession.user.image.isNotEmpty()){
                Glide.with(context).load(UserSession.user.image).into(userProfileIV)
            }

            nameTv.text = item.patientName
            pharmacyTv.text = item.pharmacyName
            dateTimeTv.text = "${convertDateTimestamp(item.date)} ${convertTimeTimestamp(item.time)} hrs"
            addressTv.text = item.address
            phoneTv.text = item.patientNumber
            emailTv.text = item.patientEmail
            phoneTv.paintFlags = phoneTv.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG

            phoneTv.setOnClickListener{
                openPhoneDialer(item.patientNumber,context)
            }
        }
    }

}