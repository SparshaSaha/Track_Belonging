package com.fourthstatelab.trackr.Activities

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import com.fourthstatelab.trackr.ListAdapters.NewDeviceAdapter

import com.fourthstatelab.trackr.R

//TODO CHECK IF THE DEVICE IS A TRACKR DEVICE
//TODO ADD THE NAME OF THE DEVICE
//TODO REGISTER THE DEVICE ON THE INTERNET

class AddDevice : AppCompatActivity() {
    var BTAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    var newDevices : ArrayList<BluetoothDevice> = ArrayList()
    lateinit var newDeviceAdapter :  NewDeviceAdapter
    lateinit var newDeviceList : ListView
    lateinit var progressRing : ProgressBar
    var intentAction : String ="fromSignin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device)
        intentAction = intent.getStringExtra("intentAction")
        initLayout()
        initBluetooth()

    }

    private fun initLayout(){
        newDeviceAdapter = NewDeviceAdapter(this@AddDevice,newDevices, object : OnBluetoothAdddedListener{
            override fun onBLuetoothAdded() {
                if(intentAction=="fromSignin"){
                    startActivity(Intent(this@AddDevice,Dashboard::class.java))
                }
                else if(intentAction=="fromDashboard"){
                    finish()
                }
                unregisterReceiver(receiver)
            }

        })
        newDeviceList = findViewById(R.id.new_device_list) as ListView
        progressRing = findViewById(R.id.new_device_progress) as ProgressBar
        newDeviceList.adapter = newDeviceAdapter
    }
    interface OnBluetoothAdddedListener{
        fun onBLuetoothAdded()
    }

    private fun initBluetooth(){
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(receiver,intentFilter)
        if(!BTAdapter.isEnabled){
            BTAdapter.enable()
        }
        else{
            BTAdapter.startDiscovery()
        }
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent) {
            val action = intent.action
            when(action){
                BluetoothAdapter.ACTION_STATE_CHANGED->{
                    if(BTAdapter.isEnabled){
                        BTAdapter.startDiscovery()
                    }
                }
                BluetoothDevice.ACTION_FOUND->{
                    val device : BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if(device !in newDevices){
                        progressRing.visibility = View.GONE
                        newDevices.add(device)
                        newDeviceAdapter.notifyDataSetChanged()
                        Log.d("Bluetooth Found","Name=> "+device.name+" Address=> "+device.address)
                    }
                    else{
                        Log.d("Bluetooth Found","Redundant Device")
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED->{
                    Log.d("Bluetooth Adapter","STARTED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED->{
                    Log.d("Bluetooth Adapter","Finished")
                    BTAdapter.startDiscovery()
                }
            }
        }
    }
}
