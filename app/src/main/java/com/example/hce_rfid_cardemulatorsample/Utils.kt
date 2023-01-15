package com.example.hce_rfid_cardemulatorsample

import android.util.Log
import java.nio.ByteBuffer

class Utils {

    companion object {
        const val MOD_ADLER = 65521

        private val HEX_CHARS = "0123456789ABCDEF"
        var APDURESPONSE = "Hello RFID"

        var LAST_KEY = "testtesttest"

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

            byteArray.forEach {
                val octet = it.toInt()
                val firstIndex = (octet and 0xF0).ushr(4)
                val secondIndex = octet and 0x0F
                result.append(HEX_CHARS_ARRAY[firstIndex])
                result.append(HEX_CHARS_ARRAY[secondIndex])
            }

            return result.toString()
        }

        //http://www.cse.yorku.ca/~oz/hash.html
        fun djb2(input: String) : String {
            var hash : UInt = 5381u
            var c : Short;

            for (i in input.indices) {
                c = input[i].code.toShort()
                hash = ((hash shl 5) + hash) + c.toByte().toUInt()
            }

            return hash.toString()
        }
    }

}