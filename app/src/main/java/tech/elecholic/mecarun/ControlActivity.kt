package tech.elecholic.mecarun

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
                    textView.text = String(buffer)
                    Log.i(TAG, "Receive ${String(buffer)}")
                } catch (e: Exception) {
                    break
                }
            }
        }.start()

        rockerView.setOnAngleChangedListener(object: RockerView.OnAngleChangedListener {
            override fun onAngleChanged(ang: Float) {
                textView.text = ang.toString()
            }
        })
    }

    fun sendOK(v: View) {
        try {
            mmOutputSteam.write("OK".toByteArray())
            Log.i(TAG, "Send")
        } catch (e: Exception) {
            Log.i(TAG, "Cannot write!")
        }
    }
}