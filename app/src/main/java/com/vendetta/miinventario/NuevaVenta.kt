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
import com.vendetta.miinventario.data.structures.NuevaVentaDatos
import com.vendetta.miinventario.databinding.ActivityNuevaVentaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NuevaVenta : AppCompatActivity() {

    lateinit var binding : ActivityNuevaVentaBinding
    var totalPrecio = 0.0f
    var totalGanancia = 0.0f
    var arrayVenta = arrayListOf<NuevaVentaDatos>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevaVentaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecycleViewNuevaVenta()

        lifecycleScope.launch(Dispatchers.Main) {initAdapter()}


        binding.scanFloatingBtn.setOnClickListener {
            initScanner()
        }

        binding.btnPagoVenta.setOnClickListener {
            Intent(this,FacturaPage::class.java).apply { startActivity(this) }
        }

        binding.readyBtn.setOnClickListener {
            saveTmpProducto()
        }


    }



    private fun initScanner() {
        val options = GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).allowManualInput().enableAutoZoom().build()
        val scanner = GmsBarcodeScanning.getClient(this, options)

        scanner.startScan().addOnSuccessListener { barcode ->
            val rawValue: String? = barcode.rawValue
            println(rawValue)
        }.addOnCanceledListener {
            //Task cancelled
        }.addOnFailureListener {
            //Task Failed
        }

    }

    private suspend fun initAdapter() {
        //Llamar al adaptador personalizado
        val adapter = DropdownAdapter(this,VentaDropdownProvider().getProductos(applicationContext))
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
        autoComplete.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.adapter.getItem(position) as VentaDropdown
            loadInfo(selectedItem)

        }
    }

    private fun loadInfo(selectedItem: VentaDropdown) {
        binding.nuevaVentaNameProductoText.setText(selectedItem.productoName)
        binding.nuevaVentaCantidadText.setText("1")
        binding.nuevaVentaPrecioText.setText(selectedItem.precioVenta.toString())
        binding.nuevaVentaProfitText.setText(selectedItem.precio.toString())
        binding.nuevaVentaIDText.setText(selectedItem.idProducto.toString())
    }

    @SuppressLint("SetTextI18n")
    private fun saveTmpProducto() {
        var id =  binding.nuevaVentaIDText.text.toString().toInt()
        var name = binding.nuevaVentaNameProductoText.text.toString()
        var cantidad = binding.nuevaVentaCantidadText.text.toString().toInt()
        var precio = binding.nuevaVentaPrecioText.text.toString().toFloat()
        var precio_venta = binding.nuevaVentaProfitText.text.toString().toFloat()
        var totalprecioTmp = precio * cantidad
        var totalprofitTmp = (precio - precio_venta) * cantidad
        totalPrecio += precio * cantidad
        totalGanancia += (precio - precio_venta) * cantidad
        val datosTmp = NuevaVentaDatos(id,name,cantidad,totalprecioTmp,totalprofitTmp)
        arrayVenta.add(datosTmp)


        binding.nuevaVentaNameProductoText.text.clear()
        binding.nuevaVentaCantidadText.text.clear()
        binding.nuevaVentaPrecioText.text.clear()
        binding.autoCompleteText.text.clear()
        binding.nuevaVentaTotalText.setText("Total Gastado: \n$${totalPrecio}")

        binding.recycleViewNuevaVenta.adapter = NuevaVentaAdapter(arrayVenta)
    }

    private fun initRecycleViewNuevaVenta() {
       val recyclerView = binding.recycleViewNuevaVenta
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = NuevaVentaAdapter(arrayVenta)
    }



}