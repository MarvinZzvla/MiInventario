package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vendetta.miinventario.recycler.UsuarioAdapter
import com.vendetta.miinventario.recycler.Usuarios
import kotlinx.android.synthetic.main.activity_usuarios_home.*

private lateinit var auth:FirebaseAuth
var usuariosProviderList = arrayListOf<Usuarios>()

class UsuariosHome : AppCompatActivity() {
    private var database = ""
    private var list = arrayListOf<QuerySnapshot>()
    private var isAdmin = false
    val fireData = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios_home)
        auth = Firebase.auth

        banner_usuarios.loadAd(AdRequest.Builder().build())
        loadPreferences()
        //getUsuarios()
        getUsuariosFire()

        if(!isAdmin){
            addUser_btn.visibility = View.INVISIBLE
        }

        addUser_btn.setOnClickListener{
            Intent(this, RegisterUsers::class.java).apply { startActivity(this) }
        }
    }
    fun getUsuariosFire(){
        fireData.collection("db1").document(database).collection("Usuarios").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (snapshot != null) {
                list.clear()
                list.add(snapshot)
                providerUsuariosFire()
            }
        }
    }
    fun providerUsuariosFire(){
        usuariosProviderList.clear()
        for (user in list[0].documents){
            val u = user.data
            usuariosProviderList.add(Usuarios("${u?.get("name").toString()} ${u?.get("apellido").toString()}",
                u?.get("email").toString(),u?.get("phone").toString(),u?.get("uid").toString(),u?.get("password").toString()))
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