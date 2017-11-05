package tech.elecholic.mecarun

import android.bluetooth.BluetoothSocket

class BluetoothSocketHandler {

    private lateinit var socket: BluetoothSocket

    companion object {
        fun get(): BluetoothSocketHandler {
            return Inner.singleInstance
        }
    }

    private object Inner {
        val singleInstance = BluetoothSocketHandler()
    }

    fun setSocket(socket: BluetoothSocket) {
        this.socket = socket
    }

    fun getSocket(): BluetoothSocket {
        return socket
    }
}