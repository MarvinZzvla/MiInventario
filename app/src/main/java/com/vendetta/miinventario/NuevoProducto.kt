package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevoProducto : AppCompatActivity() {

    lateinit var binding : ActivityNuevoProductoBinding
    private lateinit var database: InventarioDatabase
    private var mInterstitialAd: InterstitialAd? = null
    private var TAG = "NuevoProducto"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdgeWithInsets(binding.root)
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
            showAds()
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

            if (!verifyFields()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isEdit) {
                updateProducto(name, cantidad, precio, precio_venta, barcode, id)
            } else {
                saveProducto(name, cantidad, precio, precio_venta, barcode)
            }

        }

    }

    private fun initAds() {
        val localStorage = getSharedPreferences("ads_data", Context.MODE_PRIVATE)
        val isTesting = localStorage.getBoolean("isAdsEnable",false)
        if(isTesting){
            lifecycleScope.launch(Dispatchers.Main) {
                delay(5000L)
                prepareAds() }
        }

    }
    private fun prepareAds() {
        var adRequest = AdRequest.Builder().build()
        //ca-app-pub-2467116940009132/5486356001
        InterstitialAd.load(
            this,BuildConfig.AD_ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    showAds()
                }
            })

    }
    private fun showAds(){
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
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
        return binding.nuevoProductoNameProductoText.text?.isNotBlank() == true &&
            binding.nuevoProductoCantidadText.text?.isNotBlank() == true &&
            binding.nuevoProductoPrecioText.text?.isNotBlank() == true &&
            binding.nuevoProductoPrecioVentaText.text?.isNotBlank() == true
    }

    private fun parseIntOrNull(raw: String): Int? =
        raw.trim().replace(',', '.').toIntOrNull()
            ?: raw.trim().replace(',', '.').toFloatOrNull()?.toInt()

    private fun parseFloatOrNull(raw: String): Float? =
        raw.trim().replace(',', '.').toFloatOrNull()

    private fun buildProductoEntity(
        name: String,
        cantidad: String,
        precio: String,
        precioVenta: String,
        barcode: String,
        id: Int? = null
    ): ProductosEntity? {
        val available = parseIntOrNull(cantidad)
        val price = parseFloatOrNull(precio)
        val priceSell = parseFloatOrNull(precioVenta)
        if (available == null || price == null || priceSell == null) {
            Toast.makeText(
                this,
                "Cantidad y precios deben ser números válidos",
                Toast.LENGTH_SHORT
            ).show()
            return null
        }
        return if (id != null) {
            ProductosEntity(
                ID = id,
                Name = name.trim(),
                Available = available,
                Price = price,
                Price_Sell = priceSell,
                BarCode = barcode
            )
        } else {
            ProductosEntity(
                Name = name.trim(),
                Available = available,
                Price = price,
                Price_Sell = priceSell,
                BarCode = barcode
            )
        }
    }

    /***************************************************************
     * Update Producto
     * Esta funcion sirve para actualizar los datos obtenidos
     * En la base de datos
     ********************************************************/
    private fun updateProducto(
        name: String,
        cantidad: String,
        precio: String,
        precioVenta: String,
        barcode: String,
        id: String
    ) {
        val productId = parseIntOrNull(id)
        if (productId == null) {
            Toast.makeText(this, "ID de producto inválido", Toast.LENGTH_SHORT).show()
            return
        }
        val producto = buildProductoEntity(name, cantidad, precio, precioVenta, barcode, productId)
            ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            val result = database.productosDao.update(producto)
            withContext(Dispatchers.Main) {
                if (result > 0) {
                    Toast.makeText(applicationContext, "Actualizado con exito", Toast.LENGTH_SHORT)
                        .show()
                    Intent(applicationContext, HomePage::class.java).apply { startActivity(this) }
                } else {
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
    private fun saveProducto(
        name: String,
        cantidad: String,
        precio: String,
        precioVenta: String,
        barcode: String
    ) {
        val producto = buildProductoEntity(name, cantidad, precio, precioVenta, barcode) ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            val id = database.productosDao.insertAll(producto)
            withContext(Dispatchers.Main) {
                if (id == -1L) {
                    Toast.makeText(applicationContext, "Fallo en el registro", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(applicationContext, "Registrado con exito", Toast.LENGTH_SHORT)
                        .show()
                    Intent(applicationContext, HomePage::class.java).apply { startActivity(this) }
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
            binding.nuevoProductoBarcodeText.setText(rawValue)
        }.addOnCanceledListener {
            //Task cancelled
        }.addOnFailureListener {
            //Task Failed
            Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
        }

    }


}