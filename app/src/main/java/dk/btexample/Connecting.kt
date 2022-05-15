package dk.btexample

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.advertise.AdvertiseFilter

class Connecting : Activity(), Manager.ManagerListener {
    private val TAG = this::class.simpleName
    private val REQUEST_BT_PERMISSIONS = 1
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN
    )
    private lateinit var mManager: Manager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mManager = Manager.getSharedInstance()
        setContentView(R.layout.activity_connecting)
    }

    override fun onResume() {
        super.onResume()
        if (mManager.nodes.isEmpty()) {
            if (!checkScanPermission()) {
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    REQUEST_BT_PERMISSIONS
                )
                Log.d(TAG, "BT Permissions not granted")
                // TODO set error message
                gotoConnectionError()
            } else {
                startDiscovery()
            }
            setContentView(R.layout.activity_connecting)
        } else {
            gotoMain()
        }
    }

    override fun onPause() {
        super.onPause()
        stopDiscovery()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN])
    private fun startDiscovery() {
        mManager.addListener(this)
        mManager.startDiscovery(3_000, buildAdvertiseFilter())
    }

    private fun stopDiscovery() {
        mManager.removeListener(this)
        mManager.stopDiscovery()
    }

    private fun checkScanPermission() = permissions.all {
        ActivityCompat.checkSelfPermission(
            applicationContext,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * build a list of object used to filter the node to display in the activity
     * @return a filter to show the node with the advertise format defined by the BlueSTSDK specs
     */
    protected fun buildAdvertiseFilter(): List<AdvertiseFilter?>? {
        return Manager.buildDefaultAdvertiseList()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BT_PERMISSIONS) {
            if (checkScanPermission()) {
                startDiscovery()
            } else {
                Log.e(TAG, "User doesn't allow scanning BLE devices")
            }
        }
    }

    override fun onDiscoveryChange(m: Manager, enabled: Boolean) {
        Log.i(TAG, "discovery changed = $m enabled = $enabled")
        if (!enabled) {
            val nodes = mManager.nodes
            if (nodes.isEmpty()) {
                gotoConnectionError()
            } else {
                gotoMain()
            }
        }
    }

    override fun onNodeDiscovered(m: Manager, node: Node) {
        Log.i(TAG, "Discovered Node = $node")
        gotoMain()
    }

    private fun gotoMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun gotoConnectionError() {
        val intent = Intent(this, ConnectionError::class.java)
        startActivity(intent)
        finish()
    }
}