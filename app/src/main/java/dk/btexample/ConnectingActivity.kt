package dk.btexample

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.advertise.AdvertiseFilter

class ConnectingActivity : Activity(), Manager.ManagerListener {
    private val TAG = this::class.simpleName
    private val REQUEST_BT_PERMISSIONS = 1
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN
    )
    private lateinit var mManager: Manager
    private lateinit var statusText: TextView

    private val errNoPermissions =  "BLE Scanning Permissions Not Granted"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mManager = Manager.getSharedInstance()
        setContentView(R.layout.activity_connecting)
        statusText = findViewById(R.id.statusText)
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
                Log.d(TAG, errNoPermissions)
                gotoConnectionErrorActivity(errNoPermissions)
            } else {
                startDiscovery()
            }
        } else {
            connectAndGotoMainOrError()
        }
    }

    override fun onPause() {
        super.onPause()
        stopDiscovery()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN])
    private fun startDiscovery() {
        statusText.text = "Scanning for BlueNRG devices"
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
                gotoConnectionErrorActivity(errNoPermissions)
            }
        }
    }

    override fun onDiscoveryChange(m: Manager, enabled: Boolean) {
        Log.i(TAG, "discovery changed = $m enabled = $enabled")
        if (!enabled) {
            val nodes = mManager.nodes
            if (nodes.isEmpty()) {
                gotoConnectionErrorActivity("No BlueNRG devices around")
            } else {
                connectAndGotoMainOrError()
            }
        }
    }

    override fun onNodeDiscovered(m: Manager, node: Node) {
        Log.i(TAG, "Discovered Node = $node")
        connectAndGotoMainOrError()
    }

    private val mNodeStateListener = object : Node.NodeStateListener {
        override fun onStateChange(node: Node, state: Node.State, prevState: Node.State) {
            when (state) {
                Node.State.Connected -> {
                    node.removeNodeStateListener(this)
                    gotoMainActivity()
                }
                Node.State.Dead, Node.State.Lost, Node.State.Unreachable -> {
                    node.removeNodeStateListener(this)
                    gotoConnectionErrorActivity("Device state became $state")
                }
                else -> Unit
            }
        }
    }

    private fun connectAndGotoMainOrError() {
        if (mManager.nodes.isEmpty()) {
            gotoConnectionErrorActivity("No BlueNRG devices around")
        } else {
            val node = mManager.nodes[0]
            if (node.isConnected) {
                gotoMainActivity()
            } else {
                runOnUiThread { statusText.text = "Connecting to ${node.friendlyName}" }
                node.addNodeStateListener(mNodeStateListener)
                node.connect(applicationContext)
            }
        }
    }

    private fun gotoMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun gotoConnectionErrorActivity(error: String) {
        ConnectionErrorActivity.open(this, error)
        finish()
    }
}