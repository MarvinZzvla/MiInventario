package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_page.*


private lateinit var auth: FirebaseAuth
private var database = ""
private var isAdmin = false
private var fullname=""



class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        auth = Firebase.auth
        loadPreferences()

        var mAdView = adViewHome
        val adRequest: AdRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        btn_logout.setOnClickListener {
            logout()
        }


        //Botenes principales
        //ventas
        ventas_btn.setOnClickListener { Intent(this,VentasHome::class.java).apply { startActivity(this) } }
        //productos
        productos_btn.setOnClickListener { Intent(this,ProductosHome::class.java).apply { startActivity(this) } }
        //finanzas
        finanzas_btn.setOnClickListener {Intent(this,FinanzasHome::class.java).apply { startActivity(this) }}
        //usuarios
        usuarios_btn.setOnClickListener {Intent(this,UsuariosHome::class.java).apply { startActivity(this) }}

    }

    override fun onStart() {
        super.onStart()
        loadPreferences()
        loadViews()
        database_name.text = database.split("~")[0]
    }

    //Si el usuario no es administrador desactivar las opciones Productos, Finanzas y Usuarios
    fun loadViews(){
        if(!isAdmin){
            productos_btn.visibility = View.GONE
            finanzas_btn.visibility = View.GONE
            usuarios_btn.visibility = View.GONE
        }
        else{
            productos_btn.visibility = View.VISIBLE
            finanzas_btn.visibility = View.VISIBLE
            usuarios_btn.visibility = View.VISIBLE
        }
    }

    fun logout(){

        if(auth.currentUser != null){
        auth.signOut()
        }
        var prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE).edit()
        prefs.clear()
        prefs.apply()
        Intent(this,MainActivity::class.java).apply { startActivity(this) }
    }

    fun loadPreferences(){
        getSharedPreferences("login_prefs",Context.MODE_PRIVATE).apply {
            database = this.getString("database","null").toString()
        }

        getSharedPreferences("login_prefs",Context.MODE_PRIVATE).apply {
            isAdmin = this.getBoolean("isAdmin",false)
        }

        getSharedPreferences("login_prefs",Context.MODE_PRIVATE).apply {
            fullname = this.getString("name","null").toString()
        }
    }

}