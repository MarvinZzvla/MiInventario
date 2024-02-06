package com.vendetta.miinventario.data

class FacturaProvider{
    companion object{
        val facturaList = listOf<Factura>(
            Factura("Camiseta",20,100.0f),
            Factura("Pantalon",10,150.0f),
            Factura("Camisola",10,150.0f)
        )
    }
}