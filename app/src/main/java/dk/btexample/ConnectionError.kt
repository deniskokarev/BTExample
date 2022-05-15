package dk.btexample

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class ConnectionError : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection_error)
    }

    fun onTryConnectingButton(view: View) {
        val intent = Intent(this, Connecting::class.java)
        startActivity(intent)
        finish()
    }
}