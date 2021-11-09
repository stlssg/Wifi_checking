package it.polito.test.wifichecking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.widget.Toast
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.joda.time.DateTime

class WifiConnectionChangedReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

//        val action = intent?.action
//        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
//            val wifiManager = context?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
//            val wifiInfo = wifiManager.connectionInfo
//            val netId = wifiInfo.networkId
//            val db = Firebase.firestore
//            val now = DateTime.now()
//            if (netId == -1) {
//                Toast.makeText(context, "the wifi is gone", Toast.LENGTH_LONG).show()
//                val input = hashMapOf("timestamp" to now.toString(), "state" to "out")
//                db.collection("test").document(now.toString()).set(input, SetOptions.merge())
//            } else {
//                val bssid = wifiInfo.bssid.toString()
//                Toast.makeText(context, "the wifi is on", Toast.LENGTH_LONG).show()
//                val input = hashMapOf("timestamp" to now.toString(), "bssid" to bssid, "state" to "in")
//                db.collection("test").document(now.toString()).set(input, SetOptions.merge())
//            }
//        }

    }
}