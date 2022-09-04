package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.core.view.View
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register_users.*
private lateinit var auth: FirebaseAuth
private var mydatabase = ""
private var isFirst = false
class RegisterUsers : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_users)
        auth =  Firebase.auth
        loadPreferences()
        if(isFirst){
            //Set visible
            register_negocio.visibility = android.view.View.VISIBLE

        }

        btn_register.setOnClickListener {
            createUser()
        }
    }

    data class userInfo(var email:String,
                        var password:String,
                        var phone:String,
                        var name:String,var apellido:String,
                        var isAdmin:Boolean,
                        var lastLogin:String, var database:String,var uid:String)

    fun createUser(){
        if(isFirst) {
            mydatabase = register_negocio.text.toString()
        }
        register_negocio.text = Editable.Factory.getInstance().newEditable(mydatabase)


        if(checkFields()){
            //declare variables
            //Date in format dd/mm/yy at: hr:min:sec
            val date = java.util.Calendar.getInstance().time.date.toString()+"/"+
                    (java.util.Calendar.getInstance().time.month + 1).toString()+"/"+
                    (java.util.Calendar.getInstance().time.year + 1900).toString()+" - at: "+
                    java.util.Calendar.getInstance().time.hours.toString()+":"+java.util.Calendar.getInstance().time.minutes.toString()+":"+
                    java.util.Calendar.getInstance().time.seconds.toString()
            var name = register_name.text.toString() ;var last = register_last.text.toString();
            var email = register_email.text.toString();var pass = register_password.text.toString();
            var phone = register_phone.text.toString();var isAdmin = register_isAdmin.isChecked

            auth.createUserWithEmailAndPassword(email,pass).addOnSuccessListener {
               var uid = it.user?.uid.toString()
                var db = Firebase.database.getReference("Usuarios").child(uid)
                db.setValue(userInfo(email,pass,phone,name,last,isAdmin,date,mydatabase,uid))
                //Registrar en su respectiva base de datos
                var bussinesdb= Firebase.database.getReference(mydatabase).child("Usuarios").child(uid)
                bussinesdb.setValue(userInfo(email,pass,phone,name,last,isAdmin,date,mydatabase,uid))
               makeToast("Usuario registrado con exito")
            }
            if(isFirst){
                Intent(this,MainActivity::class.java).apply { startActivity(this) }
            }
            else {
                Intent(this, HomePage::class.java).apply { startActivity(this) }
            }

        }else{makeToast("Faltan campos por completar")}
    }

    fun checkFields():Boolean{
        return register_name.text.toString().isNotEmpty() && register_last.text.toString().isNotEmpty()
                &&register_email.text.toString().isNotEmpty() && register_password.text.toString().isNotEmpty()
                &&register_phone.text.toString().isNotEmpty() && register_negocio.text.toString().isNotEmpty()
    }

    fun makeToast(msj:String){
        Toast.makeText(this,msj, Toast.LENGTH_SHORT).apply { this.show() }
    }

    fun loadPreferences(){
        isFirst = intent.getBooleanExtra("isFirst",false)
        if (!isFirst) {
            getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
                mydatabase = this.getString("database", "null").toString()
            }
        }
    }
}