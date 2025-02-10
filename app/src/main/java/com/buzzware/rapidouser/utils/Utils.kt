package com.buzzware.rapidouser.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun convertDateTimestamp(milliseconds: Long): String {
    val instant = Instant.ofEpochMilli(milliseconds)
    val formatter = DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.getDefault())
    return formatter.format(instant.atZone(ZoneId.systemDefault()))
}

@RequiresApi(Build.VERSION_CODES.O)
fun convertTimeTimestamp(milliseconds: Long): String {
    val instant = Instant.ofEpochMilli(milliseconds)
    val formatter = DateTimeFormatter.ofPattern("hh:mma", Locale.getDefault())
    return formatter.format(instant.atZone(ZoneId.systemDefault()))
}

fun openPhoneDialer(driverPhoneNumber:String,context : Context){
    val dialIntent = Intent(Intent.ACTION_DIAL)
    dialIntent.data = Uri.parse("tel:$driverPhoneNumber")
    if (driverPhoneNumber.isNotEmpty()) {
        context.startActivity(dialIntent)
    } else {
        Toast.makeText(
            context,
            "Phone Number Not Available",
            Toast.LENGTH_SHORT
        ).show()
    }
}