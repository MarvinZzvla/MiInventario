package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.core.view.View
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register_users.*
private lateinit var auth: FirebaseAuth
private var mydatabase = ""
private var myUid=""
private var isFirst = false
class RegisterUsers : AppCompatActivity() {

    private var mInterstitialAd: InterstitialAd? = null
    val fireData = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_users)
        auth =  Firebase.auth

        loadAdFullScreen()
        loadPreferences()

        if(isFirst){
            //Set visible
            register_negocio.visibility = android.view.View.VISIBLE
            register_isAdmin.isChecked = true;
            register_isAdmin.visibility = android.view.View.INVISIBLE

        }

        btn_register.setOnClickListener {
            //createUser()
            createUserFireStore()
        }
        btn_hasAccount.setOnClickListener {
            onBackPressed()
        }
    }


    fun loadAdFullScreen(){
        var adRequest = AdRequest.Builder().build()
        // ORIGINAL ca-app-pub-2467116940009132/5486356001
        //TEST ca-app-pub-3940256099942544/1033173712

        InterstitialAd.load(this, "ca-app-pub-2467116940009132/5486356001",adRequest,object: InterstitialAdLoadCallback(){
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
                println("Este es  el msj: "+adError.message)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                println("Ad Loaded")
                mInterstitialAd = interstitialAd
            }
        })
    }

    data class userInfo(var email:String,
                        var password:String,
                        var phone:String,
                        var name:String,var apellido:String,
                        var isAdmin:Boolean,
                        var lastLogin:String, var database:String,var uid:String)
    fun createUserFireStore(){
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

            auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener {
                println("Estamos Creando ... ")
                if(it.isSuccessful){
                    var uid = it.result.user?.uid.toString()
                    myUid = uid
                    //var db = Firebase.database.getReference("Usuarios").child(uid)
                    var db = fireData.collection("db1").document("Usuarios").collection("Usuarios").document(uid)
                    if(isFirst){
                        //db.setValue(userInfo(email,pass,phone,name,last,isAdmin,date,"$mydatabase~$uid",uid))
                        db.set(userInfo(email,pass,phone,name,last,isAdmin,date,"$mydatabase~$uid",uid))

                    }
                    else{
                        //db.setValue(userInfo(email,pass,phone,name,last,isAdmin,date, mydatabase,uid))
                        db.set(userInfo(email,pass,phone,name,last,isAdmin,date, mydatabase,uid))
                    }
                    //Registrar en su respectiva base de datos
                    /***** Realizar cambio aqui ****/

                    if (isFirst){
                        //var bussinesdb= Firebase.database.getReference("$mydatabase~$uid").child("Usuarios").child(uid)
                        var bussinesdb = fireData.collection("db1").document("$mydatabase~$uid").collection("Usuarios").document(uid)
                        bussinesdb.set(userInfo(email,pass,phone,name,last,isAdmin,date,"$mydatabase~$uid",uid))
                        //bussinesdb.setValue(userInfo(email,pass,phone,name,last,isAdmin,date,"$mydatabase~$uid",uid))
                    }
                    else{
                        //var bussinesdb= Firebase.database.getReference(mydatabase).child("Usuarios").child(uid)
                        var bussinesdb = fireData.collection("db1").document(mydatabase).collection("Usuarios").document(uid)
                        //bussinesdb.setValue(userInfo(email,pass,phone,name,last,isAdmin,date,mydatabase,uid))
                        bussinesdb.set(userInfo(email,pass,phone,name,last,isAdmin,date, mydatabase,uid))

                    }

                    if(isFirst){

                        var prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE).edit()
                        prefs.putBoolean("isLogin", true)
                        prefs.putString("database","$mydatabase~$myUid")
                        prefs.putBoolean("isAdmin", isAdmin as Boolean)
                        prefs.putString("name", "${register_name.toString()} ${register_last.toString()}")
                        prefs.apply()
                        Intent(this,MainActivity::class.java).apply { startActivity(this) }
                    }
                    else {
                        Intent(this, HomePage::class.java).apply { startActivity(this) }
                    }

                    makeToast("Usuario registrado con exito")
                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(this)
                    }else{println("El anuncio esta cargando")}
                }

            }.addOnFailureListener {
                println("Esta es la excepcion" + it.message)
            }//END LISTENER
        }
        else{makeToast("Faltan campos por completar")}
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
        println("Base de datos:"+mydatabase)

    }
}