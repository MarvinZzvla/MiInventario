package com.vendetta.miinventario

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import com.google.android.gms.ads.AdRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_finanzas_home.*
import java.util.*
import kotlin.collections.ArrayList

class FinanzasHome : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    private var daySelected = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()
    private var monthSelected = (Calendar.getInstance().get(Calendar.MONTH)+1).toString()
    private var yearSelected = Calendar.getInstance().get(Calendar.YEAR).toString()
    private var database = ""
    private var list = arrayListOf<DataSnapshot>()
    private var listMes = arrayListOf<String>("Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto",
    "Septiembre","Octubre","Noviembre","Diciembre")
    val fireData = Firebase.firestore
    var ventasYear = "0"; var gananciasYear = "0"; var ventasMonth = "0"; var gananciasMonth = "0"; var ventasToday ="0"; var gananciasToday="0"

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finanzas_home)
        banner_finanzas.loadAd(AdRequest.Builder().build())
        loadPreferences()
        initCalendar()
        loadInfoDatabase()

        showGanancias.setOnClickListener {
            if (showGanancias.isChecked){
                infoGanancias.visibility = View.GONE
            }
            else{
                infoGanancias.visibility = View.VISIBLE
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun loadInfoDatabase(){
        readFireStore()
    }
    fun createDefaultFinanzas(){
        fireData.collection("db1").document(database).collection("Finanzas")
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun readFireStore(){
        fireData.collection("db1").document(database).collection("Finanzas").document(yearSelected).get().addOnSuccessListener {

            ventasYear = (it.data?.get("ventas")?:"0").toString()
            gananciasYear = (it.data?.get("ganancias")?:"0").toString()
            fireData.collection("db1").document(database).collection("Finanzas")
                .document(yearSelected).collection(monthSelected).document("ventas").get()
                .addOnSuccessListener {
                    ventasMonth = it.data?.get("ventas").toString()
                    if (ventasMonth == "null") {
                        ventasMonth = "0"
                    }

                    fireData.collection("db1").document(database).collection("Finanzas").document("$yearSelected/$monthSelected/$daySelected").get().addOnSuccessListener {
                        ventasToday = it.data?.get("ventas").toString()
                        if(ventasToday == "null"){ventasToday="0"}

                        fireData.collection("db1").document(database).collection("Finanzas").document(yearSelected).collection(monthSelected).document("ganancias").get().addOnSuccessListener {
                            gananciasMonth = it.data?.get("ganancias").toString()
                            if(gananciasMonth == "null"){gananciasMonth = "0"}

                            fireData.collection("db1").document(database).collection("Finanzas").document("$yearSelected/$monthSelected/$daySelected").get().addOnSuccessListener {
                                gananciasToday = it.data?.get("ganancias").toString()
                                if(gananciasToday == "null"){gananciasToday = "0"}
                                readInfo()
                            }

                        }

                    }

                }
        }


    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    fun readInfo(){

        fechaHoyFinanzas.text = "$daySelected/$monthSelected/$yearSelected"
        fechaMesFinanzas.text = "${listMes[(monthSelected.toInt()-1)]} $yearSelected"
        fechaYearFinanzas.text = yearSelected





        ventasHoyText.text = "$${ventasToday?:0.toString()}"
        ventasMesText.text =  "$${ventasMonth?:0.toString()}"
        ventasYearText.text = "$${ventasYear?:0.toString()}"

        ventasGananciasToday.text = "$${gananciasToday?:"0".toString()}"
        ventasGanaciasMes.text = "$${gananciasMonth?:"0".toString()}"
        ventasGanaciasYear.text = "$${gananciasYear?:"0".toString()}"

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun initCalendar() {
        var myCalendar = Calendar.getInstance()
        var datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            daySelected = myCalendar.get(Calendar.DAY_OF_MONTH).toString()
            monthSelected = (myCalendar.get(Calendar.MONTH) + 1).toString()
            yearSelected = myCalendar.get(Calendar.YEAR).toString()

            readFireStore()

        }

        btnFinanzasCalendar.setOnClickListener {

            DatePickerDialog(
                this, datePicker, myCalendar.get(android.icu.util.Calendar.YEAR),
                myCalendar.get(android.icu.util.Calendar.MONTH), myCalendar.get(android.icu.util.Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    fun loadPreferences(){
        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            database = this.getString("database","null").toString()
        }

        /*
        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            isAdmin = this.getBoolean("isAdmin",false)
        }

        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            fullname = this.getString("name","null").toString()
        }*/
    }
}