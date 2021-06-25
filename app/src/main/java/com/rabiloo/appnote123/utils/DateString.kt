package com.rabiloo.appnote123.utils

import android.icu.util.Calendar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

object DateString {
    val day = "ngày"
    val month = "tháng"
    val year = "năm"
    val dateArrStr = arrayListOf<String>("một","hai","ba","bốn","tư","năm","sáu","bảy","tám","chín","mười",
        "mười một","mười hai","mười ba","mười bốn","mười tư","mười năm","mười sáu","mười bảy","mười tám","mười chín",
        "hai mươi","hai mốt","hai hai","hai ba","hai bốn","hai tu","hai năm","hai sáu","hai bảy","hai tám","hai chín",
        "ba mươi", "ba mốt")
    val dateArrNumber = arrayListOf<String>("01","02","03","04","04","05","06","07","08","9","10","11","12","13","14","14","15","16","17","18","19","20","21","22","23","24","24","25","26","27","28","29","30","31")

    fun getDayOrMonth(strVoice: String, dayOrMonth: String): String{
        val hashMap = hashMapDate();
        if (dayOrMonth == "ngày"){
            val month = "tháng"
            val startNgay = strVoice.indexOf(dayOrMonth) + dayOrMonth.length + 1
            val endNgay = strVoice.indexOf(month)
            val subNgay = strVoice.substring(startNgay, endNgay).trim();
            for ((key, value) in hashMap.entries) {
                if (subNgay == key){
                    return value
                }
            }
        }else{
            val startThang = strVoice.indexOf(dayOrMonth) + dayOrMonth.length + 1
            val subThang = strVoice.substring(startThang, strVoice.length).trim()
            for ((key, value) in hashMap.entries) {
                if (subThang == key){
                    return value
                }
            }
        }
        return "-1"
    }

    fun getDayOrMonthYear(strVoice: String, dayOrMonth: String): String{
        if (dayOrMonth == "ngày"){
            val month = "tháng"
            val startNgay = strVoice.indexOf(dayOrMonth) + dayOrMonth.length + 1
            val endNgay = strVoice.indexOf(month)
            val subNgay = strVoice.substring(startNgay, endNgay).trim();
            return subNgay
        }else if (dayOrMonth == "tháng"){
            val startThang = strVoice.indexOf(dayOrMonth) + dayOrMonth.length + 1
            val endThang = strVoice.indexOf(year)
            val subThang = strVoice.substring(startThang, endThang).trim()
            return subThang
        }else{
            val startNam = strVoice.indexOf(dayOrMonth) + dayOrMonth.length + 1
            val subNgay = strVoice.substring(startNam, strVoice.length).trim()
            return subNgay
        }
        return "-1"
    }

    fun getDayOrMonthVoiceGG(strVoice: String, dayOrMonth: String): String{
        if (dayOrMonth == "ngày"){
            val month = "tháng"
            val startNgay = strVoice.indexOf(dayOrMonth) + dayOrMonth.length + 1
            val endNgay = strVoice.indexOf(month)
            val subNgay = strVoice.substring(startNgay, endNgay).trim();
            return subNgay
        }else {
            val startThang = strVoice.indexOf(dayOrMonth) + dayOrMonth.length + 1
            val endThang = strVoice.length
            val subThang = strVoice.substring(startThang, endThang).trim()
            return subThang
        }
        return "-1"
    }

    fun getDate(day: String, month: String): String{
        return "$day/$month/2021"
    }

    fun getDateYear(day: String, month: String, year: String): String{
        return "$day/$month/$year"
    }

    fun getDateFull(dayOfWeek: String, day: String, month: String): String{
        return "$dayOfWeek, $day tháng $month năm 2021"
    }

    fun getDayofWeek(date: String): String{
        val c: Calendar = Calendar.getInstance()
        val format1 = SimpleDateFormat("dd/MM/yyyy")
        val dt1: Date = format1.parse(date)
        c.time = dt1
        val dayOfWeek: Int = c.get(Calendar.DAY_OF_WEEK)
        when (dayOfWeek){
            1 -> return "Chủ nhật"
            2 -> return "Thứ hai"
            3 -> return "Thứ ba"
            4 -> return "Thứ tư"
            5 -> return "Thứ năm"
            6 -> return "Thứ sáu"
            7 -> return "Thứ bảy"
            else -> return "-1"
        }
    }

    fun hashMapDate(): HashMap<String, String>{
        val hashMap = HashMap<String, String>()
        for (i in 0 until dateArrStr.size){
            hashMap.put(dateArrStr[i],dateArrNumber[i])
        }
        return hashMap
    }
}