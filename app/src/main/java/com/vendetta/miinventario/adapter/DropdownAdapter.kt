package com.vendetta.miinventario.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import com.vendetta.miinventario.R
import com.vendetta.miinventario.data.VentaDropdown
import com.vendetta.miinventario.databinding.DropdownItemBinding
import java.lang.Integer.min

class DropdownAdapter(context: Context, private val elementos: List<VentaDropdown>) :
    ArrayAdapter<VentaDropdown>(context, 0, elementos) {

    private val elementosOriginales = ArrayList(elementos)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.dropdown_item, parent, false)

        val binding = DropdownItemBinding.bind(view)
        val elemento = elementos[position]
        val name = binding.dropdownNombreText
        val cantidad = binding.dropdownCantidadText

        name.text = elemento.productoName
        cantidad.text = "Disponibles: ${elemento.cantidad}"

        //Cambiar el color depende de la disponibilidad si quedan menos de 10 en amarillo si quedan menos de 5 en rojo
        if (elemento.cantidad.toInt() <= 10) {
            cantidad.setTextColor(Color.parseColor("#F5C842"))
            if (elemento.cantidad.toInt() <= 5) {
                cantidad.setTextColor(Color.parseColor("#F54242"))
            }
        } else {
            cantidad.setTextColor(Color.parseColor("#1FFB18"))
        }

        return view
    }

    override fun getCount(): Int {
        //Limitar dropdown
        return min(5, super.getCount())
        //return super.getCount()
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()


                if ((!constraint.isNullOrEmpty())) {
                    val suggestions = ArrayList<VentaDropdown>()
                    for (elemento in elementosOriginales) {
                        // Cambia "startsWith" por "contains"
                        if (elemento.productoName.toLowerCase()
                                .contains(constraint.toString().toLowerCase())
                        ) {
                            suggestions.add(elemento)
                        }
                    }
                    filterResults.values = suggestions
                    filterResults.count = suggestions.size
                } else {
                    filterResults.values = elementosOriginales
                    filterResults.count = elementosOriginales.size
                }

                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                clear()
                if (results != null && results.count > 0) {
                    addAll(results.values as List<VentaDropdown>)
                } else {
                    addAll(elementosOriginales)
                }
                notifyDataSetChanged()
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as VentaDropdown).productoName
            }

        }
    }

}