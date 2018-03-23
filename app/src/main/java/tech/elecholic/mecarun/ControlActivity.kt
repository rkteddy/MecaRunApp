package tech.elecholic.mecarun

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_control.*
import tech.elecholic.mecanum.RockerView
import java.lang.Exception

class ControlActivity: AppCompatActivity() {

    private val mBluetoothSocketHandler = BluetoothSocketHandler.get()
    private val socket = mBluetoothSocketHandler.getSocket()
    val mmOutputSteam = socket.outputStream
    val mmInputStream = socket.inputStream
    val TAG = "ControlActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        Thread{
            val buffer = ByteArray(1024)

            while (true) {
                while (mmInputStream.available() == 0) {}
                try {
                    mmInputStream!!.read(buffer)
                    angleView.text = String(buffer)
                    Log.i(TAG, "Receive ${String(buffer)}")
                } catch (e: Exception) {
                    break
                }
            }
        }.start()

        rockerView.setOnAngleChangedListener(object: RockerView.OnAngleChangedListener {
            override fun onAngleChanged(ang: Float) {
                mmOutputSteam.write((ang.toString() + "\n").toByteArray())
                Log.i(TAG, "Send $ang")
                angleView.text = ang.toString()
            }
        })
    }
}