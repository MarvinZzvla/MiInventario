package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vendetta.miinventario.recycler.UsuarioAdapter
import com.vendetta.miinventario.recycler.Usuarios
import kotlinx.android.synthetic.main.activity_usuarios_home.*

private lateinit var auth:FirebaseAuth
var usuariosProviderList = arrayListOf<Usuarios>()

class UsuariosHome : AppCompatActivity() {
    private var database = ""
    private var list = arrayListOf<DataSnapshot>()
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios_home)
        auth = Firebase.auth
        loadPreferences()
        getUsuarios()

        if(!isAdmin){
            addUser_btn.visibility = View.INVISIBLE
        }

        addUser_btn.setOnClickListener{
            Intent(this, RegisterUsers::class.java).apply { startActivity(this) }
        }
    }


    fun getUsuarios(){
        Firebase.database.getReference(database).child("Usuarios").addValueEventListener(
            object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()
                    list.add(snapshot)
                    providerUsuarios()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            }
        )
    }

    fun providerUsuarios(){
        usuariosProviderList.clear()

        list[0].children.forEach {
            usuariosProviderList.add(Usuarios("${it.child("name").value.toString()} ${it.child("apellido").value.toString()}",
            it.child("email").value.toString(),it.child("phone").value.toString(),it.child("uid").value.toString(),it.child("password").value.toString()))
        }

        initRecycleView()
    }

    fun initRecycleView(){
        var recyclerView = recycleUsuarios
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = UsuarioAdapter(usuariosProviderList,database)
    }

    fun loadPreferences(){
        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            database = this.getString("database","null").toString()
        }

        getSharedPreferences("login_prefs",Context.MODE_PRIVATE).apply {
            isAdmin = this.getBoolean("isAdmin",false)
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        Intent(this,HomePage::class.java).apply { startActivity(this) }
    }
}