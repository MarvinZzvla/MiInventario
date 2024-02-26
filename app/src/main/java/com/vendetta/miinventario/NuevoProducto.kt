package com.vendetta.miinventario

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.vendetta.miinventario.adapter.DropdownAdapter
import com.vendetta.miinventario.data.VentaDropdownProvider
import com.vendetta.miinventario.databinding.ActivityNuevaVentaBinding
import com.vendetta.miinventario.databinding.ActivityNuevoProductoBinding

class NuevoProducto : AppCompatActivity() {

    lateinit var binding : ActivityNuevoProductoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.scanFloatingProductoBtn.setOnClickListener {
            initScanner()
        }

        binding.btnSubmitProducto.setOnClickListener {
            Intent(this,FacturaPage::class.java).apply { startActivity(this) }
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


}