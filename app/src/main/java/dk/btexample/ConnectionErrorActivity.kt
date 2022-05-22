package dk.btexample

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView

class ConnectionErrorActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection_error)
        val connectionErrorText = findViewById<TextView>(R.id.connectionError)
        val errorMessage = intent.getStringExtra(ERROR_MESSAGE_EXTRA)?:defaultErrorMessage
        connectionErrorText.text = errorMessage
    }

    private fun gotoConnectingActivity() {
        val intent = Intent(this, ConnectingActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun onTryConnectingButton(view: View) = gotoConnectingActivity()

    companion object {
        const val defaultErrorMessage = "Cannot Connect To Sensor :-("

        val ERROR_MESSAGE_EXTRA = this::class.qualifiedName + "errorMessage"

        fun open(fromContext: Context, errorMessage: String) {
            val intent = Intent(fromContext, ConnectionErrorActivity::class.java)
            intent.putExtra(ERROR_MESSAGE_EXTRA, errorMessage)
            fromContext.startActivity(intent)
        }
    }
}