package com.vendetta.miinventario

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.vendetta.miinventario.adapter.DropdownAdapter
import com.vendetta.miinventario.data.VentaDropdownProvider
import com.vendetta.miinventario.data.database.InventarioDatabase
import com.vendetta.miinventario.data.database.InventarioDatabase.Companion.getDatabase
import com.vendetta.miinventario.data.database.entities.ProductosEntity
import com.vendetta.miinventario.databinding.ActivityNuevaVentaBinding
import com.vendetta.miinventario.databinding.ActivityNuevoProductoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevoProducto : AppCompatActivity() {

    lateinit var binding : ActivityNuevoProductoBinding
    private lateinit var database: InventarioDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Initialize databse
        database = getDatabase(this)

        binding.scanFloatingProductoBtn.setOnClickListener {
            initScanner()
        }


        //Boton de crear venta
        binding.btnSubmitProducto.setOnClickListener {
            //Obtener los valores de los campos
            var name = binding.nuevoProductoNameProductoText.text.toString()
            var cantidad = binding.nuevoProductoCantidadText.text.toString()
            var precio = binding.nuevoProductoPrecioText.text.toString()
            var precio_venta = binding.nuevoProductoPrecioVentaText.text.toString()
            var barcode = binding.nuevoProductoBarcodeText.text.toString()
            if(binding.nuevoProductoBarcodeText.text.isNullOrEmpty()){
                barcode = "0"
            }


            //Chequear si los campos no estan vacios
            if(verifyFields()) {
                //Guardar producto
                saveProducto(name, cantidad, precio, precio_venta, barcode)
            }
            else{
                //Mostrar mensaje de Completar informacion
                Toast.makeText(this,"Rellena todos los campos",Toast.LENGTH_SHORT).show()
            }

        }


    }

    /****************************************************************************
     * Verify Fields
     * Esta funcion es para verificar que los campos del formulario no esten vacios
     *******************************************************************************/
    private fun verifyFields(): Boolean {
    return !(binding.nuevoProductoNameProductoText.text.isNullOrEmpty() &&
            binding.nuevoProductoCantidadText.text.isNullOrEmpty() &&
            binding.nuevoProductoPrecioText.text.isNullOrEmpty() &&
            binding.nuevoProductoPrecioVentaText.text.isNullOrEmpty())
    }

    /***************************************************************
     * Save Producto
     * Esta funcion sirve para guardar los datos obtenidos
     * En la base de datos
     ********************************************************/
    private fun saveProducto(name: String,
                             cantidad: String,
                             precio: String,
                             precioVenta: String,
                             barcode: String)
    {

        //Declared Variables
        val producto = ProductosEntity(Name = name,
            Available = cantidad.toInt(),
            Price = precio.toFloat(),
            Price_Sell =  precioVenta.toFloat(),
            BarCode = barcode)


        //Call Coroutine
        lifecycleScope.launch(Dispatchers.IO) {
            //Insert
            val id = database.productosDao.insertAll(producto)
            //Back to Main Scope
            withContext(Dispatchers.Main){
                //Display a successful message
                if(id == -1L){
                    //Failed
                    Toast.makeText(applicationContext,"Fallo en el registro",Toast.LENGTH_SHORT).show()
                }
                else{
                    //Desplegar con exito
                Toast.makeText(applicationContext,"Registrado con exito",Toast.LENGTH_SHORT).show()
                    Intent(applicationContext,HomePage::class.java).apply { startActivity(this) }
                }

            }
        }
    }

    private fun initScanner() {
        val options = GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).allowManualInput().enableAutoZoom().build()
        val scanner = GmsBarcodeScanning.getClient(this, options)

        scanner.startScan().addOnSuccessListener { barcode ->
            val rawValue: String? = barcode.rawValue
            println(rawValue)
            binding.nuevoProductoBarcodeText.setText(rawValue)
        }.addOnCanceledListener {
            //Task cancelled
        }.addOnFailureListener {
            //Task Failed
            Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
        }

    }


}