package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.*
import com.vendetta.miinventario.recycler.Productos
import kotlinx.android.synthetic.main.activity_nuevo_producto.*
import java.util.*


private lateinit var auth: FirebaseAuth
private val fireData = Firebase.firestore
private var database = ""
private var barCode = ""
class NuevoProducto : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_producto)
        auth = Firebase.auth
        loadPreferences()
        banner_nuevoProducto.loadAd(AdRequest.Builder().build())



        crearNuevoProducto_btn.setOnClickListener {
            if (checkFields()){
                createProducto()
            }else{
                Toast.makeText(this,"Completa porfavor todos los campos",Toast.LENGTH_SHORT).show()
            }

        }

        calBasePrecio.setOnClickListener {
            Intent(this,CalculadoraBase::class.java).apply { startActivity(this) }
        }

        scanText.setOnClickListener {
         initCodeScan()

            }

        barCode_btn.setOnClickListener {

            initCodeScan()
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
                Toast.makeText(this,"Lectura exitosa: ${result.contents}",Toast.LENGTH_SHORT).show()
                barCode = result.contents
                nuevoBarCode.text = Editable.Factory.getInstance().newEditable(barCode)

            }
        }else{
            Toast.makeText(this,"Ocurrio un error inesperado", Toast.LENGTH_SHORT).show()
        }
    }


    fun createProducto(){
        fireData.collection("db1").document(database).collection("Productos").document(nuevoNameProducto.text.toString()).set(Productos(
            nuevoNameProducto.text.toString(),nuevoPrecio_producto.text.toString(),
            nuevaCantidadProducto.text.toString(),barCode
        ))
        Intent(this,ProductosHome::class.java).apply { startActivity(this) }
    }

    fun checkFields():Boolean{
        return nuevoNameProducto.text.isNotEmpty() && nuevaCantidadProducto.text.isNotEmpty() && nuevoPrecio_producto.text.isNotEmpty()
    }

    fun loadPreferences(){
        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            database = this.getString("database","null").toString()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Intent(this,ProductosHome::class.java).apply { startActivity(this) }
    }
}