package com.fourthstatelab.trackr.Utils

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.fourthstatelab.trackr.Models.Location
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.gson.Gson


object LocationService {

    private var googleApiClient : GoogleApiClient? =null

    private val LAST_LOCATION=1000
    private val CURRENT_LOCATION = 2000
    interface LocationChangedListener{
        fun onLocationChanged(location : Location)
    }

    private fun location(locationChangedListener: LocationChangedListener){
        val locationProvider : FusedLocationProviderApi= LocationServices.FusedLocationApi
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 6 *1000
        locationRequest.fastestInterval = 6*1000
        val listener : LocationListener = object : LocationListener{
            override fun onLocationChanged(location: android.location.Location?) {
                locationChangedListener.onLocationChanged(Location(location!!.latitude,location.longitude,location.accuracy))
                //locationProvider.removeLocationUpdates(googleApiClient,this)
            }

        }

        locationProvider.requestLocationUpdates(googleApiClient,locationRequest,listener)
        val location = locationProvider.getLastLocation(googleApiClient)
        Log.d("LOCATION LOCATION", Gson().toJson(location))
    }

    fun getLastLocation(context : Context) : Location{
        if(googleApiClient==null){
            googleApiClient = GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks{
                        override fun onConnected(p0: Bundle?) {
                            Log.d("LOCATION CLIENT","Connected")
                            getLastLocation(context)
                        }

                        override fun onConnectionSuspended(p0: Int) {
                            Log.d("LOCATION CLIENT","Suspended")
                        }

                    })
                    .addOnConnectionFailedListener {
                        Log.d("LOCATION CLIENT","Failed")
                    }
                    .build()

            googleApiClient!!.connect()
        }
        val locationProvider : FusedLocationProviderApi= LocationServices.FusedLocationApi
        val location = locationProvider.getLastLocation(googleApiClient)
        return Location(location.latitude,location.longitude,location.accuracy)
    }

    fun getLocationFromManager(context: Context,locationChangedListener: LocationChangedListener){
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener : android.location.LocationListener = object : android.location.LocationListener{
            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

            }

            override fun onProviderEnabled(p0: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onProviderDisabled(p0: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onLocationChanged(p0: android.location.Location) {
                locationChangedListener.onLocationChanged(Location(p0.latitude,p0.longitude,p0.accuracy))
                locationManager.removeUpdates(this)
            }
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,6,10f,listener)
    }

    fun getLocation(context:Context,locationChangedListener: LocationChangedListener){
        if(googleApiClient==null){
            googleApiClient = GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks{
                        override fun onConnected(p0: Bundle?) {
                            Log.d("LOCATION CLIENT","Connected")
                            location(locationChangedListener)
                        }

                        override fun onConnectionSuspended(p0: Int) {
                            Log.d("LOCATION CLIENT","Suspended")
                        }

                    })
                    .addOnConnectionFailedListener {
                        Log.d("LOCATION CLIENT","Failed")
                    }
                    .build()

            googleApiClient!!.connect()
        }
        else{
            location(locationChangedListener)
        }
    }
}