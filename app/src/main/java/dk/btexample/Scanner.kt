package dk.btexample

import android.Manifest.permission.BLUETOOTH_SCAN
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.annotation.RequiresPermission

class Scanner(context: Context) {
    private val TAG = this::class.simpleName
    private val mBluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothLeScanner = mBluetoothManager.adapter.bluetoothLeScanner
    private var scanning = false
    private val thread = HandlerThread("BLE Scan Thread").also { it.start() }
    private val handler = Handler(thread.looper)

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000


    private val leScanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d(TAG, "callBackType = $callbackType, result = $result")
        }
    }

    @RequiresPermission(BLUETOOTH_SCAN)
    fun scanLeDevices() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }
}