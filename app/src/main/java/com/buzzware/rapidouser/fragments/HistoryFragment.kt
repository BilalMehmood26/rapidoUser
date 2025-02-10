package com.buzzware.rapidouser.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.buzzware.rapidouser.Model.Order
import com.buzzware.rapidouser.Model.Pharmacy
import com.buzzware.rapidouser.Model.User
import com.buzzware.rapidouser.R
import com.buzzware.rapidouser.adapter.DeliveredAdapter
import com.buzzware.rapidouser.adapter.NewDeliveryAdapter
import com.buzzware.rapidouser.adapter.PendingDeliveryAdapter
import com.buzzware.rapidouser.databinding.FragmentHistoryBinding
import com.buzzware.rapidouser.utils.UserSession
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryFragment : Fragment() {

    private lateinit var binding : FragmentHistoryBinding
    private lateinit var fragmentContext: Context

    private val pendingOrderList : ArrayList<Order> = arrayListOf()
    private val newOrderList : ArrayList<Order> = arrayListOf()
    private val deliveredOrderList : ArrayList<Order> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHistoryBinding.inflate(layoutInflater)

        setListener()
        getOrders()

        return binding.root
    }

    private fun setListener() {

        binding.pendingTab.setOnClickListener {

            binding.pendingTab.setBackgroundColor(resources.getColor(R.color.dark_red_color))
            binding.pendingTab.setTextColor(resources.getColor(R.color.white))

            binding.newDeliveryTab.setBackgroundColor(Color.TRANSPARENT)
            binding.newDeliveryTab.setTextColor(resources.getColor(R.color.black))

            binding.alreadyDeliveredTab.setBackgroundColor(Color.TRANSPARENT)
            binding.alreadyDeliveredTab.setTextColor(resources.getColor(R.color.black))

            setPendingAdapter()


        }

        binding.newDeliveryTab.setOnClickListener {

            binding.newDeliveryTab.setBackgroundColor(resources.getColor(R.color.dark_red_color))
            binding.newDeliveryTab.setTextColor(resources.getColor(R.color.white))

            binding.pendingTab.setBackgroundColor(Color.TRANSPARENT)
            binding.pendingTab.setTextColor(resources.getColor(R.color.black))

            binding.alreadyDeliveredTab.setBackgroundColor(Color.TRANSPARENT)
            binding.alreadyDeliveredTab.setTextColor(resources.getColor(R.color.black))

            setNewDeliveryAdapter()


        }

        binding.alreadyDeliveredTab.setOnClickListener {

            binding.alreadyDeliveredTab.setBackgroundColor(resources.getColor(R.color.dark_red_color))
            binding.alreadyDeliveredTab.setTextColor(resources.getColor(R.color.white))

            binding.pendingTab.setBackgroundColor(Color.TRANSPARENT)
            binding.pendingTab.setTextColor(resources.getColor(R.color.black))

            binding.newDeliveryTab.setBackgroundColor(Color.TRANSPARENT)
            binding.newDeliveryTab.setTextColor(resources.getColor(R.color.black))

            setAlreadyDeliveredAdapter()


        }

    }

    private fun getOrders(){

        binding.progressBar.visibility = View.VISIBLE
        Firebase.firestore.collection("OrderRequest").addSnapshotListener { value, error ->

            if(error!=null){
                binding.progressBar.visibility = View.GONE
                Toast.makeText(fragmentContext, "${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            pendingOrderList.clear()
            newOrderList.clear()
            deliveredOrderList.clear()
            value!!.forEach {
                val order = it.toObject(Order::class.java)
                if(order.patientID.equals(UserSession.user.id)){
                    when(order.status){
                        "pending" ->  pendingOrderList.add(order)
                        "Active","Payment Approval","Paid" ->  newOrderList.add(order)
                        "Complete" ->  deliveredOrderList.add(order)
                    }
                }
            }
            binding.progressBar.visibility = View.GONE
        }
        setPendingAdapter()
    }

    private fun setNewDeliveryAdapter() {

        binding.recyclerView.layoutManager = LinearLayoutManager(fragmentContext)
        binding.recyclerView.adapter = NewDeliveryAdapter(fragmentContext, newOrderList)

    }

    private fun setAlreadyDeliveredAdapter() {

        binding.recyclerView.layoutManager = LinearLayoutManager(fragmentContext)
        binding.recyclerView.adapter = DeliveredAdapter(fragmentContext, deliveredOrderList)

    }

    private fun setPendingAdapter() {
        binding.recyclerView.layoutManager = LinearLayoutManager(fragmentContext)
        binding.recyclerView.adapter = PendingDeliveryAdapter(fragmentContext, pendingOrderList)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }
}