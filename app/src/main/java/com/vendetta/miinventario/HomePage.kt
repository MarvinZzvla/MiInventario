package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.android.synthetic.main.activity_home_page.*
import org.json.JSONArray
import kotlin.system.exitProcess


private lateinit var auth: FirebaseAuth
private var database = ""
private var isAdmin = false
private var fullname=""
private var backPressedline =0L;
private var isRatingUs = false
private var isShow = false
private var infoScreenLink = ""; private var infoScreenBtnText = ""; private var infoScreenMsg = ""



class HomePage : AppCompatActivity() {

    lateinit var reviewInfo:ReviewInfo
    lateinit var manager:ReviewManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        auth = Firebase.auth
        loadPreferences()
        activateReviewInfo()
        setConfigRemote()


        var mAdView = adViewHome
        val adRequest: AdRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


        if(isRatingUs){
            reviewButton.visibility = View.GONE
            calificanosText.visibility = View.GONE
        }


        info_btn.visibility = View.GONE
        textView23.visibility = View.GONE


       /*
        if(!isShow){
            info_btn.visibility = View.GONE
            textView23.visibility = View.GONE
        } else{
            info_btn.visibility = View.VISIBLE
            textView23.visibility = View.VISIBLE
            getSharedPreferences("login_prefs",Context.MODE_PRIVATE).edit().apply {
                this.putBoolean("isInfoScreen",true)
                this.apply()
            }
        }*/

        btn_logout.setOnClickListener {
            logout()
        }

        reviewButton.setOnClickListener {
        startReviewFlow()
        }

        info_btn.setOnClickListener {
            info_btn.visibility = View.GONE
            textView23.visibility = View.GONE
            getSharedPreferences("login_prefs",Context.MODE_PRIVATE).edit().apply {
                this.putBoolean("isInfoScreenPressed",true)
                this.apply()
            }
            Intent(this,InfoScreen::class.java).apply {
                this.putExtra("msg", infoScreenMsg)
                this.putExtra("btnText", infoScreenBtnText)
                this.putExtra("link", infoScreenLink)
                startActivity(this)
            }
        }


        //Botenes principales
        //ventas

        ventas_btn.setOnClickListener { Intent(this,VentasHome::class.java).apply { startActivity(this) } }

       /** ventas_btn.setOnClickListener { Intent(this,PantallaTest::class.java).apply { startActivity(this) } }*/
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
        loadConfigRemote()
        database_name.text = database.split("~")[0]
    }

    fun setConfigRemote(){
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 30
        }
        val firebasConfig=Firebase.remoteConfig
        firebasConfig.setConfigSettingsAsync(configSettings)
         firebasConfig.setDefaultsAsync(mapOf("show_infoScreen" to false,
         "infoScreen_btnText" to "volver",
             "infoScreen_msg" to "www.google.com",
             "infoScreen_msg" to "Ha ocurrido un error inesperado por favor volver"))
    }

    fun loadConfigRemote(){

        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener {
            if(it.isSuccessful){
                val infoButton = Firebase.remoteConfig.getBoolean("show_infoScreen")
                 infoScreenMsg = Firebase.remoteConfig.getString("infoScreen_msg")
                 infoScreenBtnText = Firebase.remoteConfig.getString("infoScreen_btnText")
                 infoScreenLink = Firebase.remoteConfig.getString("infoScreen_link")


                if (!isShow && infoButton){
                    textView23.visibility = View.VISIBLE
                    info_btn.visibility = View.VISIBLE
                }
                else if(isShow && !infoButton){
                    info_btn.visibility = View.GONE
                    textView23.visibility = View.GONE
                    getSharedPreferences("login_prefs",Context.MODE_PRIVATE).edit().apply {
                        this.putBoolean("isInfoScreenPressed", false)
                        this.apply()
                    }
                }
                else if(isShow && infoButton){
                    info_btn.visibility = View.GONE
                    textView23.visibility = View.GONE
                }
            }
        }

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

    fun activateReviewInfo(){
        manager = ReviewManagerFactory.create(this)
        var managerInfoTask:Task<ReviewInfo> = manager.requestReviewFlow()
        managerInfoTask.addOnCompleteListener {
            if(it.isSuccessful){
               reviewInfo =  it.getResult()
            }
            else{
                Toast.makeText(this,"Review Fallo al cargar",Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startReviewFlow(){
        if(reviewInfo != null){
           var flow:Task<Void> = manager.launchReviewFlow(this,reviewInfo)
            flow.addOnCompleteListener {
                Toast.makeText(this,"Gracias por la revision",Toast.LENGTH_SHORT).show()
                reviewButton.visibility = View.GONE
                calificanosText.visibility = View.GONE
                getSharedPreferences("login_prefs",Context.MODE_PRIVATE).edit().apply {
                    this.putBoolean("isRating",true)
                    this.apply()
                }
            }
        }
    }
    fun logout(){

        if(auth.currentUser != null){
        auth.signOut()
        }
        var prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE).edit()
        prefs.clear()
        prefs.apply()
        this.finish()
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

        getSharedPreferences("login_prefs",Context.MODE_PRIVATE).apply {
            isRatingUs = this.getBoolean("isRating",false)
        }

        getSharedPreferences("login_prefs",Context.MODE_PRIVATE).apply {
            isShow = this.getBoolean("isInfoScreenPressed",false)

        }

    }

    override fun onBackPressed() {
        Intent(this,HomePage::class.java).apply { startActivity(this) }
    }

}