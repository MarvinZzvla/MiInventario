package com.vendetta.miinventario.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R
import com.vendetta.miinventario.usuariosProviderList

class UsuarioAdapter(val usuariosList:ArrayList<Usuarios>,val database:String): RecyclerView.Adapter<UsuarioViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UsuarioViewHolder(layoutInflater.inflate(R.layout.usuario_card_layout,parent,false))
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        var item = usuariosProviderList[position]
        holder.render(item,database)
    }

    override fun getItemCount(): Int {
        return usuariosList.size
    }
}