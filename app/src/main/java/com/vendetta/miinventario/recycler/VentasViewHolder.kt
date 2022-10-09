package com.vendetta.miinventario.recycler

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vendetta.miinventario.R
import kotlinx.android.synthetic.main.activity_nueva_venta.*

class VentasViewHolder(view: View):RecyclerView.ViewHolder(view) {
    val fireData = Firebase.firestore

    var ventaName = view.findViewById<TextView>(R.id.item_title)
    var ventaPrecio = view.findViewById<TextView>(R.id.item_precio)
    var ventasDate = view.findViewById<TextView>(R.id.item_date)
    var ventasCantidad = view.findViewById<TextView>(R.id.item_cantidad)
    var deleteImage = view.findViewById<ImageButton>(R.id.deleteVentaBtn)


    fun render(ventas:Ventas,database:String){
        ventaName.text = ventas.venta_name
        ventaPrecio.text = (ventas.venta_precio.toInt() * ventas.venta_cantidad.toInt()).toString()
        ventasDate.text = ventas.venta_date
        if(ventas.venta_cantidad != "null")
        {
        ventasCantidad.text = ventas.venta_cantidad
        }else {ventasCantidad.text = "1"}




        deleteImage.setOnClickListener(object : View.OnClickListener{
            override fun onClick(view: View?) {
                deleteFromVentas(ventas,database)
                } //End of Click

        })
    }

    fun deleteFromVentas(ventas: Ventas, database: String) {
        var date = ventas.venta_date
        var dateToday = ""
        var timeVenta = ""
        var month = ""
        var year = ""

        //4/09/2022 - 2:57:33
        date.apply {
            this.split(" - ").apply {
                //2:57:33
                timeVenta = this[1]
                //4/09/2022
                this[0].apply {
                    //2022/09/4
                    this.split("/").apply {
                        dateToday = "${this[2]}/${this[1]}/${this[0]}"
                        year = this[2]
                        month = this[1]
                    }
                }
            }
        }

        val alertDialog: AlertDialog? = ventaName.context?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("Si",
                    DialogInterface.OnClickListener { dialog, id ->
                        fireData.collection("db1").document(database).collection("Ventas").document(database).collection(dateToday).document(timeVenta).delete()
                        updateFinanzasFire(database,dateToday,month,year,ventas)
                        updateGananciasFire(database,dateToday,month,year,ventas)
                    })
                setNegativeButton("No",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
                setTitle("Eliminar Venta")
                setMessage("¿Desea realmente eliminar esta venta?")
                show()
            }
            builder.create()
        }
        //END
    }

    fun updateGananciasFire(
        database: String,
        dateToday: String,
        month: String,
        year: String,
        ventas: Ventas
    ) {
        var countDay = 0; var countMonth = 0; var countYear = 0;
        var precioProduccion = 0
        var dateMonth = "${year}/${month}"
        var dateYear = "${year}"
        var cantidad = ventasCantidad.text.toString().toInt()

        //Obtener el precio produccion del producto
        fireData.collection("db1").document(database).collection("Productos").document(ventas.venta_name).get().addOnSuccessListener {
            precioProduccion = it.data?.get("precio").toString().toInt()
        }
        //Obtener ganancias y sumarles las nuevas ganancias del dia de hoy
        fireData.collection("db1").document(database).collection("Finanzas").document(dateToday).get().addOnSuccessListener {
            if(!it.exists()){countDay = (ventas.venta_precio.toString().toInt() + precioProduccion)*cantidad }
            else{ countDay = it.data?.get("ganancias").toString().toInt() - ((ventas.venta_precio.toString().toInt() - precioProduccion)*cantidad)}
            fireData.collection("db1").document(database).collection("Finanzas").document(dateToday).set(hashMapOf("ganancias" to countDay.toString()),
                SetOptions.merge())

            //Obtener las ganancias del mes y sumarles las nuevas ganancias del dia de hoy
            fireData.collection("db1").document(database).collection("Finanzas").document(dateMonth+"/ganancias").get().addOnSuccessListener {
                if(!it.exists()){countMonth = (ventas.venta_precio.toString().toInt() + precioProduccion)*cantidad}
                else{countMonth= it.data?.get("ganancias").toString().toInt() - ((ventas.venta_precio.toString().toInt() - precioProduccion)*cantidad)}
                fireData.collection("db1").document(database).collection("Finanzas").document(dateMonth+"/ganancias").set(hashMapOf("ganancias" to countMonth.toString()))

                fireData.collection("db1").document(database).collection("Finanzas").document(dateYear).get().addOnSuccessListener {
                    if(!it.exists()){countYear = (ventas.venta_precio.toString().toInt() + precioProduccion) * cantidad}
                    else{countYear = it.data?.get("ganancias").toString().toInt() - ((ventas.venta_precio.toString().toInt() - precioProduccion)*cantidad)}
                    println("Esta es la cantidad: " + countYear)
                    fireData.collection("db1").document(database).collection("Finanzas").document(dateYear).set(hashMapOf("ganancias" to countYear.toString()), SetOptions.merge())
                }
            }
        }
    }

    fun updateFinanzasFire(
        database: String,
        dateToday: String,
        month: String,
        year: String,
        ventas: Ventas
    ) {
        var countDay = 0; var countMonth = 0; var countYear = 0;
        var dateMonth = "${year}/${month}"
        var dateYear = "${year}"
        var cantidad = ventasCantidad.text.toString().toInt()

        //Actualizar la cantidad del producto vendido
        fireData.collection("db1").document(database).collection("Productos").document(ventas.venta_name).get().addOnSuccessListener {
            var count = it.data?.get("cantidad").toString().toInt()
            count += cantidad
            fireData.collection("db1").document(database).collection("Productos").document(ventas.venta_name).update("cantidad",count.toString())
        }

        //Get ventas del dia
        fireData.collection("db1").document(database).collection("Finanzas").document(dateToday).get().addOnSuccessListener {
            if(!it.exists()){ countDay = ventas.venta_precio.toInt()*cantidad }
            else{ countDay = it.data?.get("ventas").toString().toInt() - ventas.venta_precio.toInt() * cantidad }
            //Finanzas del dia -Set Ventas del dia
            fireData.collection("db1").document(database).collection("Finanzas").document(dateToday).set(
                hashMapOf("ventas" to countDay.toString()))

            //Get finanzas del mes
            fireData.collection("db1").document(database).collection("Finanzas").document(dateMonth+"/ventas").get().addOnSuccessListener {
                if(!it.exists()){countMonth = ventas.venta_precio.toInt() * cantidad}
                else{countMonth = it.data?.get("ventas").toString().toInt() - ventas.venta_precio.toInt() * cantidad}
                fireData.collection("db1").document(database).collection("Finanzas").document(dateMonth+"/ventas").set(
                    hashMapOf("ventas" to countMonth.toString()))

                //Get finanzas del año
                fireData.collection("db1").document(database).collection("Finanzas").document(dateYear).get().addOnSuccessListener {
                    if(!it.exists()){countYear = ventas.venta_precio.toInt()* cantidad}
                    else{ countYear = it.data?.get("ventas").toString().toInt() - (ventas.venta_precio.toInt() * cantidad)}
                    fireData.collection("db1").document(database).collection("Finanzas").document(dateYear).set(
                        hashMapOf("ventas" to countYear.toString()))

                }
            }
        }

    }


}