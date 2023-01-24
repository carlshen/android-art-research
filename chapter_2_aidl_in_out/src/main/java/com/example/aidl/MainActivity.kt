package com.example.aidl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {
    private val serviceBound = AtomicBoolean(false)
    private var iController: IController? = null
    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected: currThread=${Thread.currentThread().name}")
            serviceBound.set(true)
            iController = IController.Stub.asInterface(service)
            registerCallback()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected: currThread=${Thread.currentThread().name}")
            serviceBound.set(false)
        }
    }

    private val callback = object: ICallback.Stub() {
        override fun onResult(result: Int) {
            Log.d(TAG, "onResult: result=$result, currThread=${Thread.currentThread().name}")
        }
    }

    private val stateIn = State(1)
    private val stateOut = State(1)
    private val stateInOut = State(1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        findViewById<View>(R.id.btn_bind_service).setOnClickListener {
            if (serviceBound.get()) return@setOnClickListener
            val intent = Intent()
            intent.component = ComponentName(this@MainActivity, RemoteService::class.java)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        findViewById<View>(R.id.btn_unbind_service).setOnClickListener {
            if (serviceBound.get()) {
                unbindService(serviceConnection)
                serviceBound.set(false)
            }
        }
        findViewById<View>(R.id.btn_transIn).setOnClickListener {
            if (serviceBound.get()) {
                Log.d(TAG, "transIn start: stateIn.value=${stateIn.value}")
                try {
                    iController?.transIn(stateIn)
                } catch (e: Exception) {
                }
                Log.d(TAG, "transIn end: stateIn.value=${stateIn.value}")
            }
        }
        findViewById<View>(R.id.btn_transOut).setOnClickListener {
            if (serviceBound.get()) {
                Log.d(TAG, "transOut start: stateOut.value=${stateOut.value}")
                try {
                    iController?.transOut(stateOut)
                } catch (e: Exception) {
                }
                Log.d(TAG, "transOut end: stateOut.value=${stateOut.value}")
            }
        }
        findViewById<View>(R.id.btn_transInOut).setOnClickListener {
            if (serviceBound.get()) {
                Log.d(TAG, "transInOut start: stateInOut.value=${stateInOut.value}")
                try {
                    iController?.transInOut(stateInOut)
                } catch (e: Exception) {
                }
                Log.d(TAG, "transInOut end: stateInOut.value=${stateInOut.value}")
            }
        }
    }

    private fun registerCallback() {
        try {
            Log.d(TAG, "registerCallback: callback=$callback, currThread=${Thread.currentThread().name}")
            iController?.registerCallback(callback)
        } catch (e: Exception) {
            Log.e(TAG, "registerCallback: ", e)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}