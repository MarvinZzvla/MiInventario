package com.vendetta.miinventario

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.*
import androidx.core.view.size
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_nueva_venta.*
import java.util.*


class NuevaVenta : AppCompatActivity() {
    var database = ""
    var producto = ""
    var date = ""

    var cantidad = 1;
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var myCalendar : Calendar
    private val fireData = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_venta)

        banner_nuevaVenta.loadAd(AdRequest.Builder().build())
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

            /*****NUEVA BASE DE DATOS******/
            fireData.collection("db1").document(database).collection("Productos").document(producto).get().addOnSuccessListener {
                var cantidadDisponible = it.data?.get("cantidad").toString().toInt()
                println("Cantidad disponible" + it.data?.get("cantidad").toString())
                if (cantidadDisponible >= cantidad){
                    CrearBaseDatos()
                }
                else{Toast.makeText(this,"Cantidad Insuficiente de este producto, tienes: $cantidadDisponible $producto",Toast.LENGTH_SHORT).show()}
            }
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
                Calendar.getInstance().time.hours.toString()+":"+ Calendar.getInstance().time.minutes.toString()+":"+
                Calendar.getInstance().time.seconds.toString()
        nuevaFecha_ventas.text = Editable.Factory.getInstance().newEditable(date)
    }

    fun readData(){
        var listProductos = arrayListOf<String>()
        var adapter = ArrayAdapter(this, R.layout.spinner_style,listProductos)

        /***New Base de datos***/
        fireData.collection("db1").document(database).collection("Productos").get().addOnSuccessListener {

            for (producto in it.documents) {
                listProductos.add(producto.id.toString())

            }
            if (listProductos.isEmpty()){
                Toast.makeText(this,"Primero registre un producto",Toast.LENGTH_SHORT).show()
            }
            spinner_productos.adapter = adapter
            spinner_productos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

            /*****Nueva Base de datos*******/
            fireData.collection("db1").document(database).collection("Ventas").document(database).collection(dateToday).document(timeNow).set(
                ventaData(
                    producto,
                    nuevaFecha_ventas.text.toString(),
                    nuevoPrecio_ventas.text.toString().toInt(),
                    cantidad
                )
            )
            fireData.collection("db1").document(database).collection("Productos").document(producto).get().addOnSuccessListener {
                var actualCantidad = it.data?.get("cantidad").toString().toInt() - cantidad
                fireData.collection("db1").document(database).collection("Productos").document(producto).update("cantidad",actualCantidad)
            }

            updateFinanzasFire(dateToday,timeNow)
            updateGanaciasFire(dateToday,timeNow)

            Intent(this,VentasHome::class.java).apply { startActivity(this) }
        }
        //Si la venta no se realizo
        else{
            Toast.makeText(this,"Verifique todos los campos esten completos",Toast.LENGTH_SHORT).show()
        }
        }

    fun updateGanaciasFire(dateToday: String, timeNow: String) {
        var countDay = 0;
        var countMonth = 0;
        var countYear = 0;
        var precioProduccion = 0
        var dateMonth = "${myCalendar.time.year + 1900}/${myCalendar.time.month + 1}"
        var dateYear = "${myCalendar.time.year + 1900}"

        //Obtener el precio produccion del producto
        fireData.collection("db1").document(database).collection("Productos").document(producto)
            .get().addOnSuccessListener {
            precioProduccion = it.data?.get("precio").toString().toInt()

        //Obtener ganancias y sumarles las nuevas ganancias del dia de hoy
        fireData.collection("db1").document(database).collection("Finanzas").document(dateToday)
            .get().addOnSuccessListener {
            if (it.data?.get("ganancias") == null) {
                countDay =
                    (nuevoPrecio_ventas.text.toString().toInt() - precioProduccion) * cantidad
            } else {
                countDay = it.data?.get("ganancias").toString()
                    .toInt() + ((nuevoPrecio_ventas.text.toString()
                    .toInt() - precioProduccion) * cantidad)
            }
            fireData.collection("db1").document(database).collection("Finanzas").document(dateToday)
                .set(hashMapOf("ganancias" to countDay.toString()), SetOptions.merge())

            //Obtener las ganancias del mes y sumarles las nuevas ganancias del dia de hoy
            fireData.collection("db1").document(database).collection("Finanzas")
                .document(dateMonth + "/ganancias").get().addOnSuccessListener {
                if (!it.exists()) {
                    countMonth =
                        (nuevoPrecio_ventas.text.toString().toInt() - precioProduccion) * cantidad
                } else {
                    countMonth = it.data?.get("ganancias").toString()
                        .toInt() + ((nuevoPrecio_ventas.text.toString()
                        .toInt() - precioProduccion) * cantidad)
                }
                fireData.collection("db1").document(database).collection("Finanzas")
                    .document(dateMonth + "/ganancias")
                    .set(hashMapOf("ganancias" to countMonth.toString()))

                fireData.collection("db1").document(database).collection("Finanzas")
                    .document(dateYear).get().addOnSuccessListener {
                    if (it.data?.get("ganancias") == null) {
                        countYear = (nuevoPrecio_ventas.text.toString()
                            .toInt() - precioProduccion) * cantidad
                    } else {
                        countYear = it.data?.get("ganancias").toString()
                            .toInt() + ((nuevoPrecio_ventas.text.toString()
                            .toInt() - precioProduccion) * cantidad)
                    }
                    fireData.collection("db1").document(database).collection("Finanzas")
                        .document(dateYear).set(
                        hashMapOf("ganancias" to countYear.toString()),
                        SetOptions.merge()
                    )
                }
            }
        }
    }
    }


    fun updateFinanzasFire(dateToday: String, timeNow: String) {
        var countDay = 0; var countMonth = 0; var countYear = 0;
        var dateMonth = "${myCalendar.time.year+1900}/${myCalendar.time.month + 1}"
        var dateYear = "${myCalendar.time.year+1900}"

        //Get ventas del dia
        fireData.collection("db1").document(database).collection("Finanzas").document(dateToday).get().addOnSuccessListener {
            if(it.data?.get("ventas") == null){ countDay = nuevoPrecio_ventas.text.toString().toInt()*cantidad }
            else{ countDay = it.data?.get("ventas").toString().toInt() + nuevoPrecio_ventas.text.toString().toInt() * cantidad }
            //Finanzas del dia -Set Ventas del dia
            fireData.collection("db1").document(database).collection("Finanzas").document(dateToday).set(
                hashMapOf("ventas" to countDay.toString()))

            //Get finanzas del mes
            fireData.collection("db1").document(database).collection("Finanzas").document(dateMonth+"/ventas").get().addOnSuccessListener {
                if(!it.exists()){countMonth = nuevoPrecio_ventas.text.toString().toInt() * cantidad}
                else{countMonth = it.data?.get("ventas").toString().toInt() + nuevoPrecio_ventas.text.toString().toInt() * cantidad}
                fireData.collection("db1").document(database).collection("Finanzas").document(dateMonth+"/ventas").set(
                    hashMapOf("ventas" to countMonth.toString()))

                //Get finanzas del año
                fireData.collection("db1").document(database).collection("Finanzas").document(dateYear).get().addOnSuccessListener {
                    if(it.data?.get("ventas") == null){countYear = nuevoPrecio_ventas.text.toString().toInt()* cantidad}
                    else{ countYear = it.data?.get("ventas").toString().toInt() + (nuevoPrecio_ventas.text.toString().toInt() * cantidad)}
                    fireData.collection("db1").document(database).collection("Finanzas").document(dateYear).set(
                        hashMapOf("ventas" to countYear.toString()))

                    //Inicializar anuncio
                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(this)
                    }else{println("El anuncio esta cargando")}
                }
            }
        }
    }
    /******DELETE****/


    override fun onBackPressed() {
        super.onBackPressed()
        Intent(this,VentasHome::class.java).apply { startActivity(this) }
    }

    fun checkFields():Boolean{return producto.isNotEmpty() && nuevaFecha_ventas.text.toString().isNotEmpty() && nuevoPrecio_ventas.text.toString().isNotEmpty()}
}