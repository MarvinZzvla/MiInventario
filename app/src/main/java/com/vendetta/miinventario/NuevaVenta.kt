package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_nueva_venta.*
import java.time.LocalDateTime


class NuevaVenta : AppCompatActivity() {
    var database = ""
    var producto = ""
    var date = ""
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_venta)

        crearNuevaVenta_btn.setOnClickListener {
            CrearBaseDatos()
        }

    }

    override fun onStart() {
        super.onStart()
        loadPreferences()
        readData()

         date = java.util.Calendar.getInstance().time.date.toString()+"/"+
                (java.util.Calendar.getInstance().time.month + 1).toString()+"/"+
                (java.util.Calendar.getInstance().time.year + 1900).toString()+" - "+
                java.util.Calendar.getInstance().time.hours.toString()+":"+java.util.Calendar.getInstance().time.minutes.toString()+":"+
                java.util.Calendar.getInstance().time.seconds.toString()


        nuevaFecha_ventas.text = Editable.Factory.getInstance().newEditable(date)


    }

    fun readData(){
        var listProductos = arrayListOf<String>()
        var adapter = ArrayAdapter(this, R.layout.spinner_style,listProductos)

        Firebase.database.getReference(database).child("Productos").get().addOnSuccessListener {
            for(child in it.children){
                listProductos.add(child.key.toString())
            }

            spinner_productos.adapter = adapter
            spinner_productos.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    producto = listProductos[p2]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    
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

    data class ventaData(val name:String,val date:String,val precio:Int)

    @RequiresApi(Build.VERSION_CODES.O)
    fun CrearBaseDatos(){
        var dateToday = "${LocalDateTime.now().year}/${LocalDateTime.now().monthValue}/${LocalDateTime.now().dayOfMonth}"
        var timeNow = "${LocalDateTime.now().hour.toString()}:${LocalDateTime.now().minute.toString()}:${LocalDateTime.now().second.toString()}"

        if(checkFields()) {
            Firebase.database.getReference(database).child("Ventas").child(dateToday).child(timeNow).setValue(
                ventaData(
                    producto,
                    nuevaFecha_ventas.text.toString(),
                    nuevoPrecio_ventas.text.toString().toInt()
                )
            )
            Intent(this,VentasHome::class.java).apply { startActivity(this) }
        }
        else{
            Toast.makeText(this,"Verifique todos los campos esten completos",Toast.LENGTH_SHORT).show()
        }
        }

    override fun onBackPressed() {
        super.onBackPressed()
        Intent(this,VentasHome::class.java).apply { startActivity(this) }
    }

    fun checkFields():Boolean{return producto.isNotEmpty() && nuevaFecha_ventas.text.toString().isNotEmpty() && nuevoPrecio_ventas.text.toString().isNotEmpty()}
}