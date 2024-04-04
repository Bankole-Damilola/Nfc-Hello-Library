package com.example.nfcapp

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.widget.ExpandableListView
import android.widget.TextView
import android.widget.Toast
import com.example.firstlibrary.Hello
import com.example.nfcapp.utils.CustomExpandableListAdapter
import com.example.nfcapp.utils.MyUtil
import com.example.nfcapp.utils.TagWrapper
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private var adapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private lateinit var tv : TextView

    // private lateinit var currentTagView: TextView
    private lateinit var expandableListView: ExpandableListView

    private var touchDownX: Float = 0.toFloat()
    private var touchUpX: Float = 0.toFloat()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // currentTagView = findViewById(R.id.currentTagView)
        // currentTagView.text = "Loading..."

        tv = findViewById(R.id.text_view)
        val hello = Hello(this, "Hello")
        hello.getHelloToast()

        expandableListView = findViewById(R.id.expandableListView)
        expandableListView.setOnTouchListener { _, event ->
            val swipeThreshold = 150f

            when (event.action) {
                MotionEvent.ACTION_DOWN -> touchDownX = event.x
                MotionEvent.ACTION_UP -> {
                    touchUpX = event.x
                    val deltaX = touchUpX - touchDownX

                    if (deltaX > swipeThreshold) {
                        showPreviousTag()
                    } else if (deltaX < -swipeThreshold) {
                        showNextTag()
                    }
                }
            }

            false
        }

        adapter = NfcAdapter.getDefaultAdapter(this)
    }

//    override fun onResume() {
//        super.onResume()
//
//        if (!adapter?.isEnabled!!) {
//            MyUtil.showNfcSettingsDialog(this)
//            return
//        }
//
//        if (pendingIntent == null) {
//            pendingIntent = PendingIntent.getActivity(
//                this, 0,
//                Intent(this, this::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
//                PendingIntent.FLAG_MUTABLE
//            )
//
//            // currentTagView.text = "Scan a tag"
//        }
//
//        showTag()
//
//        adapter?.enableForegroundDispatch(this, pendingIntent, null, null)
//    }

//    override fun onPause() {
//        super.onPause()
//        adapter?.disableForegroundDispatch(this)
//    }

