package tech.elecholic.mecarun

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.lang.Exception
import java.lang.NullPointerException
import java.lang.RuntimeException
import java.util.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 1
    private var mmServerSocket: BluetoothServerSocket? = null
    private val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            ?: throw NullPointerException("Device does not support Bluetooth")
    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private val MY_UUID = UUID.fromString("f710e010-b190-4d24-a47b-eb7b100bab39")
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Rotate mecanum
        val lowAnimation = AnimationUtils.loadAnimation(this, R.anim.low_speed_rotation)
        lowAnimation.interpolator = LinearInterpolator()
        mecanum_icon.startAnimation(lowAnimation)
    }

    /**
     * Search devices
     */
    fun onClickSearch(v: View) {
        if (mBluetoothAdapter.isDiscovering) {
            mBluetoothAdapter.cancelDiscovery()
        }
        mBluetoothAdapter.startDiscovery()
    }

    /**
     * Connection
     */
    fun startConnection(mmDevice: BluetoothDevice) {
        // Start a thread for connection
        mBluetoothAdapter.cancelDiscovery()
        var bluetoothSocket: BluetoothSocket? = null
        try {
            bluetoothSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID)
        } catch (e: Exception) {
            Log.i(TAG, "Fetch socket error: ${e.message}")
        }
        Thread.sleep(500)
        val connectThread = Thread {
            try {
                bluetoothSocket!!.connect()
            } catch (e: Exception) {
                // Unable to connect, try to close the socket and get out
                Log.i(TAG, "Bluetooth connection to server exception: ${e.message}")
                try {
                    bluetoothSocket!!.close()
                } catch (e: Exception) {
                }
                return@Thread
            }
            manageConnectedSocket(bluetoothSocket!!)
        }
        connectThread.start()
    }

    /**
     * Process for bluetooth successfully connect to server
     */
    private fun manageConnectedSocket(socket: BluetoothSocket) {
        val mBluetoothSocketHandler = BluetoothSocketHandler.get()
        mBluetoothSocketHandler.setSocket(socket)
        val intent = Intent(this, ControlActivity::class.java)
        startActivity(intent)
        Log.i(TAG, "Successfully connect to server")
    }

    /**
     * Initialize broadcast receiver
     */
    private val mBluetoothReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.i(TAG, "Action: $action")
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val scanDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (scanDevice == null || scanDevice.name == null) return
                    Log.i(TAG, "name=" + scanDevice.name + "address=" + scanDevice.address)
                    var name = scanDevice.name
                    val address = scanDevice.address
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.i(TAG, "Start scanning")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i(TAG, "Finish scanning")
                }
            }
        }
    }

    /**
     * When exit activity
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBluetoothReceiver)
    }
}
