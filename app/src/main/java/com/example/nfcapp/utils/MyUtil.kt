package com.example.nfcapp.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

object MyUtil {
    private val hexArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    fun bytesToHex(bytes: ByteArray?): String? {
        if (bytes == null) return null

        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }

        return "0x" + String(hexChars)
    }

    fun bytesToHexAndString(bytes: ByteArray?): String? {
        if (bytes == null) return null

        return bytesToHex(bytes) + " (" + String(bytes) + ")"
    }

    fun now(): String {
        val tz = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        df.timeZone = tz
        return df.format(Date())
    }

    fun showNfcSettingsDialog(app: Activity) {
        AlertDialog.Builder(app)
            .setTitle("NFC is disabled")
            .setMessage("You must enable NFC to use this app.")
            .setPositiveButton(android.R.string.yes) { _, _ ->
                app.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            }
            .setNegativeButton(android.R.string.no) { _, _ ->
                app.finish()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}