//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        Log.d("onNewIntent", "Discovered tag with intent $intent")
//
//        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
//        val isoDepTag = IsoDep.get(tag)
//        isoDepTag.connect()
//        if(isoDepTag.isConnected) {
//            val response = isoDepTag.transceive(Utils.hexStringToByteArray("00A4040007A0000000031010"))
//            runOnUiThread {
//                tv.text = Utils.toHex(response)
//                try {
//                    Log.v("ReturnedByteMeaning", readRecord(response))
//                } catch (e: Exception) {
//                    Log.e("RespondTag", e.message.toString())
//                }
//            }
//        } else {
//            Log.e("Tag", "Connection failed")
//        }
//        Log.v("TagFromIntent", tag.toString())
//        // val tagId = MyUtil.bytesToHex(tag?.id)
////        Log.v("TagFromIntent", tagId.toString())
////        val tagWrapper = tagId?.let { TagWrapper(it) }
////
////        val misc = ArrayList<String>()
////        misc.add("scanned at: ${MyUtil.now()}")
////
////        val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
////
////        var tagData = ""
////        Log.v("TagFromIntent", rawMsgs.toString())
////
////        if (rawMsgs != null) {
////            val msg = rawMsgs[0] as NdefMessage
////            val cardRecord = msg.records[0]
////            try {
////                tagData = readRecord(cardRecord.payload)
////                Log.v("TagScanTry", tagData)
////            } catch (e: UnsupportedEncodingException) {
////                e.message?.let { Log.e("TagScan", it) }
////                return
////            }
////        }
////
////        misc.add("tag data: $tagData")
////        if (tagWrapper != null) {
////            tagWrapper.techList["Misc"] = misc
////        }
////
////        for (tech in tag?.techList!!) {
////            val techInfo = tech.replace("android.nfc.tech.", "")
////            Log.v("TagFromIntent", techInfo)
////            val info = getTagInfo(tag, techInfo)
////            if (tagWrapper != null) {
////                tagWrapper.techList["Technology: $techInfo"] = info
////            }
////        }
////
////        if (tags.size == 1) {
////            Toast.makeText(this, "Swipe right to see previous tags", Toast.LENGTH_LONG).show()
////        }
////
////        if (tagWrapper != null) {
////            tags.add(tagWrapper)
////        }
////        currentTagIndex = tags.size - 1
////        showTag()
//    }

    @Throws(UnsupportedEncodingException::class)
    private fun readRecord(payload: ByteArray): String {
        val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
        val languageCodeLength = payload[0].toInt() and 63
        return String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, Charset.forName(textEncoding))
    }

    private fun showPreviousTag() {
        currentTagIndex = (currentTagIndex - 1 + tags.size) % tags.size
        showTag()
    }

    private fun showNextTag() {
        currentTagIndex = (currentTagIndex + 1) % tags.size
        showTag()
    }

    private fun showTag() {
        if (tags.isEmpty()) return

        val tagWrapper = tags[currentTagIndex]
        val techList = tagWrapper.techList
        val expandableListTitle = ArrayList(techList.keys)

        expandableListView.setAdapter(
            CustomExpandableListAdapter(this, expandableListTitle, techList)
        )

        val count = expandableListView.count
        for (i in 0 until count) expandableListView.expandGroup(i)

        // currentTagView.text = "Tag ${tagWrapper.id} (${currentTagIndex + 1}/${tags.size})"
    }

    private fun getTagInfo(tag: Tag, tech: String): List<String> {
        val info = ArrayList<String>()

        when (tech) {
            "NfcA" -> {
                info.add("aka ISO 14443-3A")

                val nfcATag = NfcA.get(tag)
                info.add("atqa: ${MyUtil.bytesToHexAndString(nfcATag.atqa)}")
                info.add("sak: ${nfcATag.sak}")
                info.add("maxTransceiveLength: ${nfcATag.maxTransceiveLength}")
            }

            "NfcF" -> {
                info.add("aka JIS 6319-4")

                val nfcFTag = NfcF.get(tag)
                info.add("manufacturer: ${MyUtil.bytesToHex(nfcFTag.manufacturer)}")
                info.add("systemCode: ${MyUtil.bytesToHex(nfcFTag.systemCode)}")
                info.add("maxTransceiveLength: ${nfcFTag.maxTransceiveLength}")
            }

            "NfcV" -> {
                info.add("aka ISO 15693")

                val nfcVTag = NfcV.get(tag)
                info.add("dsfId: ${nfcVTag.dsfId}")
                info.add("responseFlags: ${nfcVTag.responseFlags}")
                info.add("maxTransceiveLength: ${nfcVTag.maxTransceiveLength}")
            }

            "Ndef" -> {
                val ndefTag = Ndef.get(tag)
                var ndefMessage: NdefMessage? = null

                try {
                    ndefTag.connect()
                    ndefMessage = ndefTag.ndefMessage
                    ndefTag.close()

                    for (record in ndefMessage?.records ?: emptyArray()) {
                        val id = if (record.id.isEmpty()) "null" else MyUtil.bytesToHex(record.id)
                        info.add("record[$id].tnf: ${record.tnf}")
                        info.add("record[$id].type: ${MyUtil.bytesToHexAndString(record.type)}")
                        info.add("record[$id].payload: ${MyUtil.bytesToHexAndString(record.payload)}")
                    }

                    info.add("messageSize: ${ndefMessage?.byteArrayLength}")

                } catch (e: Exception) {
                    e.printStackTrace()
                    info.add("error reading message: ${e.toString()}")
                }

                val typeMap = mapOf(
                    Ndef.NFC_FORUM_TYPE_1 to "typically Innovision Topaz",
                    Ndef.NFC_FORUM_TYPE_2 to "typically NXP MIFARE Ultralight",
                    Ndef.NFC_FORUM_TYPE_3 to "typically Sony Felica",
                    Ndef.NFC_FORUM_TYPE_4 to "typically NXP MIFARE Desfire"
                )

                val type = ndefTag.type
                val typeDescription = typeMap[type] ?: ""
                info.add("type: $type${if (typeDescription.isNotEmpty()) " ($typeDescription)" else ""}")

                info.add("canMakeReadOnly: ${ndefTag.canMakeReadOnly()}")
                info.add("isWritable: ${ndefTag.isWritable}")
                info.add("maxSize: ${ndefTag.maxSize}")
            }

            "NdefFormatable" -> {
                info.add("nothing to read")
            }

            "MifareUltralight" -> {
                val mifareUltralightTag = MifareUltralight.get(tag)
                info.add("type: ${mifareUltralightTag.type}")
                info.add("timeout: ${mifareUltralightTag.timeout}")
                info.add("maxTransceiveLength: ${mifareUltralightTag.maxTransceiveLength}")
            }

            "IsoDep" -> {
                info.add("aka ISO 14443-4")

                val isoDepTag = IsoDep.get(tag)
                info.add("historicalBytes: ${MyUtil.bytesToHexAndString(isoDepTag.historicalBytes)}")
                info.add("hiLayerResponse: ${MyUtil.bytesToHexAndString(isoDepTag.hiLayerResponse)}")
                info.add("timeout: ${isoDepTag.timeout}")
                info.add("extendedLengthApduSupported: ${isoDepTag.isExtendedLengthApduSupported}")
                info.add("maxTransceiveLength: ${isoDepTag.maxTransceiveLength}")
//                val readerCallBack = NfcAdapter.ReaderCallback {
//                    isoDepTag.connect()
//                    val response = isoDepTag.transceive(Utils.hexStringToByteArray("00A4040007A000000077AB01"))
//                    Log.v("MainActivity", response.size.toString())
//                    if (response.isNotEmpty() && Utils.byteToHex(response[response.size - 2]) == "90" && Utils.byteToHex(response[response.size - 1]) == "00") {
//                        val gpoResponse = isoDepTag.transceive(Utils.hexStringToByteArray("00B2010C00E4"))
//                        Log.v("MainActivityGPO", Utils.toHex(response))
//                    }
//                    runOnUiThread { tv.append("\nCard Response: " + Utils.toHex(response)) }
//                    isoDepTag.close()
//                }
            }
        else -> {
                info.add("unknown tech!")
            }
        }

        return info
    }

    companion object {
        private val tags = ArrayList<TagWrapper>()
        private var currentTagIndex = -1
    }

