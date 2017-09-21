package com.fourthstatelab.trackr.Activities
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.View
import android.widget.ListView
import com.fourthstatelab.trackr.Data
import com.fourthstatelab.trackr.ListAdapters.DevicesAdapter
import com.fourthstatelab.trackr.Models.Device
import com.fourthstatelab.trackr.Models.Location

import com.fourthstatelab.trackr.R
import com.fourthstatelab.trackr.Services.BluetoothService
import com.fourthstatelab.trackr.Utils.HttpRequest
import com.fourthstatelab.trackr.Utils.LocationService
import com.fourthstatelab.trackr.Utils.Preference
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.gson.Gson
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng





class Dashboard : FragmentActivity(), OnMapReadyCallback {

    private val BTAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var foundDevices : ArrayList<Device>  = ArrayList()
    private var notDiscovered: ArrayList<Device> = ArrayList(Data.myDevices)
    private var lostDevices: ArrayList<Device> = ArrayList()
    private val maxAttempts = 1
    private var attempts: Int = 0
    private lateinit var list : ListView
    private lateinit var listAdapter : DevicesAdapter
    private lateinit var mMap: GoogleMap
    private var globalLocation : Location? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initLayout()
        initBluetooth()

        startService(Intent(this@Dashboard, BluetoothService::class.java))
    }

    interface OnBluetoothClickedListener{
        fun onBLuetoothClicked(device : Device)
    }

    private fun initLayout(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        listAdapter = DevicesAdapter(applicationContext,foundDevices,object : OnBluetoothClickedListener{
            override fun onBLuetoothClicked(device : Device) {

            }

        })
        list = findViewById(R.id.device_list)
        list.adapter = listAdapter
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        LocationService.getLocationFromManager(applicationContext,object : LocationService.LocationChangedListener{
            override fun onLocationChanged(location: Location) {
                val myLocation = LatLng(location.lat, location.lon)
                mMap.addMarker(MarkerOptions().position(myLocation).title("Your Location"))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17F))
                Log.d("LOCATION UPDATE",Gson().toJson(location))
                globalLocation = location
                //if(!BTAdapter.isDiscovering) setLost(notDiscovered.size-1)
            }
        })
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

    fun checkNotLost(address: String){
        for(device : Device in notDiscovered!!){
            if(device.deviceAddress == address){
                device.isLost=false
                foundDevices.add(device)
                notDiscovered.remove(device)
                Log.d("BLUETOOTH DEVICE FOUND",device.deviceName+" "+device.deviceAddress)
                listAdapter.notifyDataSetChanged()
                setNotLostCont(device)
                for(lostDevice: Device in lostDevices){
                    if(lostDevice.deviceAddress==device.deviceAddress){
                        lostDevices.remove(lostDevice)
                        Preference.put(applicationContext,Preference.LOST_DEVICES,lostDevices)
                        return
                    }
                }
                return
            }
        }
    }

    fun onAddClicked(view : View){
        unregisterReceiver(receiver)
        val intent = Intent(this@Dashboard,AddDevice::class.java)
        intent.putExtra("intentAction","fromDashboard")
        startActivityForResult(intent,1203)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        notDiscovered = ArrayList(Data.myDevices)
        foundDevices = ArrayList()
        initBluetooth()
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun endDiscovery(){
        attempts=0
        onAttemptComplete()
    }

    fun onAttemptComplete (){
        //setNotLost(foundDevices.size-1)
        for(device : Device in notDiscovered){
            device.isLost = true
            foundDevices.add(device)
            lostDevices.add(device)
            Preference.put(applicationContext,Preference.LOST_DEVICES,lostDevices)
        }
        listAdapter.notifyDataSetChanged()
        if(globalLocation!=null) setLost(notDiscovered.size-1)
        Log.d("BLUETOOTH N FOUND", (Gson()).toJson(notDiscovered))
        notDiscovered = ArrayList(foundDevices)
        foundDevices = ArrayList()
        BTAdapter.startDiscovery()
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context : Context?, intent: Intent?) {
            val action = intent?.action
            when(action){
                BluetoothAdapter.ACTION_STATE_CHANGED->{
                    if(BTAdapter.isEnabled){
                        BTAdapter.startDiscovery()
                    }
                }
                BluetoothDevice.ACTION_FOUND->{
                    val device : BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    checkNotLost(device.address)
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED->{
                    attempts++
                    Log.d("Bluetooth Adapter","STARTED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED->{
                    Log.d("Bluetooth Adapter","Finished")
                    endDiscovery()

                }
            }
        }
    }

    fun setLost(index : Int){
        if(index<0){
            return
        }
        else{
            val device : Device = notDiscovered[index]
            HttpRequest("/device_lost",HttpRequest.Method.GET)
                    .addParam("lat",globalLocation?.lat.toString())
                    .addParam("lon",globalLocation?.lon.toString())
                    .addParam("mac",device.deviceAddress)
                    .sendRequest(object : HttpRequest.OnResponseListener{
                        override fun OnResponse(response: String?) {
                            if(response!=null){
                                Log.d("HTTP SET LOST",response)
                            }
                            setLost(index-1)
                        }

                    })
        }
    }

    fun setNotLost(index : Int){
        if(index>=0){
            val device : Device = foundDevices[index]
            HttpRequest("/device_found",HttpRequest.Method.GET)
                    .addParam("mac",device.deviceAddress)
                    .sendRequest(object : HttpRequest.OnResponseListener{
                        override fun OnResponse(response: String?) {
                            if(response!=null){
                                Log.d("HTTP SET NOT LOST",response)
                            }
                            setNotLost(index-1)
                        }

                    })
        }
    }

    fun setNotLostCont(device : Device){
        HttpRequest("/device_found",HttpRequest.Method.GET)
                .addParam("mac",device.deviceAddress)
                .sendRequest(object : HttpRequest.OnResponseListener{
                    override fun OnResponse(response: String?) {
                        if(response!=null){
                            Log.d("HTTP SET NOT LOST",response)
                        }
                    }
                })
    }
}
