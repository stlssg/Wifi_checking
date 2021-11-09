package it.polito.test.wifichecking

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.*
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.joda.time.*
import org.joda.time.format.DateTimeFormat
import java.time.Instant.now
import java.time.LocalDateTime.now
import java.time.LocalTime.now
import java.util.concurrent.TimeUnit

class MyWifiService(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    @SuppressLint("LongLogTag")
    override fun doWork(): Result {

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val netID = wifiInfo.networkId
        val bssid = wifiInfo.bssid
//        val SSID = wifiInfo.ssid
        Log.d("wifiInfo", wifiInfo.toString())
        Log.d("netID", netID.toString())

        val address = inputData.getString("Address").toString()
        val start = inputData.getString("Time_Start").toString()
        val end = inputData.getString("Time_End").toString()
//        var interval = 10.toLong()
        var interval = 10.toLong()
        val now = DateTime.now()
        if (!isTimeInRange(now, start, end)) {
//            interval = 20
            interval = getInterval(now, start)
            if (interval == 0.toLong())
                interval = 600.toLong()
        }

//        if (address == bssid) {
//            val db = Firebase.firestore
//            val input = hashMapOf("timestamp" to now.toString(), "BSSID" to bssid)
//            db.collection("test")
//                    .add(input)
//                    .addOnSuccessListener { documentReference ->
//                        Log.d("DocumentSnapshot added with ID: ${documentReference.id}", "yes")
//                    }
//                    .addOnFailureListener { e ->
//                        Log.w("Error adding document", e)
//                    }
//        } else {
//            val db = Firebase.firestore
//            val input = hashMapOf("timestamp" to "why!!!!!", "BSSID" to bssid, "address" to address)
//            db.collection("test")
//                .add(input)
//                .addOnSuccessListener { documentReference ->
//                    Log.d("DocumentSnapshot added with ID: ${documentReference.id}", "yes")
//                }
//                .addOnFailureListener { e ->
//                    Log.w("Error adding document", e)
//                }
//        }

        if (address == bssid) {
            val db = Firebase.firestore
            val input = hashMapOf("timestamp" to now.toString(), "BSSID" to bssid)
            db.collection("test").document(now.toString()).set(input, SetOptions.merge())
        }

        Log.d("interval", "$interval")
        Log.d("outputData", "$address $start $end")
        Log.d("work success", "success function called!!!!!!!!!")
        repeat(address, start, end, interval)

        return Result.success()
    }

    private fun repeat(address: String, start: String, end: String, interval: Long){
        val myRequest: WorkRequest = OneTimeWorkRequestBuilder<MyWifiService>()
            .setInputData(workDataOf("Address" to address, "Time_Start" to start, "Time_End" to end))
            .addTag("oneTimeWork")
            .setInitialDelay(interval, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(myRequest)
    }

    private fun isTimeInRange(now: DateTime, start: String, end: String): Boolean {
        val format = DateTimeFormat.forPattern("HH:mm")
        val startTime: LocalTime = format.parseLocalTime(start)
        val endTime: LocalTime = format.parseLocalTime(end)
        val timeZone = DateTimeZone.getDefault()
        val today: LocalDate = LocalDate.now(timeZone)
        val startMoment: DateTime = today.toLocalDateTime(startTime).toDateTime(timeZone)
        val endMoment: DateTime = today.toLocalDateTime(endTime).toDateTime(timeZone)
        return now.isAfter(startMoment) && now.isBefore(endMoment)
    }

    private fun getInterval(now: DateTime, start: String): Long {
        val format = DateTimeFormat.forPattern("HH:mm")
        val startTime: LocalTime = format.parseLocalTime(start)
        val timeZone = DateTimeZone.getDefault()
        val today: LocalDate = LocalDate.now(timeZone)
        val startMoment: DateTime = today.toLocalDateTime(startTime).toDateTime(timeZone)
        return if (startMoment.isAfter(now)) {
            Seconds.secondsBetween(now, startMoment).seconds.toLong()
        } else {
            val endOfDayString = "23:59"
            val endOfDay: LocalTime = format.parseLocalTime(endOfDayString)
            val endMoment: DateTime = today.toLocalDateTime(endOfDay).toDateTime(timeZone)
            val intervalToday = Seconds.secondsBetween(now, endMoment).seconds.toLong()
            val intervalNextDay = (start.substring(0,2).toInt() * 60 * 60 + start.substring(3,5).toInt() * 60).toLong()
            intervalToday + intervalNextDay
        }
    }
}