//    var nfcAdapter : NfcAdapter? = null
//    private lateinit var textView: TextView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        textView = findViewById(R.id.text_view)
//
//        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
//    }
//
//    private fun playSound() {
//        try {
//            val notification =
//                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//            val ringtone = RingtoneManager.getRingtone(
//                applicationContext,
//                notification
//            )
//            ringtone.play()
//        } catch (e: Exception) {
//            // Some error playing sound
//            runOnUiThread {
//                Toast.makeText(this, "Error al intentar escribir", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
//    }
//
//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        var tagFromIntent: Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
//        val nfc = NfcA.get(tagFromIntent)
//
//        val atqa: ByteArray = nfc.atqa
//        val sak: Short = nfc.sak
//
//        nfc.connect()
//        val isConnected= nfc.isConnected
//
//
//        if(isConnected) {
//            val byteArray = byteArrayOf(1,2)
//            val receivedData:ByteArray= nfc.transceive(byteArray)
//            // ..
//            //code to handle the received data
//            // Received data would be in the form of a byte array that can be converted to string
//            //NFC_READ_COMMAND would be the custom command you would have to send to your NFC Tag in order to read it
//            // ..
//        } else{
//        Log.e("ans", "Not connected")
//        }
//    }

//    private fun enableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {
//
//        val intent = Intent(activity.applicationContext, activity.javaClass)
//        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//
//        val pendingIntent = PendingIntent.getActivity(
//            activity.applicationContext,
//            0,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val filters = arrayOfNulls<IntentFilter>(1)
//        val techList = arrayOf<Array<String>>()
//
//        filters[0] = IntentFilter()
//        with(filters[0]) {
//            this?.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
//            this?.addCategory(Intent.CATEGORY_DEFAULT)
//            try {
//                this?.addDataType("text/plain")
//            } catch (e: IntentFilter.MalformedMimeTypeException) {
//                throw RuntimeException(e)
//            }
//        }
//
//        adapter?.enableForegroundDispatch(activity, pendingIntent, filters, techList)
//    }
//
    override fun onResume() {
        super.onResume()
        adapter?.enableReaderMode(this, this,
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null)

       // enableForegroundDispatch(this, this.nfcAdapter)
    }

    override fun onPause() {
        super.onPause()

        adapter?.disableReaderMode(this)
    }


    override fun onTagDiscovered(p0: Tag?) {

        // 00A4040007A000000077AB01 this s for lagId
        // 00A4040007A0000000031010 this is for visa card

        if (p0 != null) {
            Log.v("MainActivity", Utils.toHex(p0.id))
        }

        val tagTech = p0?.techList
        p0?.describeContents()
        var isIsoDepInTechList = false
        if (tagTech != null) {
            for (tech in tagTech) {
                if (tech == "android.nfc.tech.IsoDep") isIsoDepInTechList = true
            }
        }
        if (isIsoDepInTechList) {

            val isoDep = IsoDep.get(p0)
            isoDep.connect()
            val response = isoDep.transceive(Utils.hexStringToByteArray(
                "00A4040007A000000077AB01"))
            Log.v("MainActivity", response.size.toString())
            if (response.isNotEmpty() && Utils.byteToHex(response[response.size - 2]) == "90" && Utils.byteToHex(response[response.size - 1]) == "00") {
                val gpoResponse = isoDep.transceive(Utils.hexStringToByteArray(
                    "00B2010C00E4"))

                Log.v("MainActivityGPO", Utils.toHex(gpoResponse))
            }

            runOnUiThread { tv.append("\nCard Response: "
                    + Utils.toHex(response)) }
            isoDep.close()
        }
    }
}