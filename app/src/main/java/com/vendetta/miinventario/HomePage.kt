package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.common.collect.ImmutableList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.android.synthetic.main.activity_home_page.*
import android.app.Activity
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore


private lateinit var auth: FirebaseAuth
private var database = ""
private var isAdmin = false
private var fullname=""
private var backPressedline =0L;
private var isRatingUs = false
private var isShow = false
private var infoScreenLink = ""; private var infoScreenBtnText = ""; private var infoScreenMsg = ""
lateinit var billingClient:BillingClient
var productDetailsList: MutableList<ProductDetails> = ArrayList()



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
        initializeBilling()


        var mAdView = adViewHome
        val adRequest: AdRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


        if(isRatingUs){
            reviewButton.visibility = View.GONE
            calificanosText.visibility = View.GONE
        }


        info_btn.visibility = View.GONE
        textView23.visibility = View.GONE

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

        buttonPay.setOnClickListener {
            purchaseFlow()
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

    fun purchaseFlow(){
        val activity : Activity = this;

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(productDetailsList[0])
                // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                // for a list of offers that are available to the user
                .setOfferToken(productDetailsList[0].subscriptionOfferDetails?.get(0)?.offerToken.toString())
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

// Launch the billing flow
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
    }


    private fun initializeBilling() {

         billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(object :PurchasesUpdatedListener{
                override fun onPurchasesUpdated(billingResult: BillingResult, list: MutableList<Purchase>?) {
                    if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK && list !=null){
                        for (purchase in list) {

                            //verifySubPurchase(purchase);

                        }
                    }
                }

            }).build()

        //start the connection after initializing the billing client
            establishConnection()
    }

    private fun establishConnection() {

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    showProducts();
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                establishConnection();
            }
        })
    }

    private fun showProducts() {

        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    ImmutableList.of(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("remove_ads_mes")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()))
                .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) {
                billingResult,
                productList ->

            productDetailsList.clear()
            productDetailsList.addAll(productList)

            println("Este es el token" + productList[0].subscriptionOfferDetails?.get(0)?.offerToken)
            // check billingResult
            // process returned productDetailsList
        }



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