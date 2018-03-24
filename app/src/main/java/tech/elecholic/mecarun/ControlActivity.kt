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
    val mmOutputStream = socket.outputStream
    val mmInputStream = socket.inputStream
    val outputHeader_1 = 0xff
    val outputHeader_2 = 0xfe
    val outputHeader_3 = 0x01
    var x = 0
    var y = 0
    var r = 0
    val outputEnd = 0x00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        /*Thread{
            val buffer = ByteArray(1024)

            while (true) {
                while (mmInputStream.available() == 0) {}
                try {
                    mmInputStream!!.read(buffer)
                    Log.i(TAG, "Receive ${String(buffer)}")
                } catch (e: Exception) {
                    break
                }
            }
        }.start()*/

        rockerViewLeft.setOnSpeedChangedListener(object: RockerView.OnSpeedChangedListener {
            override fun onSpeedChanged(xSpeed: Float, ySpeed: Float) {
                x = xSpeed.toInt()
                y = ySpeed.toInt()
                xSpeedTextView.text = x.toString()
                ySpeedTextView.text = y.toString()
            }
        })

        rockerViewRight.setOnSpeedChangedListener(object: RockerView.OnSpeedChangedListener {
            override fun onSpeedChanged(xSpeed: Float, ySpeed: Float) {
                r = xSpeed.toInt()
                rotationTextView.text = r.toString()
            }
        })

        Thread{
            while (true) {
                Thread.sleep(50)
                mmOutputStream.write(outputHeader_1)
                mmOutputStream.write(outputHeader_2)
                mmOutputStream.write(outputHeader_3)
                mmOutputStream.write(x/256)
                mmOutputStream.write(x%256)
                mmOutputStream.write(y/256)
                mmOutputStream.write(y%256)
                mmOutputStream.write(r/256)
                mmOutputStream.write(r%256)
                mmOutputStream.write(outputEnd)
            }
        }.start()
    }
}