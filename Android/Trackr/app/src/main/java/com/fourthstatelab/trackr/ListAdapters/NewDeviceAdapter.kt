package com.fourthstatelab.trackr.ListAdapters

import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import com.fourthstatelab.trackr.Activities.AddDevice
import com.fourthstatelab.trackr.Activities.Dashboard
import com.fourthstatelab.trackr.Data
import com.fourthstatelab.trackr.Models.Device
import com.fourthstatelab.trackr.R
import com.fourthstatelab.trackr.Utils.HttpRequest
import com.fourthstatelab.trackr.Utils.Preference
import org.w3c.dom.Text

/**
 * Created by sid on 9/2/17.
 */
class NewDeviceAdapter(val context : Context, var list: ArrayList<BluetoothDevice>, var addedListener : AddDevice.OnBluetoothAdddedListener)  : BaseAdapter() {

    override fun getView(position: Int, p1: View?, p2: ViewGroup?): View {
        val textView :TextView = View.inflate(context, R.layout.item_new_device,null) as TextView
        val bluetoothDevice : BluetoothDevice = list[position]
        var alertDialog: AlertDialog? = null
        textView.text = list[position].name
        textView.setOnClickListener({
            var alertDialogBuilder : AlertDialog.Builder = AlertDialog.Builder(context)
            val view = View.inflate(context,R.layout.floatingview_askname,null)
            val nameView = view.findViewById<TextView>(R.id.ask_device_name)
            val button = view.findViewById<TextView>(R.id.button_add_device)

            button.setOnClickListener(object : View.OnClickListener{
                override fun onClick(p0: View?) {
                    val deviceName = nameView.text.toString()
                    if(!deviceName.isEmpty()) {
                        Log.d("Bluetooth Tapped", "Name=> " + bluetoothDevice.name + " Address=> " + bluetoothDevice.address)
                        val device = Device(bluetoothDevice.name, bluetoothDevice.address, deviceName)
                        addDevice(bluetoothDevice.address,device)
                        alertDialog?.cancel()
                    }
                    else{
                        Toast.makeText(context,"Give a name to device",Toast.LENGTH_LONG).show()
                    }
                }

            })
            alertDialogBuilder.setView(view)
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()

        })
        return textView
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

    fun addDevice(mac  :String, device : Device){
        val progressDialog = ProgressDialog(context)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Adding device "+ device.customName)
        progressDialog.show()
        HttpRequest("/addevice",HttpRequest.Method.GET)
                .addParam("userid",Data.user.u_id.toString())
                .addParam("mac",mac)
                .sendRequest(object : HttpRequest.OnResponseListener{
                    override fun OnResponse(response: String?) {
                        if(response!=null){
                            Log.d("Response",response)
                            if(response=="1"){
                                Data.myDevices!!.add(device)
                                Preference.put(context,Preference.MY_DEVICES, Data.myDevices)
                                addedListener.onBLuetoothAdded()
                            }
                            else{
                                Toast.makeText(context,"Couldn't add device",Toast.LENGTH_SHORT).show()
                            }
                        }
                        else{
                            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG).show()
                        }
                        progressDialog.cancel()
                    }

                })

    }

}