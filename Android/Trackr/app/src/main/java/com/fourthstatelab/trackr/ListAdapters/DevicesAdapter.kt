package com.fourthstatelab.trackr.ListAdapters
import android.app.Activity
import android.app.FragmentManager
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.fourthstatelab.trackr.Activities.Dashboard
import com.fourthstatelab.trackr.Models.Device
import com.fourthstatelab.trackr.Models.Location
import com.fourthstatelab.trackr.R
import com.fourthstatelab.trackr.Utils.HttpRequest
import com.fourthstatelab.trackr.Utils.LocationService
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by sid on 8/9/17.
 */
class DevicesAdapter(var context : Context,var list : ArrayList<Device>, var clickListener:Dashboard.OnBluetoothClickedListener) : BaseAdapter()/*, OnMapReadyCallback*/ {
    /*override fun onMapReady(p0: GoogleMap?) {
        LocationService.getLocationFromManager(context,object : LocationService.LocationChangedListener{
            override fun onLocationChanged(location: Location) {
                var loc1 : android.location.Location  = android.location.Location("")
                loc1.latitude = location.lat
                loc1.longitude = location.lon
                HttpRequest("/getdeviceinfo",HttpRequest.Method.GET)
                        .addParam("t1","distnace")
                        .addParam("t2","time")
                        .sendRequest(object : HttpRequest.OnResponseListener{
                            override fun OnResponse(response: String?) {


                            }

            })
            }
    })
    }*/

/*HttpRequest("/machine_learning",HttpRequest.Method.GET)
.addParam("t1","distnace")
.addParam("t2","time")
.sendRequest(object : HttpRequest.OnResponseListener{
    override fun OnResponse(response: String?) {


    }*/

    override fun getView(position: Int, p1: View?, p2: ViewGroup?): View {
        val device : Device = list[position]
        val view = View.inflate(context, R.layout.item_device,null)
        val nameView = view.findViewById<TextView>(R.id.device_name)
        val iv_error= view.findViewById<ImageView>(R.id.iv_not_found)

        nameView.text = device.customName
        iv_error.visibility = if(device.isLost){
            View.VISIBLE
        }
        else{
            View.GONE
        }

        view.setOnClickListener(object  : View.OnClickListener {
            override fun onClick(p0: View?) {
                clickListener.onBLuetoothClicked(list[position])
                                /*var activity: FragmentActivity = context as FragmentActivity
                val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.detail_map) as SupportMapFragment
                mapFragment.getMapAsync(this@DevicesAdapter)*/
                HttpRequest("/getdeviceinfo", HttpRequest.Method.GET)
                        .addParam("id", device.deviceAddress)
                        .sendRequest(object : HttpRequest.OnResponseListener {
                            override fun OnResponse(response: String?) {
                                if(response!=null){
                                    Log.d("MACHINE",response)
                                    var loc1 : Location = Gson().fromJson(response,object: TypeToken<Location>(){}.type)
                                    LocationService.getLocationFromManager(context,object : LocationService.LocationChangedListener{
                                        override fun onLocationChanged(location: Location) {
                                            Log.d("MACHINE",Gson().toJson(location))
                                            var loc2 : android.location.Location  = android.location.Location("")
                                            loc2.latitude = location.lat
                                            loc2.longitude = location.lon
                                            val R = 6371 // km
                                            val x = (location.lon - loc1.lon) * Math.cos((loc1.lat + loc2.latitude) / 2)
                                            val y = loc2.latitude - loc1.lat
                                            val distance = Math.sqrt(x * x + y * y) * R
                                            HttpRequest("/machine_learning",HttpRequest.Method.GET)
                                                    .addParam("t1",distance.toString())
                                                    .addParam("t2","10")
                                                    .sendRequest(object : HttpRequest.OnResponseListener{
                                                        override fun OnResponse(response: String?) {
                                                            if(response!=null){
                                                                Log.d("DEVICE PROB",response)
                                                                if(response=="1"){
                                                                    Toast.makeText(context,"There is a fair chance of finding your device\naccording to our machine learning algorithm",Toast.LENGTH_LONG).show()
                                                                }
                                                                else{
                                                                    Toast.makeText(context,"There is a high chance that you will not\n find your device\naccording to our machine learning algorithm",Toast.LENGTH_LONG).show()
                                                                }
                                                            }
                                                        }

                                                    })
                                        }
                                    })
                                }
                                else{
                                    Log.d("DEVICE INFO","NULL")
                                }
                            }

                        })
            }
        })
        return view
    }

    override fun getItem(p0: Int): Any {
        return list[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }
}