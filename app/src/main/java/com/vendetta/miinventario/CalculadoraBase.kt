package com.vendetta.miinventario

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_calculadora_base.*

class CalculadoraBase : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculadora_base)
        banner_calcular.loadAd(AdRequest.Builder().build())

        calcular_btn.setOnClickListener {
            if(checkCamps()){
                calcularPrecioUnidad()
            }
            else{Toast.makeText(this,"Por favor completa todos los campos", Toast.LENGTH_SHORT).show()}

        }

        back_btn.setOnClickListener {
            Intent(this,NuevoProducto::class.java).apply { startActivity(this) }
        }
    }

    fun calcularPrecioUnidad(){
        var gastosTotales = gastosTotalesCalcular.text.toString().toInt()
        var precioPublico = precioPublicoCalcular.text.toString().toInt()
        var cantidadTotal = cantidadTotalCalcular.text.toString().toInt()

        if(cantidadTotal == 0){Toast.makeText(this,"La cantidad total no puede ser 0",Toast.LENGTH_SHORT).show();return}


        var precioUnidad = (gastosTotales / cantidadTotal)
        precioUnidadCalcular.text = precioUnidad.toString()

        gananciaUnidadCalcular.text = (precioPublico - precioUnidad ).toString()


    }

    fun checkCamps():Boolean{
        return gastosTotalesCalcular.text.isNotEmpty() && precioPublicoCalcular.text.isNotEmpty()&&cantidadTotalCalcular.text.isNotEmpty()
    }
}