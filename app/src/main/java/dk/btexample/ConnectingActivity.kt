package dk.btexample

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
            connectAndGotoMainOrError()
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
                connectAndGotoMainOrError()
            }
        }
    }

    override fun onNodeDiscovered(m: Manager, node: Node) {
        Log.i(TAG, "Discovered Node = $node")
        connectAndGotoMainOrError()
    }

    // has to be object to use `this` reference
    private val mNodeStateListener = object : Node.NodeStateListener {
        override fun onStateChange(node: Node, state: Node.State, prevState: Node.State) {
            when (state) {
                Node.State.Connected -> {
                    node.removeNodeStateListener(this)
                    gotoMain()
                }
                Node.State.Dead, Node.State.Lost, Node.State.Unreachable -> {
                    node.removeNodeStateListener(this)
                    gotoConnectionError()
                }
                else -> Unit
            }
        }
    }

    private fun connectAndGotoMainOrError() {
        if (mManager.nodes.isEmpty()) {
            gotoConnectionError()
        } else {
            val node = mManager.nodes[0]
            if (node.isConnected) {
                gotoMain()
            } else {
                node.addNodeStateListener(mNodeStateListener)
                node.connect(applicationContext)
            }
        }
    }

    private fun gotoMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun gotoConnectionError() {
        val intent = Intent(this, ConnectionErrorActivity::class.java)
        startActivity(intent)
        finish()
    }
}