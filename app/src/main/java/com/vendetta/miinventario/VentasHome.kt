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
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
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
private var list = arrayListOf<QuerySnapshot>()
private var nameProducts = arrayListOf<String>()
private var precioProducts = arrayListOf<String>()
private var dateProducts = arrayListOf<String>()
public var ventasProviderList = arrayListOf<Ventas>()

class VentasHome : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    val fireData = Firebase.firestore
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventas_home)
        auth = Firebase.auth

        adViewVentas.loadAd(AdRequest.Builder().build())

        loadPreferences()
        initCalendar()
        getVentas()
        ventasHomeFechaText.text = "Registros del $daySelected/$monthSelected/$yearSelected"


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
        ventasHomeFechaText.text = "Registros del $daySelected/$monthSelected/$yearSelected"

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun initCalendar() {
        var myCalendar = Calendar.getInstance()
        var datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLable(myCalendar)
            //providerVentas()
            getVentas()


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

        fireData.collection("db1").document(database).collection("Ventas").document(database)
            .collection(yearSelected.toString()).document(monthSelected.toString()).collection(
                daySelected.toString()).addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    list.clear()
                    list.add(snapshot)
                    providerVentas()
                }
            }

    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun providerVentas() {
        nameProducts.clear()
        precioProducts.clear()
        dateProducts.clear()
        ventasProviderList.clear()

        /*** NUEVA BASE DE DATOS *******/
        for (ventas in list[0].documents){
            nameProducts.add(ventas.data?.get("name").toString())
            precioProducts.add(ventas.data?.get("precio").toString())
            dateProducts.add(ventas.data?.get("date").toString())
            ventasProviderList.add(Ventas(ventas.data?.get("name").toString(), ventas.data?.get("date").toString(),
                ventas.data?.get("precio").toString(),ventas.data?.get("cantidad").toString()))
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