package com.example.marcelo.icargo.utils

import java.text.SimpleDateFormat
import java.util.*


class ReturnData {
 companion object {
     fun dataString(formato: String): String {

         val dateFormat1 = SimpleDateFormat(formato)
         val data1 = Date()
         val cal1 = Calendar.getInstance()
         cal1.setTime(data1)
         val dataAgora = cal1.getTime()

         return dateFormat1.format(dataAgora)

     }
 }
}