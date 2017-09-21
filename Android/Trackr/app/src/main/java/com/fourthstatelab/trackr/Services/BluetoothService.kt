package com.fourthstatelab.trackr.Services

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import com.fourthstatelab.trackr.Data
import com.fourthstatelab.trackr.Models.Device
import com.fourthstatelab.trackr.Models.Location
import com.fourthstatelab.trackr.Utils.HttpRequest
import com.fourthstatelab.trackr.Utils.LocationService
import com.fourthstatelab.trackr.Utils.Preference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BluetoothService : Service() {

    private val BTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var foundDevices: ArrayList<Device> = ArrayList()
    private var notDiscovered: ArrayList<Device> = ArrayList(Data.myDevices)
    private var lostDevices: ArrayList<Device> = ArrayList()
    private lateinit var globalLocation: Location
    override fun onCreate() {
        super.onCreate()
        Log.d("Services", "Created")
        if(readDataSuccessful()) {
            LocationService.getLocationFromManager(applicationContext, object : LocationService.LocationChangedListener {
                override fun onLocationChanged(location: Location) {
                    initBluetooth()
                    Log.d("Service LOCATION UPDATE", Gson().toJson(location))
                    globalLocation = location
                    //if(!BTAdapter.isDiscovering) setLost(notDiscovered.size-1)
                }
            })
        }
    }

    fun readDataSuccessful(): Boolean {
        val json: String = Preference.get(applicationContext, Preference.MY_DEVICES, "")
        Log.d("Preferences",json)
        return if (!json.isEmpty()) {
            notDiscovered = Gson().fromJson(json, object : TypeToken<ArrayList<Device>>() {}.type)
            true
        } else {
            false
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    private fun initBluetooth() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(receiver, intentFilter)
        if (!BTAdapter.isEnabled) {
            BTAdapter.enable()
        } else {
            BTAdapter.startDiscovery()
        }
    }

    override fun onDestroy() {
        Log.d("Services", "Destroyed")
        super.onDestroy()
    }

    fun checkNotLost(address: String) {
        var found = false
        for (device: Device in notDiscovered) {
            if (device.deviceAddress == address) {
                device.isLost = false
                foundDevices.add(device)
                notDiscovered.remove(device)
                Log.d("Service BLUE DEV FOUND", device.deviceName + " " + device.deviceAddress)
                //listAdapter.notifyDataSetChanged()
                setNotLostCont(device)
                found=true
                break
            }
        }
        for(lostDevice: Device in lostDevices){
            if(lostDevice.deviceAddress == address){
                setNotLostCont(lostDevice)
                lostDevices.remove(lostDevice)
                 Preference.put(applicationContext,Preference.LOST_DEVICES,lostDevices)
                found=true
                return
            }
        }
        if(!found)checkOthersAndUpdate(address)
    }

    fun checkOthersAndUpdate(device : String){
        HttpRequest("/device_lost",HttpRequest.Method.GET)
                .addParam("lat",globalLocation.lat.toString())
                .addParam("lon",globalLocation.lon.toString())
                .addParam("mac",device)
                .sendRequest(object : HttpRequest.OnResponseListener{
                    override fun OnResponse(response: String?) {
                        if(response!=null){
                            Log.d("Service HTTP SET LOST",response+" "+device)
                        }
                    }

                })
    }

    fun endDiscovery() {
        onAttemptComplete()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    fun onAttemptComplete() {
        //setNotLost(foundDevices.size-1)
        for (device: Device in notDiscovered) {
            device.isLost = true
            foundDevices.add(device)
            lostDevices.add(device)
            Preference.put(applicationContext,Preference.LOST_DEVICES,lostDevices)
        }
        //listAdapter.notifyDataSetChanged()
        if (globalLocation != null) setLost(notDiscovered.size - 1)
        Log.d("Service BLUE N FOUND", (Gson()).toJson(notDiscovered))
        notDiscovered = ArrayList(foundDevices)
        foundDevices = ArrayList()
        BTAdapter.startDiscovery()
    }

    fun setLost(index : Int) {
        if (index < 0) {
            return
        } else {
            val device: Device = notDiscovered[index]
            HttpRequest("/device_lost", HttpRequest.Method.GET)
                    .addParam("lat", globalLocation.lat.toString())
                    .addParam("lon", globalLocation.lon.toString())
                    .addParam("mac", device.deviceAddress)
                    .sendRequest(object : HttpRequest.OnResponseListener {
                        override fun OnResponse(response: String?) {
                            if (response != null) {
                                Log.d("Service HTTP SET LOST", response+" Device"+device.customName)
                            }
                            setLost(index - 1)
                        }

                    })
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when (action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    if (BTAdapter.isEnabled) {
                        BTAdapter.startDiscovery()
                    }
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    checkNotLost(device.address)
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("Service Bluetooth", "STARTED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("Service Bluetooth", "Finished")
                    endDiscovery()

                }
            }
        }
    }
    fun setNotLostCont(device : Device){
        HttpRequest("/device_found", HttpRequest.Method.GET)
                .addParam("mac",device.deviceAddress)
                .sendRequest(object : HttpRequest.OnResponseListener{
                    override fun OnResponse(response: String?) {
                        if(response!=null){
                            Log.d("Service HTTP SET N LOST",response+device.customName)
                        }
                    }
                })
    }
}
