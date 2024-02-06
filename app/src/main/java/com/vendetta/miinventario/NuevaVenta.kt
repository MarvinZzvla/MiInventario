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

class NuevaVenta : AppCompatActivity() {

    lateinit var binding : ActivityNuevaVentaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevaVentaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAdapter()


        binding.scanFloatingBtn.setOnClickListener {
            initScanner()
        }

        binding.btnPagoVenta.setOnClickListener {
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

    private fun initAdapter() {
        //Llamar al adaptador personalizado
        val adapter = DropdownAdapter(this,VentaDropdownProvider.mutableList)
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
    }

}