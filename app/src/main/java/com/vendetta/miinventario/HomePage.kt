package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_page.*

private lateinit var auth: FirebaseAuth
private var database = ""
private var isAdmin = false



class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        auth = Firebase.auth
        loadPreferences()
        if(!isAdmin){
            addUser_btn.visibility = View.INVISIBLE
        }


        btn_logout.setOnClickListener {
            logout()
        }

        addUser_btn.setOnClickListener{

            Intent(this, RegisterUsers::class.java).apply { startActivity(this) }
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
    }

}