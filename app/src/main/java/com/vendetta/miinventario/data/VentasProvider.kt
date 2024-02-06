package com.vendetta.miinventario.data

import java.util.Date

class VentasProvider {
    companion object{
        val ventaList = listOf<Ventas>(
            Ventas(
                Date(),
                1,
                arrayListOf("Camiseta","Pantalon"),
                486.00f,
                120.00f),
            Ventas(
                Date(),
                2,
                arrayListOf("Licra","Pantalon"),
                200.00f,
                120.00f),
            Ventas(
                Date(),
                3,
                arrayListOf("Short","Pantalon"),
                220.00f,
                120.00f),
            Ventas(
                Date(),
                4,
                arrayListOf("Sudadera","Pantalon"),
                6000.00f,
                120.00f),
            Ventas(
                Date(),
                5,
                arrayListOf("Sueter","Pantalon"),
                90.00f,
                70.00f),
        )
    }
}