package com.vendetta.miinventario

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vendetta.miinventario.recycler.Ventas
import com.vendetta.miinventario.recycler.VentasAdapter
import kotlinx.android.synthetic.main.activity_ventas_home.*
import kotlinx.android.synthetic.main.card_layout.*
import java.util.*
import kotlin.collections.ArrayList

private var database = ""
private var isAdmin = false
private var fullname=""
private lateinit var auth: FirebaseAuth
@RequiresApi(Build.VERSION_CODES.N)
var daySelected = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
@RequiresApi(Build.VERSION_CODES.N)
var monthSelected = Calendar.getInstance().get(Calendar.MONTH)+1
@RequiresApi(Build.VERSION_CODES.N)
var yearSelected = Calendar.getInstance().get(Calendar.YEAR)
private var list = arrayListOf<DataSnapshot>()
private var nameProducts = arrayListOf<String>()
private var precioProducts = arrayListOf<String>()
private var dateProducts = arrayListOf<String>()
public var ventasProviderList = arrayListOf<Ventas>()

class VentasHome : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventas_home)
        auth = Firebase.auth

        adViewVentas.loadAd(AdRequest.Builder().build())

        loadPreferences()
        initCalendar()
        getVentas()


        crearVenta_btn.setOnClickListener {
        Intent(this,NuevaVenta::class.java).apply { startActivity(this) }
        }





    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun updateLable(myCalendar: Calendar){
       /* var myFormat = "dd-MM-yyyy"
        var sdf = SimpleDateFormat(myFormat, Locale.UK)
        datePicker_text.text = sdf.format(myCalendar.time)
        */
        daySelected = myCalendar.get(Calendar.DAY_OF_MONTH)
        monthSelected = myCalendar.get(Calendar.MONTH)+1
        yearSelected = myCalendar.get(Calendar.YEAR)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun initCalendar() {
        var myCalendar = Calendar.getInstance()
        var datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLable(myCalendar)
            providerVentas()


        }

        btnFiltrarVenta.setOnClickListener {
            DatePickerDialog(
                this, datePicker, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    fun loadPreferences(){
        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            database = this.getString("database","null").toString()
        }

        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            isAdmin = this.getBoolean("isAdmin",false)
        }

        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            fullname = this.getString("name","null").toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getVentas(){

         Firebase.database.getReference(database).child("Ventas").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()
                    list.add(snapshot)
                    providerVentas()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun providerVentas() {
        nameProducts.clear()
        precioProducts.clear()
        dateProducts.clear()
        ventasProviderList.clear()
        var ventasHoy = list[0].child(yearSelected.toString()).child(monthSelected.toString()).child(
            daySelected.toString()).children

            ventasHoy.forEach {
                nameProducts.add(it.child("name").value.toString())
                precioProducts.add(it.child("precio").value.toString())
                dateProducts.add(it.child("date").value.toString())
                ventasProviderList.add(Ventas(it.child("name").value.toString(),
                    it.child("date").value.toString(), it.child("precio").value.toString()))
            }
        initRecycleView()


    }

    fun initRecycleView(){
        val recyclerView = recycleVentas
        sortList()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = VentasAdapter(ventasProviderList, database)
    }

    fun sortList(){
        var sortList = ArrayList<Ventas>()
        var size = ventasProviderList.size-1
        for (element in ventasProviderList)
        {
            sortList.add(ventasProviderList[size])
            size--
        }
        ventasProviderList = sortList

    }
    override fun onBackPressed() {
        super.onBackPressed()
        Intent(this,HomePage::class.java).apply { startActivity(this) }
    }
}