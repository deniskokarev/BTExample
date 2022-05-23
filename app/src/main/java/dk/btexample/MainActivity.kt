package dk.btexample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.st.BlueSTSDK.Feature.FeatureListener
import com.st.BlueSTSDK.Features.FeaturePressure
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node
import dk.btexample.databinding.ActivityMainBinding

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

    private val mNodeStateListener = Node.NodeStateListener() { _, state, _ ->
        when (state) {
            Node.State.Lost, Node.State.Dead, Node.State.Unreachable -> {
                Log.w(TAG, "Connection state changed to ${state}, proceeding to Connecting screen")
                // will invoke onPause(), which removes listeners
                gotoConnectingActivity()
            }
            else -> Unit
        }
    }

    private var mNode: Node? = null

    private var mFeaturePressure: FeaturePressure? = null

    private val mPressureListener =
        FeatureListener { f, sample ->
            val pressureFeature = f as FeaturePressure
            val data = (FeaturePressure::getPressure)(sample)
            val text = "%.01f cpm".format(data)
            runOnUiThread { binding.output.text = text }
        }

    private fun addFeatureListeners(n: Node, feature: FeaturePressure) {
        mFeaturePressure = feature
        feature.addFeatureListener(mPressureListener)
        val sample = feature.sample
        if (sample != null) {
            mPressureListener.onUpdate(feature, sample)
        } else {
            binding.output.text = "updating"
        }
        val rc = n.enableNotification(feature)
        Log.d(TAG, "enableNotification() = $rc")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "BT onResume()")
        val nodes = mManager.nodes
        if (nodes.isEmpty() || !nodes[0].isConnected) {
            mNode = null
            gotoConnectingActivity()
        } else {
            // need to add Node state and Feature listeners
            val n = nodes[0]
            mNode = n
            n.addNodeStateListener(mNodeStateListener)
            val featureList = n.getFeatures(FeaturePressure::class.java)
            if (featureList.isNotEmpty()) {
                addFeatureListeners(n, featureList[0])
            } else {
                binding.output.text = "no pressure feature"
            }
        }
    }

    private fun removeListeners() {
        mFeaturePressure?.removeFeatureListener(mPressureListener)
        mNode?.disableNotification(mFeaturePressure)
        mNode?.removeNodeStateListener(mNodeStateListener)
        mFeaturePressure = null
        mNode = null
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "BT onPause()")
        removeListeners()
    }

    private fun gotoConnectingActivity() {
        val intent = Intent(this, ConnectingActivity::class.java)
        startActivity(intent)
        finish()
    }
}