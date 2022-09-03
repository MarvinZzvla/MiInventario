package com.vendetta.miinventario

import android.app.DatePickerDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_finanzas_home.*
import java.util.*

class FinanzasHome : AppCompatActivity() {
    private var daySelected = ""
    private var monthSelected = ""
    private var yearSelected =""
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finanzas_home)

        initCalendar()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun initCalendar() {
        var myCalendar = Calendar.getInstance()
        var datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(android.icu.util.Calendar.YEAR, year)
            myCalendar.set(android.icu.util.Calendar.MONTH, month)
            myCalendar.set(android.icu.util.Calendar.DAY_OF_MONTH, dayOfMonth)

            daySelected = dayOfMonth.toString()
            monthSelected = (month + 1).toString()
            yearSelected = year.toString()

        }

        btnFinanzasCalendar.setOnClickListener {
            var date = Calendar.getInstance().time.date.toString()+"/"+
                    (Calendar.getInstance().time.month + 1).toString()+"/"+
                    (Calendar.getInstance().time.year + 1900).toString()+" - "+
                    Calendar.getInstance().time.hours.toString()+":"+ Calendar.getInstance().time.minutes.toString()+":"+
                    Calendar.getInstance().time.seconds.toString()
            var dateToday = ""
            date.apply {
                this.split(" - ").apply {
                    this[0].apply {
                        this.split("/").apply {
                             dateToday = "${this[2]}/${this[1]}/${this[0]}"
                        }
                    }
                }
            }


            println("Esta es la fecha de hoy " + dateToday)
            /*
            DatePickerDialog(
                this, datePicker, myCalendar.get(android.icu.util.Calendar.YEAR),
                myCalendar.get(android.icu.util.Calendar.MONTH), myCalendar.get(android.icu.util.Calendar.DAY_OF_MONTH)
            ).show()*/
        }
    }
}