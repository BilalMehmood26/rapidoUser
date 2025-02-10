package com.buzzware.rapidouser.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.buzzware.rapidouser.R
import com.buzzware.rapidouser.constants.FragmentListener
import com.buzzware.rapidouser.databinding.ActivityDashBoardBinding
import com.buzzware.rapidouser.databinding.AlertScheduleLayoutBinding
import com.buzzware.rapidouser.fragments.*

class DashBoardActivity : AppCompatActivity(), FragmentListener{

    private lateinit var binding : ActivityDashBoardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setView()
        setListener()

    }

    private fun setView() {
        setHomeTab()

    }

    private fun setListener() {

        binding.homeTab.setOnClickListener {
            setHomeTab()
        }

        binding.historyTab.setOnClickListener {
            setHistoryTab()
        }

        binding.profileTab.setOnClickListener {
            setProfileTab()
        }

        binding.appBar.backIV.setOnClickListener {
            onBackPressed()
        }
    }


    private fun setHomeTab() {
        binding.homeTab.setImageResource(R.drawable.icon_home_selected)
        binding.historyTab.setImageResource(R.drawable.icon_history_unselected)
        binding.profileTab.setImageResource(R.drawable.icon_profile_unselected)
        binding.appBar.root.visibility = View.VISIBLE
        binding.appBar.backIV.visibility = View.INVISIBLE
        binding.appBar.titleTV.text = "Rapido-rx"
        loadFragment(HomeFragment())
    }

    private fun setHistoryTab() {
        binding.homeTab.setImageResource(R.drawable.icon_home_unselected)
        binding.historyTab.setImageResource(R.drawable.icon_history_selected)
        binding.profileTab.setImageResource(R.drawable.icon_profile_unselected)
        binding.appBar.titleTV.text = "Delivery History"
        binding.appBar.root.visibility = View.VISIBLE
        binding.appBar.backIV.visibility = View.INVISIBLE
        loadFragment(HistoryFragment())
    }

    private fun setProfileTab() {
        binding.homeTab.setImageResource(R.drawable.icon_home_unselected)
        binding.historyTab.setImageResource(R.drawable.icon_history_unselected)
        binding.profileTab.setImageResource(R.drawable.icon_profile_selected)
        binding.appBar.root.visibility = View.GONE
        loadFragment(ProfileFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(binding.container.id, fragment)
        transaction.commit()

        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

    }

    override fun loadFragment(value: String) {
        when (value) {
            "homeOrderNow" -> {
                binding.appBar.titleTV.text = "Payment"
                binding.appBar.root.visibility = View.VISIBLE
                binding.appBar.backIV.visibility = View.VISIBLE
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(binding.container.id, PaymentFragment())
                transaction.addToBackStack(null)
                transaction.commit()
            }

            "history" -> {
                setHistoryTab()
            }

            "homeSchedule" -> {
                showDialogue()
            }
            "profileAccount" -> {
                binding.appBar.titleTV.text = "Account"
                binding.appBar.root.visibility = View.VISIBLE
                binding.appBar.backIV.visibility = View.VISIBLE
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(binding.container.id, AccountFragment())
                transaction.addToBackStack(null)
                transaction.commit()
            }
            "profileNotification" -> {
                binding.appBar.titleTV.text = "Notifications"
                binding.appBar.root.visibility = View.VISIBLE
                binding.appBar.backIV.visibility = View.VISIBLE
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(binding.container.id, NotificationFragment())
                transaction.addToBackStack(null)
                transaction.commit()
            }

            "profileLogout" -> {
                finish()
            }
        }

    }

    private fun showDialogue() {

        val dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = AlertScheduleLayoutBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.orderNowTV.setOnClickListener {
            dialog.dismiss()
            binding.appBar.titleTV.text = "Payment"
            binding.appBar.root.visibility = View.VISIBLE
            binding.appBar.backIV.visibility = View.VISIBLE
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(binding.container.id, PaymentFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        dialog.show()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        checkInstance()
    }

    private fun checkInstance(){
        when (supportFragmentManager.findFragmentById(binding.container.id)){

            is HomeFragment ->{
                setHomeTab()
            }

            is PaymentFragment ->{
                binding.appBar.root.visibility = View.VISIBLE
                binding.appBar.backIV.visibility = View.VISIBLE
            }

            is HistoryFragment ->{
                setHistoryTab()
            }

            is ProfileFragment ->{
                setProfileTab()
            }

            is AccountFragment ->{
                binding.appBar.root.visibility = View.VISIBLE
                binding.appBar.backIV.visibility = View.VISIBLE
            }

            is NotificationFragment ->{
                binding.appBar.root.visibility = View.VISIBLE
                binding.appBar.backIV.visibility = View.VISIBLE
            }

        }
    }
}