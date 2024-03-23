package com.vendetta.miinventario

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.vendetta.miinventario.adapter.DropdownAdapter
import com.vendetta.miinventario.adapter.NuevaVentaAdapter
import com.vendetta.miinventario.data.VentaDropdown
import com.vendetta.miinventario.data.VentaDropdownProvider
import com.vendetta.miinventario.data.database.InventarioDatabase
import com.vendetta.miinventario.data.database.InventarioDatabase.Companion.getDatabase
import com.vendetta.miinventario.data.database.entities.FinanzasEntity
import com.vendetta.miinventario.data.database.entities.VentasEntity
import com.vendetta.miinventario.data.structures.NuevaVentaDatos
import com.vendetta.miinventario.databinding.ActivityNuevaVentaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.Format
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NuevaVenta : AppCompatActivity() {

    //Declare variables
    lateinit var binding: ActivityNuevaVentaBinding //GET Layout
    lateinit var database: InventarioDatabase //GET Database
    lateinit var listOfProductos: ArrayList<VentaDropdown> //GET list of products
    lateinit var builder: AlertDialog.Builder
    var totalPrecio = 0.0f
    var totalGanancia = 0.0f
    var arrayVenta = arrayListOf<NuevaVentaDatos>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevaVentaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecycleViewNuevaVenta()

        builder = AlertDialog.Builder(this)
        //Get database Instance or create new one
        database = getDatabase(this)
        //Use Coroutine to initialize the AutoComplete text
        lifecycleScope.launch(Dispatchers.Main) { initAdapter() }

        //Initialize Barcode Scanner
        binding.scanFloatingBtn.setOnClickListener {
            initScanner()
        }
        //Procesesar pago buton
        binding.btnPagoVenta.setOnClickListener {
            displayDialog()
        }

        //Add product to recycleview or cart shop
        binding.readyBtn.setOnClickListener {
            addToRecycle()
        }
        binding.btnAddtoRecycle.setOnClickListener {
            addToRecycle()
        }
    }

    /*************************************************************
     * Init Scanner
     * Initialize the barcode scanner
     * Search barcode into list of products in the database
     **********************************************************/
    private fun initScanner() {
        val options =
            GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .allowManualInput().enableAutoZoom().build()
        val scanner = GmsBarcodeScanning.getClient(this, options)

        scanner.startScan().addOnSuccessListener { barcode ->
            //Get barcode
            val rawValue: String? = barcode.rawValue
            //Find the product using barcode
            for (elemento in listOfProductos) {
                if (elemento.barcode == rawValue) {
                    //Print result into RecycleView
                    loadInfo(elemento)
                }

            }

        }.addOnCanceledListener {
            //Task cancelled
        }.addOnFailureListener {
            //Task Failed
        }

    }

    /***********************************************
     * initRecycleViewNuevaVenta
     **********************************************/
    private fun initRecycleViewNuevaVenta() {
        val recyclerView = binding.recycleViewNuevaVenta
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = NuevaVentaAdapter(arrayVenta)
    }

    /************************************************************************************
     * Init Adapter
     * Initialize the adapter for autocompleteTextView
     ************************************************************************************/
    private suspend fun initAdapter() {
        listOfProductos = VentaDropdownProvider().getProductos(applicationContext)
        //Llamar al adaptador personalizado
        val adapter = DropdownAdapter(this, listOfProductos)
        val autoComplete = binding.autoCompleteText //Autocomplete

        autoComplete.setAdapter(adapter)
        autoComplete.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                autoComplete.showDropDown()
            }
        }

        autoComplete.setOnClickListener {
            autoComplete.showDropDown()
        }
        autoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.adapter.getItem(position) as VentaDropdown
                loadInfo(selectedItem)

            }
    }
    /************************************************
     * AddtoRecycle
     * Verify Editables and Save tmp producto
     **************************************************/
    private fun addToRecycle() {
        if (verifyFields()) {
            //Save product into recycleview
            saveTmpProducto()
        } else {
            Toast.makeText(this, "Por favor rellene todos los campos", Toast.LENGTH_SHORT)
                .show()
        }
    }

    /******************************************************************************
     * Load Info
     * Obtain the product info and display it into EditText
     ******************************************************************************/
    private fun loadInfo(selectedItem: VentaDropdown) {
        binding.nuevaVentaNameProductoText.setText(selectedItem.productoName)
        binding.nuevaVentaCantidadText.setText("1")
        binding.nuevaVentaPrecioText.setText(selectedItem.precioVenta.toString())
        binding.nuevaVentaProfitText.setText(selectedItem.precio.toString())
        binding.nuevaVentaIDText.setText(selectedItem.idProducto.toString())
    }

    @SuppressLint("SetTextI18n")
    /*********************************************************************************
     * Save Tmp Producto
     * Get data from editables and add into arrayList (RecycleView)
     * Display the data into recycleView
     *********************************************************************************/
    private fun saveTmpProducto() {
        //Get Editables
        var id = binding.nuevaVentaIDText.text.toString().toInt()
        var name = binding.nuevaVentaNameProductoText.text.toString()
        var cantidad = binding.nuevaVentaCantidadText.text.toString().toInt()
        var precio = binding.nuevaVentaPrecioText.text.toString().toFloat()
        var precio_venta = binding.nuevaVentaProfitText.text.toString().toFloat()
        var totalprecioTmp = precio * cantidad // Get the total price
        var totalprofitTmp = (precio - precio_venta) * cantidad //Get the total profit
        totalPrecio += precio * cantidad //Add to current total price the new price
        totalGanancia += (precio - precio_venta) * cantidad //Add to current total profit the new profit
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var date = formato.format(Date())
        //Add producto into array
        val datosTmp = NuevaVentaDatos(id, name, cantidad, totalprecioTmp, totalprofitTmp,date)
        arrayVenta.add(datosTmp)

        //Reset editables
        binding.nuevaVentaNameProductoText.text.clear()
        binding.nuevaVentaCantidadText.text.clear()
        binding.nuevaVentaPrecioText.text.clear()
        binding.autoCompleteText.text.clear()
        //Update the total
        binding.nuevaVentaTotalText.setText("Total Gastado: \n$${totalPrecio}")

        //Add product into Shop Cart
        binding.recycleViewNuevaVenta.adapter = NuevaVentaAdapter(arrayVenta)
    }

    /***********************************************************************************
     * Save Venta Database
     * When you finished the shop cart, process the payment
     ***********************************************************************************/
    private suspend fun saveVentaDatabase() {
        //Get the actual date and format it
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var date = formato.format(Date())
        //Get the last ID
        val idFactura = database.ventasDao.getMaxIdFactura() ?: 0
        //Get the product and transform it into VentasEntity
        //saveVentaFinanzas(arrayVenta)
        for (venta in arrayVenta) {
            //INSERT VENTAS IN DATABASE
            val ventas = VentasEntity(
                productos = venta.name,
                FK_Producto = venta.id.toString(), Date = date,
                Quantity = venta.cantidad, Price = venta.precio_total,
                Profit = venta.precio_sell_total, ID_Factura = (idFactura + 1)
            )
            //INSERT FINANZAS IN THE DATABASE
            val finanzas = FinanzasEntity(
                Date = date, Total =  venta.precio_total,
                TotalGanancias = venta.precio_sell_total,
                FK_Venta = (idFactura + 1)
            )
            //Insert the product in Ventas TABLE
            database.ventasDao.insertAll(ventas)
            //Insert product finanzas in FINANZAS TABLE
            database.finanzasDao.insert(finanzas)
        }
        val arrayVentaTmp = ArrayList(arrayVenta)
        //Clear
        arrayVenta.clear()
        //Update recycle
        binding.recycleViewNuevaVenta.adapter = NuevaVentaAdapter(arrayVenta)
        //Start Receipt Screen
        Intent(this, FacturaPage::class.java).apply {
            putExtra("arrayVenta",arrayVentaTmp)
            putExtra("factura_number",idFactura + 1)
            putExtra("totalPrice",totalPrecio)
            startActivity(this)
        }
    }

    private fun saveVentaFinanzas(arrayVenta: ArrayList<NuevaVentaDatos>) {

    }


    /**********************************************************************************
     * Verify Fields
     * Check Editables are not empty or null
     **********************************************************************************/
    private fun verifyFields(): Boolean {
        val name = binding.nuevaVentaNameProductoText.text
        val cantidad = binding.nuevaVentaCantidadText.text
        val precio = binding.nuevaVentaPrecioText.text

        return !(name.isNullOrBlank() && cantidad.isNullOrEmpty() && precio.isNullOrEmpty())
    }

    private fun displayDialog() {
        builder.setTitle("Procesar Pago")
        builder.setMessage("¿Estás seguro?")

        // Configurar el botón de "Sí"
        builder.setPositiveButton("Sí") { dialog, which ->
            // Acciones a realizar cuando el usuario presiona "Sí"
            lifecycleScope.launch(Dispatchers.Main) { saveVentaDatabase() }
        }

        // Configurar el botón de "No"
        builder.setNegativeButton("No") { dialog, which ->
            // Acciones a realizar cuando el usuario presiona "No"
        }

        // Mostrar el diálogo
        builder.show()
    }


}