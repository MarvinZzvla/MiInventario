package com.vendetta.miinventario.data

class VentaDropdownProvider {
    companion object{
        val dropdownList = listOf<VentaDropdown>(
            VentaDropdown("Camiseta","10"),
            VentaDropdown("Pantalon","5"),
            VentaDropdown("Licra","10"),
            VentaDropdown("Short","50"),
            VentaDropdown("Camisola","5"),
            VentaDropdown("Sudadera","80")
        )
        val mutableList = ArrayList(dropdownList)
    }
}