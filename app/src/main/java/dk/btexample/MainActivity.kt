package dk.btexample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dk.btexample.databinding.ActivityMainBinding
import android.util.Log
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node

class MainActivity : Activity() {
    private val TAG = this::class.simpleName

    private lateinit var binding: ActivityMainBinding

    private lateinit var mManager: Manager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        Log.d(TAG, "BT onCreate()")
        mManager = Manager.getSharedInstance()
        setContentView(binding.root)
    }

    private val mNodeStateListener = Node.NodeStateListener() {
            _, state, _ ->
        when (state) {
            Node.State.Connected -> Unit
            else -> gotoConnecting()
        }
    }

    private var mNode: Node? = null

    override fun onResume() {
        super.onResume()
        val nodes = mManager.nodes
        if (nodes.isEmpty() || !nodes[0].isConnected) {
            mNode = null
            gotoConnecting()
        } else {
            val n = nodes[0]
            mNode = n
            n.addNodeStateListener(mNodeStateListener)
            binding.output.text = "${n.friendlyName}: ${n.isConnected}"
        }
    }

    override fun onPause() {
        super.onPause()
        mNode?.removeNodeStateListener(mNodeStateListener)
    }

    private fun gotoConnecting() {
        val intent = Intent(this, Connecting::class.java)
        startActivity(intent)
        finish()
    }
}