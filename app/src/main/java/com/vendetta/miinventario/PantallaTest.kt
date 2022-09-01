package com.vendetta.miinventario

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_pantalla_test.*
import java.text.SimpleDateFormat
import java.util.*

class PantallaTest : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_test)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun updateLable(myCalendar:Calendar){
        var myFormat = "dd-MM-yyyy"
        var sdf = SimpleDateFormat(myFormat, Locale.UK)
        datePicker_text.text = sdf.format(myCalendar.time)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun initCalendar(){
        var myCalendar = Calendar.getInstance()
        var datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR,year)
            myCalendar.set(Calendar.MONTH,month)
            myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateLable(myCalendar)
        }

        btnCreaFecha.setOnClickListener {
            DatePickerDialog(this,datePicker,myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
}