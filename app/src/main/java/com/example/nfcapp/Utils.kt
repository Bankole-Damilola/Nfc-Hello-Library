package com.example.nfcapp

import android.util.Log
import kotlin.math.log

object Utils {

    private val HEX_CHARS = "0123456789ABCDEF"
    fun hexStringToByteArray(data: String) : ByteArray {

        val result = ByteArray(data.length / 2)

        for (i in 0 until data.length step 2) {
            val firstIndex = HEX_CHARS.indexOf(data[i]);
            val secondIndex = HEX_CHARS.indexOf(data[i + 1]);

            val octet = firstIndex.shl(4).or(secondIndex)
            result.set(i.shr(1), octet.toByte())
        }

        return result
    }

    private val HEX_CHARS_ARRAY = "0123456789ABCDEF".toCharArray()
    fun toHex(byteArray: ByteArray) : String {
        val result = StringBuffer()

        // Log.v("Method Called", "$byteArray")

        byteArray.forEach {
            // Log.v("Individual byte array element", "$it")
            val octet = it.toInt()
            // Log.v("Foreach octet", octet.toString())
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            // Log.v("Foreach firstIndex", "$firstIndex")
            // Log.v("Foreach secondIndex", "$secondIndex")
            result.append(HEX_CHARS_ARRAY[firstIndex])
            result.append(HEX_CHARS_ARRAY[secondIndex])

            // Log.v("In it foreach", "${HEX_CHARS_ARRAY[firstIndex]} and ${HEX_CHARS_ARRAY[secondIndex]}")
        }

        return result.toString()
    }

    fun byteToHex(byte: Byte) : String {

        val result = StringBuffer()
        val octet = byte.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F

        result.append(HEX_CHARS_ARRAY[firstIndex])
        result.append(HEX_CHARS_ARRAY[secondIndex])

        return result.toString()

    }
}