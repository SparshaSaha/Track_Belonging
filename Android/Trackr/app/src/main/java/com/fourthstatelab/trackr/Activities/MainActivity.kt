package com.fourthstatelab.trackr.Activities

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

import com.fourthstatelab.trackr.R


class MainActivity : AppCompatActivity() {

    private val BTAdapter = BluetoothAdapter.getDefaultAdapter()
    lateinit var rssi_msg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rssi_msg = findViewById(R.id.textView1) as TextView
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        /*BTAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                rssi_msg.setText("\n"+rssi_msg.getText() + bluetoothDevice.getName() + " =>Address=>"+bluetoothDevice.getAddress());
            }
        });*/

        BTAdapter.startDiscovery()


        /*final Set<BluetoothDevice> devices = BTAdapter.getBondedDevices();
        for(final BluetoothDevice bluetoothDevice : devices){
            rssi_msg.setText("\n"+rssi_msg.getText() + bluetoothDevice.getName() + " =>Address=>"+bluetoothDevice.getAddress());
            bluetoothDevice.connectGatt(getApplicationContext(), false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    Log.d("CONNECTION of "+bluetoothDevice.getName(),"Changed Address: "+bluetoothDevice.getAddress());
                    gatt.readRemoteRssi();
                }

                @Override
                public void onReadRemoteRssi(final BluetoothGatt gatt, int rssi, int status) {
                    super.onReadRemoteRssi(gatt, rssi, status);
                    Log.d("DEVICE","Name: "+ bluetoothDevice.getName()+ " RSSi : "+String.valueOf(rssi));
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gatt.readRemoteRssi();
                        }
                    },200);
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    Log.d("DISCOVERED",gatt.getDevice().getAddress());
                }
            });
        }*/
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                addToTextView(intent, "FOUND")
            }
            else if (action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
                Log.d("Adapter", "Discovery Finished")
            }
        }
    }

    private fun addToTextView(intent: Intent, tag: String) {
        val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, java.lang.Short.MIN_VALUE).toInt()
        val name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME)
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        device.connectGatt(applicationContext, true, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                gatt.readRemoteRssi()
                super.onConnectionStateChange(gatt, status, newState)
            }

            override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
                Log.d(gatt.device.name, "" + rssi)
                gatt.readRemoteRssi()
                super.onReadRemoteRssi(gatt, rssi, status)
            }
        })
        rssi_msg.text = rssi_msg.text.toString() + tag + " " + name + " => " + rssi + "dBm, Address=> " + device.address
    }
}
