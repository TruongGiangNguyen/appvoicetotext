package com.rabiloo.appnote.datepickerdialog

import android.app.DatePickerDialog
import android.content.Context
import java.util.*

class DatePicker(val context: Context, val listener: DatePickerDialog.OnDateSetListener) {

    fun open() {
        val c: Calendar = Calendar.getInstance()
        val mYear: Int = c.get(Calendar.YEAR)
        val mMonth: Int = c.get(Calendar.MONTH)
        val mDay: Int = c.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(context, listener, mYear, mMonth, mDay).show()
        }

}