package dk.btexample

import android.Manifest.permission.BLUETOOTH
import android.Manifest.permission.BLUETOOTH_ADMIN
import android.app.Activity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import dk.btexample.databinding.ActivityMainBinding
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.util.Log
import android.view.View

class MainActivity : Activity() {
    private val TAG = this::class.simpleName
    private val REQUEST_BT_PERMISSIONS = 1

    private lateinit var binding: ActivityMainBinding
    private lateinit var scanner: Scanner

    private val permissions = arrayOf(BLUETOOTH, BLUETOOTH_ADMIN)

    private fun checkScanPermission() = permissions.all {
        ActivityCompat.checkSelfPermission(
            applicationContext,
            it
        ) == PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "BT onCreate()")

        scanner = Scanner(applicationContext)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun onScanButton(view: View) {
        if (!checkScanPermission()) {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                REQUEST_BT_PERMISSIONS
            )
            Log.d(TAG, "BT Permissions not granted")
        } else {
            scanner.scanLeDevices()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BT_PERMISSIONS) {
            if (checkScanPermission()) {
                scanner.scanLeDevices()
            } else {
                Log.e(TAG, "User doesn't allow scanning BLE devices")
            }
        }
    }

}