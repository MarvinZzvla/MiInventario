package com.vendetta.miinventario

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.vendetta.miinventario.adapter.DropdownAdapter
import com.vendetta.miinventario.data.Productos
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

        var isEdit = intent.getBooleanExtra("isEdit",false)
        if(isEdit){
           initEditProducto()
        }

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
            var id = binding.nuevoProductoIDText.text.toString()
            if(binding.nuevoProductoBarcodeText.text.isNullOrEmpty()){
                barcode = "0"
            }

            //Chequear si los campos no estan vacios
            if(verifyFields()) {
                //Guardar producto
                if(isEdit){
                    updateProducto(name, cantidad, precio, precio_venta, barcode,id)
                }
                else{
                    saveProducto(name, cantidad, precio, precio_venta, barcode)
                }

            }
            else{
                //Mostrar mensaje de Completar informacion
                Toast.makeText(this,"Rellena todos los campos",Toast.LENGTH_SHORT).show()
            }

        }


    }

    /******************************************************
     * Init Edit Producto
     * Load all Configurations if the product is being editing
     *************************************************************/
    private fun initEditProducto() {
        binding.btnDeleteProducto.visibility = View.VISIBLE
        var producto = intent.getSerializableExtra("arrayProducto") as Productos
        loadValues(producto)
        binding.btnDeleteProducto.setOnClickListener {
            deleteProducto(producto)
        }

    }


    /************************************************************
     * Load Values
     * Is product is editing display producto information
     ************************************************************/
    private fun loadValues(producto: Productos?) {
        //Disable editText Producto name
        //binding.nuevoProductoNameProductoText.isEnabled = false
        //Editar productos
        binding.btnSubmitProducto.setText("Editar Producto")
        //Load Values into Editables
        binding.nuevoProductoIDText.setText(producto?.id.toString())
        binding.nuevoProductoNameProductoText.setText( producto?.productoName)
         binding.nuevoProductoCantidadText.setText( producto?.cantidad.toString())
         binding.nuevoProductoPrecioText.setText( producto?.precio_costo.toString())
         binding.nuevoProductoPrecioVentaText.setText(producto?.precio_venta.toString())
        binding.nuevoProductoBarcodeText.setText( producto?.barCode)
        binding.headingProductosText.text = "Editar producto: ${producto?.productoName}"
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
     * Update Producto
     * Esta funcion sirve para actualizar los datos obtenidos
     * En la base de datos
     ********************************************************/
    private fun updateProducto(name: String,
                               cantidad: String,
                               precio: String,
                               precioVenta: String,
                               barcode: String,
                               id:String) {
        // Theread IO
        lifecycleScope.launch(Dispatchers.IO){
            //Create a producto entity to update it into database, data is getting from editables
            val producto = ProductosEntity(
                ID = id.toInt(),
                Name = name,
                Available = cantidad.toInt(),
                Price = precio.toFloat(),
                Price_Sell =  precioVenta.toFloat(),
                BarCode = barcode)
            //Save result
            val result = database.productosDao.update(producto)
            //Using the Thread Main to display results and Toasts
            withContext(Dispatchers.Main) {
                if (result > 0) {
                    //Desplegar con exito
                    Toast.makeText(applicationContext, "Actualizado con exito", Toast.LENGTH_SHORT)
                        .show()
                    Intent(applicationContext, HomePage::class.java).apply { startActivity(this) }
                } else {
                    //Failed
                    Toast.makeText(applicationContext, "Ocurrio un error", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }
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

    /************************************************************************
     * Delete producto
     * Delete the producto selected from databse
     * Display confirmation dialog
     *******************************************************************/
    private fun deleteProducto(producto: Productos) {
        //Create a producto entity to delete the product selected from database
        //The data is get from recycleView en HomePage
        val productoEntity = ProductosEntity(
            producto.id,
            producto.productoName,
            producto.cantidad,
            producto.precio_costo,
            producto.precio_venta,
            producto.barCode
        )
        //Create Dialog to confirm delete option
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar ${producto.productoName}")
        builder.setMessage("¿Estás seguro de eliminar permanentemente ${producto.productoName}?")

        // Configurar el botón de "Sí"
        builder.setPositiveButton("Sí") { dialog, which ->
            // Acciones a realizar cuando el usuario presiona "Sí"
            lifecycleScope.launch(Dispatchers.IO) {
                database.productosDao.delete(productoEntity)
                //Change to Main Thread to display results into a toast
                withContext(Dispatchers.Main){
                    Toast.makeText(applicationContext, "Borrado con exito", Toast.LENGTH_SHORT)
                        .show()
                    Intent(applicationContext, HomePage::class.java).apply { startActivity(this) }
                }
            }
        }

        // Configurar el botón de "No"
        builder.setNegativeButton("No") { dialog, which ->
            // Acciones a realizar cuando el usuario presiona "No"
        }

        // Mostrar el diálogo
        builder.show()
    }

    /**********************************************************************
     * Init Scanner
     * Initialize BarCodeScanner
     ***********************************************************************/
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