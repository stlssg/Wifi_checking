package it.polito.test.wifichecking

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService

const val shortcut_id_IN = "id_in"
const val shortcut_id_OUT = "id_out"

object Shortcuts {

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun setUp(context: Context) {
        val shortcutManager: ShortcutManager? = getSystemService<ShortcutManager>(context, ShortcutManager::class.java)

        val intentCheckIn = Intent(Intent.ACTION_VIEW, null, context, CheckInActivity::class.java)
        val intentCheckOut = Intent(Intent.ACTION_VIEW, null, context, CheckOutActivity::class.java)

        val shortcutIn: ShortcutInfo = ShortcutInfo.Builder(context, shortcut_id_IN)
            .setShortLabel("Check In")
            .setLongLabel("Register the IN action")
            .setIcon(Icon.createWithResource(context, R.drawable.ic_baseline_login_24))
            .setIntent(intentCheckIn)
            .build()
        val shortcutOut: ShortcutInfo = ShortcutInfo.Builder(context, shortcut_id_OUT)
            .setShortLabel("Check Out")
            .setLongLabel("Register the OUT action")
            .setIcon(Icon.createWithResource(context, R.drawable.ic_baseline_logout_24))
            .setIntent(intentCheckOut)
            .build()

        shortcutManager!!.dynamicShortcuts = listOf(shortcutIn, shortcutOut)
    }

}