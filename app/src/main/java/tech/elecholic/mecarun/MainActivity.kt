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
    private var MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val TAG = "MainActivity"
    private var deviceFoundFlag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Rotate mecanum
        val lowAnimation = AnimationUtils.loadAnimation(this, R.anim.low_speed_rotation)
        lowAnimation.interpolator = LinearInterpolator()
        mecanum_icon.startAnimation(lowAnimation)

        // Launch Bluetooth
        try {
            launchBluetooth(this, REQUEST_ENABLE_BT)
        } catch (e: NullPointerException) {
            Log.e(TAG, e.message)
        } catch (e: RuntimeException) {
            Log.e(TAG, e.message)
        }

        if (mBluetoothAdapter.isEnabled)
        {
            checkAccredit()
            initFilter()
            //startServer()
        }
    }

    /**
     * Initialize Server
     */
    private fun startServer() {
        if (!mBluetoothAdapter.isEnabled) {
            throw RuntimeException("Please launch bluetooth")
        }
        mmServerSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Teddy", MY_UUID)
        mmServerSocket?: return
        // New thread to start a server socket and wait for connection
        val serverThread = Thread {
            Log.i(TAG, "Server started")
            var socket: BluetoothSocket?
            while(true){
                try{
                    socket = mmServerSocket!!.accept()
                } catch (e: IOException) {
                    break
                }
                if (socket != null) {
                    manageConnectedSocket(socket)
                    mmServerSocket!!.close()
                }
            }
        }
        serverThread.start()
    }

    /**
     * Launch bluetooth
     */
    fun onClickLaunch(v: View) {
        try {
            launchBluetooth(this, REQUEST_ENABLE_BT)
        } catch (e: NullPointerException) {
            Log.i(TAG, e.message)
        } catch (e: RuntimeException) {
            Log.i(TAG, e.message)
        }
    }

    /**
     * Search devices
     */
    fun onClickSearch(v: View) {
//        if (mBluetoothAdapter.isDiscovering) {
//            mBluetoothAdapter.cancelDiscovery()
//        }
//        mBluetoothAdapter.startDiscovery()
        if (!mBluetoothAdapter.isDiscovering) {
            mBluetoothAdapter.startDiscovery()
        }
        Log.i(TAG, "Clicked")
    }

    /**
     * Connection
     */
    fun startConnection(mmDevice: BluetoothDevice) {
        // Start a thread for connection
        mBluetoothAdapter.cancelDiscovery()
        connectingAnimation(this)
        var bluetoothSocket: BluetoothSocket? = null
        try {
            bluetoothSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID)
//            bluetoothSocket = mmDevice.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType).invoke(mmDevice, 1) as BluetoothSocket
        } catch (e: Exception) {
            Log.i(TAG, "Fetch socket error: ${e.message}")
        }
        Thread.sleep(500)
        Thread {
            Thread {
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
                manageConnectedSocket(bluetoothSocket)
            }.start()
        }.start()
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
     * Initialize broadcast filter
     */
    private fun initFilter() {
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mBluetoothReceiver, filter)
    }

    /**
     * Animation shown while scaning
     */
    private fun scaningAnimation(context: Context) {
        // Set animation
        // Accelerate rotating mecanum
        val highAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom_in)
        highAnimation.interpolator = LinearInterpolator()
        mecanum_icon.startAnimation(highAnimation)
        // Button leave
        val leftAnimation = AnimationUtils.loadAnimation(context, R.anim.left_out)
        val rightAnimation = AnimationUtils.loadAnimation(context, R.anim.right_out)
        rightAnimation.fillAfter = true
        leftAnimation.fillAfter = true
        launchBtn.startAnimation(rightAnimation)
        searchBtn.startAnimation(leftAnimation)
        // Text flash
        searchText.visibility = View.VISIBLE
        notFoundText.visibility = View.GONE
        val alphaAnimation = AlphaAnimation(0f, 1f)
        alphaAnimation.duration = 1000
        alphaAnimation.interpolator = LinearInterpolator()
        alphaAnimation.repeatCount = Animation.INFINITE
        alphaAnimation.repeatMode = Animation.REVERSE
        searchText.startAnimation(alphaAnimation)
    }

    /**
     * Animation shown while scaning
     */
    private fun connectingAnimation(context: Context) {
        // Text flash
        searchText.visibility = View.GONE
        notFoundText.visibility = View.GONE
        connectingText.visibility = View.VISIBLE
        val alphaAnimation = AlphaAnimation(0f, 1f)
        alphaAnimation.duration = 1000
        alphaAnimation.interpolator = LinearInterpolator()
        alphaAnimation.repeatCount = Animation.INFINITE
        alphaAnimation.repeatMode = Animation.REVERSE
        searchText.clearAnimation()
        connectingText.startAnimation(alphaAnimation)
    }

    /**
     * Animation shown if device not found
     */
    private fun notFoundAnimation(context: Context) {
        // Set animation
        // Recover rotating speed
        val lowAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom_out)
        lowAnimation.interpolator = LinearInterpolator()
        mecanum_icon.startAnimation(lowAnimation)
        // Button back
        val leftAnimation = AnimationUtils.loadAnimation(context, R.anim.left_in)
        val rightAnimation = AnimationUtils.loadAnimation(context, R.anim.right_in)
        rightAnimation.fillAfter = true
        leftAnimation.fillAfter = true
        launchBtn.startAnimation(leftAnimation)
        searchBtn.startAnimation(rightAnimation)
        // Set text
        searchText.visibility = View.GONE
        searchText.clearAnimation()
        notFoundText.visibility = View.VISIBLE
    }

    /**
     * Animation shown if connection failed
     */
    private fun connectionFailedAnimation(context: Context) {
        // Set animation
        // Recover rotating speed
        val lowAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom_out)
        lowAnimation.interpolator = LinearInterpolator()
        mecanum_icon.startAnimation(lowAnimation)
        // Button back
        val leftAnimation = AnimationUtils.loadAnimation(context, R.anim.left_in)
        val rightAnimation = AnimationUtils.loadAnimation(context, R.anim.right_in)
        rightAnimation.fillAfter = true
        leftAnimation.fillAfter = true
        launchBtn.startAnimation(leftAnimation)
        searchBtn.startAnimation(rightAnimation)
        // Set text
        searchText.visibility = View.GONE
        searchText.clearAnimation()
        connectionFailedText.visibility = View.VISIBLE
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
                    val name = scanDevice.name
                    val address = scanDevice.address
                    if (name == "ZINGBT"){
                        deviceFoundFlag = 1
                        try {
                            scanDevice.javaClass.getMethod("createBond").invoke(scanDevice)
                        } catch (e: Exception) {
                            Log.i(TAG, "Bond failed")
                            connectionFailedAnimation(context)
                        }
                        startConnection(scanDevice)
                    }
                    Log.i(TAG, "name = $name address = $address")
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.i(TAG, "Start scanning")
                    scaningAnimation(context)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i(TAG, "Finish scanning")
                    if (deviceFoundFlag == 0) {
                        notFoundAnimation(context)
                    }
                }
            }
        }
    }

    /**
     * Launch bluetooth
     */
    fun launchBluetooth(context: Activity, requestCode: Int) {
        if (!mBluetoothAdapter.isEnabled) {
            val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            context.startActivityForResult(enableBTIntent, requestCode)
        }
    }

    /**
     * Search devices
     */
    fun searchDevices() {
        val pairedDevices = mBluetoothAdapter.bondedDevices
        for (mmDevice in pairedDevices)
            if (mmDevice.name == "ZINGBT") {
                deviceFoundFlag = 1
                connectingAnimation(this)
                startConnection(mmDevice)
            }
        if (mBluetoothAdapter.isDiscovering) {
            mBluetoothAdapter.cancelDiscovery()
        }
        mBluetoothAdapter.startDiscovery()
    }

    /**
     * Stop Searching
     */
    fun cancelSearch() {
        if (mBluetoothAdapter.isDiscovering) {
            mBluetoothAdapter.cancelDiscovery()
        }
    }

    /**
     * Location permission request
     */
    private fun checkAccredit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_COARSE_LOCATION)
            }
        }
    }

    /**
     * Deal with location permission request result
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "User permit location permission")
                searchDevices()
            } else {
                Log.i(TAG, "User refuse location permission")
                cancelSearch()
                finish()
            }
        }
    }

    /**
     * Deal with bluetooth launching permission request result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode ==REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Bluetooth launched, start server")
                checkAccredit()
                initFilter()
                startServer()
            } else {
                Log.i(TAG, "User refuse to launch bluetooth")
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
