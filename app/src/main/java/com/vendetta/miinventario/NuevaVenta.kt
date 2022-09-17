package com.vendetta.miinventario

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_nueva_venta.*
import java.time.LocalDateTime
import java.util.*


class NuevaVenta : AppCompatActivity() {
    var database = ""
    var producto = ""
    var date = ""
    var cantidad = 1;
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var myCalendar : Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_venta)

        loadAdFullScreen()

        crearNuevaVenta_btn.setOnClickListener {
            cantidad = 1
            if(nuevaCantidad_ventas.text.isNotEmpty()){
                if(nuevaCantidad_ventas.text.toString().toInt() <= 0){
                    cantidad = 1
                }
                else {
                    cantidad = nuevaCantidad_ventas.text.toString().toInt()
                }
            }

            println("Este es el producto a vender" + producto)
            Firebase.database.getReference(database).child("Productos").child(producto).child("cantidad").get().addOnSuccessListener {
                if(it.exists()){
                    var cantidadDisponible = it.value.toString().toInt()
                    if(cantidadDisponible >= cantidad){
                        CrearBaseDatos()
                    } else{Toast.makeText(this,"Cantidad Insuficiente de este producto, tienes: $cantidadDisponible $producto",Toast.LENGTH_SHORT).show()}
                }else{
                    Toast.makeText(this,"Ocurrio un error inesperado intente mas tarde",Toast.LENGTH_SHORT).show()
                }
            }
            //CrearBaseDatos()
        }

    }

    override fun onStart() {
        super.onStart()
        loadPreferences()
        readData()
        loadCalendar()

        nuevaFecha_ventas.isFocusable = false
    }

    fun loadAdFullScreen(){
        var adRequest = AdRequest.Builder().build()
        //ca-app-pub-2467116940009132/5486356001
        InterstitialAd.load(this, "ca-app-pub-2467116940009132/5486356001",adRequest,object: InterstitialAdLoadCallback(){
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
                println("Este es  el msj: "+adError.message)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                println("Ad Loaded")
                mInterstitialAd = interstitialAd
            }
        })
    }

    fun loadCalendar(){
        myCalendar = Calendar.getInstance()
        var datePicker = DatePickerDialog.OnDateSetListener { datePicker, year, month,dayOfMonth ->
            myCalendar.set(Calendar.YEAR,year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)

            date = myCalendar.time.date.toString()+"/"+
                    (myCalendar.time.month + 1).toString()+"/"+
                    (myCalendar.time.year + 1900).toString()+" - "+
                    Calendar.getInstance().time.hours.toString()+":"+ Calendar.getInstance().time.minutes.toString()+":"+
                    Calendar.getInstance().time.seconds.toString()

            nuevaFecha_ventas.text = Editable.Factory.getInstance().newEditable(date)
        }
        nuevaFecha_ventas.setOnClickListener {
            DatePickerDialog(this,datePicker,myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        date = myCalendar.time.date.toString()+"/"+
                (myCalendar.time.month + 1).toString()+"/"+
                (myCalendar.time.year + 1900).toString()+" - "+
                Calendar.getInstance().time.hours.toString()+":"+ Calendar.getInstance().time.minutes.toString()
        nuevaFecha_ventas.text = Editable.Factory.getInstance().newEditable(date)
    }

    fun readData(){
        var listProductos = arrayListOf<String>()
        var adapter = ArrayAdapter(this, R.layout.spinner_style,listProductos)

        Firebase.database.getReference(database).child("Productos").get().addOnSuccessListener {
            for(child in it.children){
                listProductos.add(child.key.toString())
            }

            spinner_productos.adapter = adapter
            spinner_productos.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    producto = listProductos[p2]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }
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

    data class ventaData(val name:String,val date:String,val precio:Int, val cantidad:Int)

    fun CrearBaseDatos(){
        //var dateToday = "${LocalDateTime.now().year}/${LocalDateTime.now().monthValue}/${LocalDateTime.now().dayOfMonth}"
        var dateToday = "${myCalendar.time.year + + 1900}/${myCalendar.time.month + 1}/${myCalendar.time.date}"
        //var timeNow = "${LocalDateTime.now().hour.toString()}:${LocalDateTime.now().minute.toString()}:${LocalDateTime.now().second.toString()}"
        var timeNow = nuevaFecha_ventas.text.toString()
        timeNow.split(" - ").apply {
            timeNow = this[1]
        }

        if(checkFields()) {
            //Si la venta se realizo
            Firebase.database.getReference(database).child("Ventas").child(dateToday).child(timeNow).setValue(
                ventaData(
                    producto,
                    nuevaFecha_ventas.text.toString(),
                    nuevoPrecio_ventas.text.toString().toInt(),
                    cantidad
                )
            )
            //Actualizar la cantidad actual de producto vendido
            Firebase.database.getReference(database).child("Productos").child(producto).child("cantidad").get().addOnSuccessListener {
                var count = it.value.toString().toInt()
                count -= cantidad
                Firebase.database.getReference(database).child("Productos").child(producto).child("cantidad").setValue(count.toString())

            }
            updateFinanzas(dateToday,timeNow)

            Intent(this,VentasHome::class.java).apply { startActivity(this) }
        }
        //Si la venta no se realizo
        else{
            Toast.makeText(this,"Verifique todos los campos esten completos",Toast.LENGTH_SHORT).show()
        }
        }

    fun updateFinanzas(dateToday: String, timeNow: String) {
        var countDay = 0; var countMonth = 0; var countYear = 0;
        var dateMonth = "${myCalendar.time.year+1900}/${myCalendar.time.month + 1}"
        var dateYear = "${myCalendar.time.year+1900}"
        //Get ventas del dia
        Firebase.database.getReference(database).child("Finanzas").child(dateToday)
            .child("ventas").get().addOnSuccessListener {
                if (!it.exists()){ countDay = nuevoPrecio_ventas.text.toString().toInt() * cantidad}
                else{
                countDay = it.value.toString().toInt() + nuevoPrecio_ventas.text.toString().toInt()* cantidad}
                //Finanzas del dia - Set ventas del dia
                Firebase.database.getReference(database).child("Finanzas").child(dateToday)
                    .child("ventas").setValue((countDay).toString())

            //Get Finanzas del mes
                Firebase.database.getReference(database).child("Finanzas").child(dateMonth).
                child("ventas").get().addOnSuccessListener {
                    if (!it.exists()){
                        countMonth = nuevoPrecio_ventas.text.toString().toInt() * cantidad
                    }
                    else{
                        countMonth = it.value.toString().toInt() + nuevoPrecio_ventas.text.toString().toInt()* cantidad
                    }
                    Firebase.database.getReference(database).child("Finanzas").child(dateMonth)
                        .child("ventas").setValue((countMonth).toString())

                    //Get Finanzas del año
                    Firebase.database.getReference(database).child("Finanzas").child(dateYear)
                        .child("ventas").get().addOnSuccessListener {
                            if(!it.exists()){
                                countYear = nuevoPrecio_ventas.text.toString().toInt()* cantidad
                            }
                            else{
                                countYear = it.value.toString().toInt() + (nuevoPrecio_ventas.text.toString().toInt() * cantidad)
                            }

                            Firebase.database.getReference(database).child("Finanzas").child(dateYear).
                                child("ventas").setValue(countYear)
                            //Inicializar anuncio
                            if (mInterstitialAd != null) {
                                mInterstitialAd?.show(this)
                            }else{println("El anuncio esta cargando")}

                        }
                }
            }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        Intent(this,VentasHome::class.java).apply { startActivity(this) }
    }

    fun checkFields():Boolean{return producto.isNotEmpty() && nuevaFecha_ventas.text.toString().isNotEmpty() && nuevoPrecio_ventas.text.toString().isNotEmpty()}
}