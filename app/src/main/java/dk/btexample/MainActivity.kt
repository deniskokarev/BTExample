package dk.btexample

import android.Manifest.permission.*
import android.app.Activity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import dk.btexample.databinding.ActivityMainBinding
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.util.Log
import android.view.View
import androidx.annotation.RequiresPermission
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.advertise.AdvertiseFilter

class MainActivity : Activity(), Manager.ManagerListener {
    private val TAG = this::class.simpleName
    private val REQUEST_BT_PERMISSIONS = 1

    private lateinit var binding: ActivityMainBinding

    private val permissions = arrayOf(ACCESS_FINE_LOCATION, BLUETOOTH, BLUETOOTH_ADMIN)

    lateinit var mManager: Manager

    private fun checkScanPermission() = permissions.all {
        ActivityCompat.checkSelfPermission(
            applicationContext,
            it
        ) == PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "BT onCreate()")

        mManager = Manager.getSharedInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onPause() {
        super.onPause()
        stopDiscovery()
    }

    fun onStartScanButton(view: View) {
        if (!checkScanPermission()) {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                REQUEST_BT_PERMISSIONS
            )
            Log.d(TAG, "BT Permissions not granted")
        } else {
            startDiscovery()
        }
    }

    fun onStopScanButton(view: View) {
        stopDiscovery()
    }

    /**
     * build a list of object used to filter the node to display in the activity
     * @return a filter to show the node with the advertise format defined by the BlueSTSDK specs
     */
    protected fun buildAdvertiseFilter(): List<AdvertiseFilter?>? {
        return Manager.buildDefaultAdvertiseList()
    }

    @RequiresPermission(allOf = [ACCESS_FINE_LOCATION, BLUETOOTH, BLUETOOTH_ADMIN])
    private fun startDiscovery() {
        mManager.addListener(this)
        mManager.startDiscovery(10_000, buildAdvertiseFilter())
    }

    private fun stopDiscovery() {
        mManager.removeListener(this)
        mManager.stopDiscovery()
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
    }

    override fun onNodeDiscovered(m: Manager, node: Node) {
        Log.i(TAG, "node = $node")
    }

}