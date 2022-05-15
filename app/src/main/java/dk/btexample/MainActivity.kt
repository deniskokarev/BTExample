package dk.btexample

import android.Manifest.permission.*
import android.app.Activity
import android.content.Intent
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

class MainActivity : Activity() {
    private val TAG = this::class.simpleName
    private val REQUEST_BT_PERMISSIONS = 1

    private lateinit var binding: ActivityMainBinding

    private lateinit var mManager: Manager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        Log.d(TAG, "BT onCreate()")
        mManager = Manager.getSharedInstance()
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        val nodes = mManager.nodes
        if (nodes.isEmpty()) {
            gotoConnecting()
        } else {
            val n : Node = nodes[0]
            binding.output.text = "Name: ${n.friendlyName}"
        }
    }

    private fun gotoConnecting() {
        val intent = Intent(this, Connecting::class.java)
        startActivity(intent)
        finish()
    }

    private fun gotoConnectionError() {
        val intent = Intent(this, ConnectionError::class.java)
        startActivity(intent)
        finish()
    }
}