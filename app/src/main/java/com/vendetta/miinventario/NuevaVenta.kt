package com.vendetta.miinventario

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.android.synthetic.main.activity_nueva_venta.*
import kotlinx.android.synthetic.main.activity_nuevo_producto.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.*


class NuevaVenta : AppCompatActivity() {
    var database = ""
    var producto = ""
    var date = ""
    private var barCode = ""

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
            checkQuantity()

        }

        barCodeVentas_btn.setOnClickListener {

            initCodeScan()
        }

    }

    private fun checkQuantity(){
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
        fireData.collection("db1").document(database).collection("Productos").document(producto).get().addOnCompleteListener {
            if (it.isSuccessful&&it.result.exists()) {
                var cantidadDisponible = it.result.data?.get("cantidad").toString().toInt()
                println("Cantidad disponible" + it.result.data?.get("cantidad").toString())
                if (cantidadDisponible >= cantidad) {
                    CrearBaseDatos()
                } else {
                    Toast.makeText(
                        nuevaCantidad_ventas.context,
                        "Cantidad Insuficiente de este producto, tienes: $cantidadDisponible $producto",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun initCodeScan() {
        ScanOptions().apply {
            this.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            this.setPrompt("Presiona subir volumen para encender la linterna y bajar volumen para apagarla")
            this.setCameraId(0)
            this.setBeepEnabled(true)

            this.setBarcodeImageEnabled(true)

            barcodeLauncher.launch(this)
        }
    }

    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->

        if (result != null){
            if (result.contents == null){
                Toast.makeText(this,"Lectura cancelada",Toast.LENGTH_SHORT).show()
            }
            else{
                barCode = result.contents
                fireData.collection("db1").document(database).collection("Productos").whereEqualTo("barCode",barCode).get().addOnSuccessListener {
                    for(document in it.documents){
                        nuevaVenta_Name.text = Editable.Factory.getInstance().newEditable(document.data?.get("name").toString())
                        producto = nuevaVenta_Name.text.toString()
                    }
                    if(!it.isEmpty) {
                        nuevaVenta_Name.visibility = View.VISIBLE
                        spinner_productos.visibility = View.GONE
                        Toast.makeText(this,"Lectura exitosa: ${result.contents}",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        nuevaVenta_Name.visibility = View.GONE
                        spinner_productos.visibility = View.VISIBLE
                        Toast.makeText(this,"No fue encontrado ningun producto", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }else{
            Toast.makeText(this,"Ocurrio un error inesperado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        loadPreferences()
        readData()
        loadCalendar()
        barCode = ""
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
                    if(barCode != ""){
                        producto = nuevaVenta_Name.text.toString()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    if(barCode == ""){
                        Toast.makeText(nuevaVenta_Name.context,"Ingresa un producto primero",Toast.LENGTH_SHORT).show()
                    }
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

        println("Este es el producto: " + producto)

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
                println("Actual cantidad $actualCantidad")
                fireData.collection("db1").document(database).collection("Productos").document(producto).update("cantidad",actualCantidad)

            }

            updateFinanzas(dateToday)
            updateGanancias(dateToday)



            Intent(this,VentasHome::class.java).apply { startActivity(this) }
        }
        //Si la venta no se realizo
        else{
            Toast.makeText(this,"Verifique todos los campos esten completos",Toast.LENGTH_SHORT).show()
        }
        }

    fun updateFinanzas(dateToday: String) {
        var countDay = 0; var countMonth = 0; var countYear = 0;
        var dateMonth = "${myCalendar.time.year+1900}/${myCalendar.time.month + 1}"
        var dateYear = "${myCalendar.time.year+1900}"

        println("Paso por aqui")


        fireData.collection("db1").document(database)
            .collection("Finanzas").document(dateMonth+"/ventas").get().addOnCompleteListener {
            if(it.isSuccessful){
                countYear = nuevoPrecio_ventas.text.toString().toInt()* cantidad
                countMonth = nuevoPrecio_ventas.text.toString().toInt() * cantidad
                countDay = nuevoPrecio_ventas.text.toString().toInt() * cantidad

                /***Si es la primera vez en crear una venta***/
                if(!it.result.exists()){
                    println("NO se ha creado antes VENTAS")
                    /***AÑO****/
                    fireData.collection("db1").document(database).collection("Finanzas")
                        .document(dateYear).set(hashMapOf("ventas" to countYear,"comida" to 0), SetOptions.merge())

                    /****MES****/
                    fireData.collection("db1").document(database).collection("Finanzas")
                        .document(dateMonth+"/ventas").set(hashMapOf("ventas" to countMonth))
                    /****DIA*****/
                    fireData.collection("db1").document(database).collection("Finanzas")
                        .document(dateToday).set(hashMapOf("ventas" to countDay), SetOptions.merge())
                }
                /******* Venta normal ******/
                else{
                    println("Ya se ha creado antes VENTAS")
                    /***AÑO***/
                    fireData.collection("db1").document(database).collection("Finanzas")
                        .document(dateYear).update("ventas",FieldValue.increment(countYear.toLong()))

                    /****MES****/
                    fireData.collection("db1").document(database).collection("Finanzas")
                        .document(dateMonth+"/ventas").update("ventas",FieldValue.increment(countMonth.toLong()))
                    /****DIA****/
                    fireData.collection("db1").document(database).collection("Finanzas")
                        .document(dateToday).update("ventas",FieldValue.increment(countDay.toLong()))
                }
            }

            /*** Caso falle la conexion **/
            else{
                Toast.makeText(this,"Ocurrio un error en la conextion",Toast.LENGTH_SHORT).show()
            }
        }

    }


    fun updateGanancias(dateToday: String){
        var countDay = 0;
        var countMonth = 0;
        var countYear = 0;
        var precioProduccion = 0
        var dateMonth = "${myCalendar.time.year + 1900}/${myCalendar.time.month + 1}"
        var dateYear = "${myCalendar.time.year + 1900}"

        /*** Obtener precio de produccion del producto**/
        fireData.collection("db1").document(database)
            .collection("Productos").document(producto).get().addOnCompleteListener {
                if(it.isSuccessful){
                    if(it.result.exists()){
                        precioProduccion = it.result.data?.get("precio").toString().toInt()

                        /*** Actualizar las ganancias***/
                        fireData.collection("db1").document(database)
                            .collection("Finanzas").document(dateMonth+"/ganancias").get().addOnCompleteListener {
                                if (it.isSuccessful){
                                    /***Declaracion de variables***/
                                    countYear = (nuevoPrecio_ventas.text.toString().toInt() - precioProduccion) * cantidad
                                    countMonth = (nuevoPrecio_ventas.text.toString().toInt() - precioProduccion) * cantidad
                                    countDay = (nuevoPrecio_ventas.text.toString().toInt() - precioProduccion) * cantidad

                                    println(" Ganancia anual: $countYear Ganancia mensual: $countMonth Ganancia diaria: $countDay")

                                    /***Si es la primera vez en crear Ganancias***/
                                    if(!it.result.exists()){
                                        println("NO se ha creado antes Ganancias")
                                        /***AÑO***/
                                        fireData.collection("db1").document(database)
                                            .collection("Finanzas").document(dateYear).set(hashMapOf("ganancias" to countYear), SetOptions.merge())
                                        /***MES***/
                                        fireData.collection("db1").document(database)
                                            .collection("Finanzas").document(dateMonth+"/ganancias").set(hashMapOf("ganancias" to countMonth))
                                        /***DIA***/
                                        fireData.collection("db1").document(database).collection("Finanzas")
                                            .document(dateToday).set(hashMapOf("ganancias" to countDay), SetOptions.merge())
                                    }
                                    /*** Creacion de ganancias normales ***/
                                    else{
                                        println("Ya se ha creado antes Ganancias")
                                        /***AÑO**/
                                        fireData.collection("db1").document(database).collection("Finanzas").document(dateYear)
                            .update("ganancias",FieldValue.increment(countYear.toLong()))
                                        /***MES**/
                                        fireData.collection("db1").document(database)
                                            .collection("Finanzas").document(dateMonth+"/ganancias").update("ganancias",FieldValue.increment(countMonth.toLong()))
                                        /***DIA**/
                                        fireData.collection("db1").document(database)
                                            .collection("Finanzas").document(dateToday).update("ganancias",FieldValue.increment(countDay.toLong()))
                                    }
                                }

                            }
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