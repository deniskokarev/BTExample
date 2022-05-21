package dk.btexample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View

class ConnectionErrorActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection_error)
    }

    fun onTryConnectingButton(view: View) {
        val intent = Intent(this, ConnectingActivity::class.java)
        startActivity(intent)
        finish()
    }
}