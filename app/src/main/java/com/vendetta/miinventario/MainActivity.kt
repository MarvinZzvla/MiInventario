package com.vendetta.miinventario


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

private lateinit var auth: FirebaseAuth
private lateinit var database:HashMap<String,Any?>
//Variable para saber si esta logeado en Firebase auth
var isLogin = true
private var backPressedline =0L;


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        auth = Firebase.auth
        MobileAds.initialize(this) {}

        //Boton de login
        btn_login.setOnClickListener {
            //Se desactiva despues de dar un click

            loginUser()
        }

        crearInventarioBtn.setOnClickListener {
            Intent(this,RegisterUsers::class.java).apply {
                putExtra("isFirst",true)
                startActivity(this)
            }
        }


    }//end on create

    override fun onStart() {
        super.onStart()
        checkSession()
    }//END START

    /***************************************
     * Login sesion
     * Guarda las preferencias de la sesion para que no se tenga que volver a iniciar sesion
     * Guarda los datos del usuario de manera local para luego utilizarlos
     ***************************************/
    @SuppressLint("CommitPrefEdits")
    fun loginSession(){
        var prefs = getSharedPreferences("login_prefs",Context.MODE_PRIVATE).edit()
        prefs.putBoolean("isLogin", true)
        prefs.putString("database", database.get("database").toString())
        prefs.putBoolean("isAdmin", database.get("admin") as Boolean)
        prefs.putString("name", "${database.get("name").toString()} ${database.get("apellido").toString()}")
        prefs.apply()
        Intent(this,HomePage::class.java).apply { startActivity(this) }

    }

    /***************************************************
     * Chequea si el usuario esta logeado de ser asi lo manda a la pantalla principal
     **************************************************/
    fun checkSession(){
        var prefs = getSharedPreferences("login_prefs",Context.MODE_PRIVATE)
        isLogin = prefs.getBoolean("isLogin", false)
        if (isLogin){
            Intent(this,HomePage::class.java).apply { startActivity(this) }
        }
    }

    /**************************************
     * Login user
     * Llama a la funcion de logearse con firebase y chequea que los campos se esten cumpliendo
     **************************************/
    fun loginUser(){
        if (auth.currentUser == null){
            if(checkFields()){
                loginFirebase()
            }
            else{makeToast("Rellene los campos")}
        }
        else{
            println("Usuario loogeado con exito")
            auth.signOut()
            loginFirebase()
        }
    }

    /**************************************
     * Login Firebase
     * Loogea al usuario a firebase
     ********************************/
    fun loginFirebase(){
        var email = login_email.text.toString()
        var pass = login_password.text.toString()
        btn_login.isClickable = false
        btn_login.isEnabled = false
        auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener {
            if (it.isSuccessful){

                getReference()
            }
            else{
                btn_login.isClickable = true
                btn_login.isEnabled = true
                makeToast("Correo o contraseña invalidos")
            }
        }
    }

    /***********************************
     * Chquea que los campos de email
     * y password no esten vacios
     ***************************************/
    fun checkFields():Boolean{
        return login_email.text.toString().isNotEmpty() && login_password.text.toString().isNotEmpty()
    }

    /*********************************
     * Genera un toast de una manera mas sencilla
     *************************************/
    fun makeToast(msj:String){
        Toast.makeText(this,msj,Toast.LENGTH_SHORT).apply { this.show() }
    }

    /**************************************************
     * Get Reference
     * Obtiene la database que corresponde ese usuario para mostrar la respectiva info
     ****************************************************/
    fun getReference(){
        Firebase.database.getReference("Usuarios").child(auth.currentUser?.uid.toString()).get().apply {this.addOnSuccessListener {
            database = it.value as HashMap<String, Any?>
            loginSession()
        }
        }
    }

    override fun onBackPressed() {

        Intent(this,MainActivity::class.java).apply { startActivity(this) }
    }

    }



