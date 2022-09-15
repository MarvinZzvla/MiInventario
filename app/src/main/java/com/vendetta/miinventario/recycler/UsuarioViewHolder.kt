package com.vendetta.miinventario.recycler

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vendetta.miinventario.R

class UsuarioViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private lateinit var auth:FirebaseAuth
    var usuarioName = view.findViewById<TextView>(R.id.usuario_item_title)
    var usuarioEmail = view.findViewById<TextView>(R.id.usuario_item_email)
    var usuarioPhone = view.findViewById<TextView>(R.id.usuario_item_phone)
    var usuarioDelete = view.findViewById<ImageButton>(R.id.deleteUsuarioBtn)
    fun render(usuario: Usuarios, database: String){
        auth = Firebase.auth
        usuarioName.text = usuario.name
        usuarioEmail.text = usuario.email
        usuarioPhone.text = usuario.phone
        usuarioDelete.setOnClickListener {
            var user = auth.signInWithEmailAndPassword(usuario.email,usuario.pass)
            user.addOnSuccessListener {
                it.user?.delete()
                auth.signOut()
                //Remover de su respectiva base de datos
                Firebase.database.getReference(database).child("Usuarios").child(usuario.uid).removeValue()
                //Remover de la base de datos en general
                Firebase.database.getReference("Usuarios").child(usuario.uid).removeValue()
            }

        }

    }

}