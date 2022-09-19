package com.vendetta.miinventario.recycler

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vendetta.miinventario.R
import kotlinx.android.synthetic.main.activity_nueva_venta.*

class VentasViewHolder(view: View):RecyclerView.ViewHolder(view) {

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
                        Firebase.database.getReference(database).child("Ventas").child(dateToday).child(timeVenta).removeValue().addOnSuccessListener {
                            Toast.makeText(itemView.context,"Eliminado!",Toast.LENGTH_SHORT).show()
                        }
                        updateFinanzas(database,dateToday,month,year,ventas)
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

    fun updateFinanzas(
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

        //Update Cantidad
        //Actualizar la cantidad actual de producto vendido
        Firebase.database.getReference(database).child("Productos").child(ventas.venta_name).child("cantidad").get().addOnSuccessListener {
            var count = it.value.toString().toInt()
            count+=cantidad
            Firebase.database.getReference(database).child("Productos").child(ventas.venta_name).child("cantidad").setValue(count.toString())

        }

        //Get ventas del dia
        Firebase.database.getReference(database).child("Finanzas").child(dateToday)
            .child("ventas").get().addOnSuccessListener {
                if (!it.exists()){ countDay = ventas.venta_precio.toInt()*cantidad}
                else{
                    countDay = it.value.toString().toInt() - ventas.venta_precio.toString().toInt()*cantidad}
                //Finanzas del dia - Set ventas del dia
                Firebase.database.getReference(database).child("Finanzas").child(dateToday)
                    .child("ventas").setValue(countDay.toString())

                //Get Finanzas del mes
                Firebase.database.getReference(database).child("Finanzas").child(dateMonth).
                child("ventas").get().addOnSuccessListener {
                    if (!it.exists()){
                        countMonth = ventas.venta_precio.toString().toInt()*cantidad
                    }
                    else{
                        countMonth = it.value.toString().toInt() - ventas.venta_precio.toString().toInt()*cantidad
                    }
                    Firebase.database.getReference(database).child("Finanzas").child(dateMonth)
                        .child("ventas").setValue(countMonth.toString())

                    //Get Finanzas del año
                    Firebase.database.getReference(database).child("Finanzas").child(dateYear)
                        .child("ventas").get().addOnSuccessListener {
                            if(!it.exists()){
                                countYear = ventas.venta_precio.toString().toInt()*cantidad
                            }
                            else{
                                countYear = it.value.toString().toInt() - ventas.venta_precio.toString().toInt()*cantidad
                            }

                            Firebase.database.getReference(database).child("Finanzas").child(dateYear).
                            child("ventas").setValue(countYear)
                        }
                }
            }


    }
}