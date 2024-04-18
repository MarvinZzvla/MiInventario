package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.consumePurchase
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList
import com.vendetta.miinventario.data.database.InventarioDatabase
import com.vendetta.miinventario.data.database.entities.DataClientEntity
import com.vendetta.miinventario.databinding.ActivitySubscriptionPageBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class SubscriptionPage : AppCompatActivity() {
    lateinit var binding: ActivitySubscriptionPageBinding
    lateinit var billingClient: BillingClient
    private lateinit var database: InventarioDatabase
    private lateinit var supabase: SupabaseClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = InventarioDatabase.getDatabase(this)
        initSupabase()

        //Declared a Listener after billing flow to handle the purchases
        val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                //IF Purchase PASSS
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        lifecycleScope.launch {
                            handlePurchase(purchase)
                        }
                    }
                    //If purchase was cancelled
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                    //If purchase was completed but not registered
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {


                } else {
                    // Handle any other error codes.
                    Toast.makeText(
                        this,
                        "Ha ocurrido un error inesperado: " + billingResult.responseCode + " " + billingResult.debugMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                    println(billingResult.debugMessage)
                }
            }
//Initialize the Billing Client to make a connection with Play Console
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        //When Pay button is clicked
        binding.googlePayBtn.setOnClickListener {
            //Start Connection
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // The BillingClient is ready. You can query purchases here.
                        getProductos()
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.

                }
            })

        }


        binding.submitCode.setOnClickListener {
            //DISABLE BUTTON TO AVOID REPEAT ACTION
            val button = binding.submitCode
            button.isEnabled = false
            //GET THE INPUT TEXT
            var codeText = binding.secretPassText.text.toString()
            //IF input text is not empty
            if (codeText.isNotEmpty()) {
                //Call couroutine IO
                lifecycleScope.launch(Dispatchers.IO) {
                    //Try the following
                    try {
                        var token = sendRequest(false)//sendresquest obtain a boolean parameters false = GET HTTPS true= POST HTTPS
                        val jsonObject = JSONObject(token).getString("token") //Obtain response and get the apu value
                        //If api key that user input is equal to api key from server
                        if(jsonObject.toString() == codeText){
                            //SEND A REQUEST TO CREATE A NEW API KEY
                            var status = sendRequest(true)
                            //GET THE STATUS CODE
                            val jsonObject = JSONObject(status).getString("code")
                            //IF the status CODE is EQUAL 201 = SUCCESS
                            if(jsonObject.toString() == "201"){
                                println("Creado con exito ")
                                withContext(Dispatchers.Main){
                                    button.isEnabled = true
                                    successPage()
                                }
                            }

                        }
                        else{
                           withContext(Dispatchers.Main){
                               button.isEnabled = true
                               Toast.makeText(applicationContext,"Codigo incorrecto, verifique e intente nuevamente",Toast.LENGTH_SHORT).show()
                           }
                        }
                    } catch (err: Exception) {
                        withContext(Dispatchers.Main)
                        {
                            button.isEnabled = true
                            //PASS ERROR CODE and HANDLE IT
                            handleError(err.message)
                        }

                    }

                }
            }
            else{
                button.isEnabled = true
                Toast.makeText(this,"Por favor ingrese un codigo antes de enviar",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun successPage() {
        //HACER
    }

    private fun handleError(code: String?) {

        when (code) {
            "429" -> Toast.makeText(applicationContext, "Muchos intentos, vuelva en 10 minutos", Toast.LENGTH_SHORT).show()

            else -> Toast.makeText(applicationContext, "Error desconocido: $code", Toast.LENGTH_SHORT).show()
        }
    }


    private fun sendRequest(put: Boolean):String? {
        val url = BuildConfig.URL_INVENTARIO
        val apiKey = BuildConfig.API_INVENTARIO_KEY
        val client = OkHttpClient()
        val JSON = "application/json; charset=utf-8".toMediaType()
        val body = RequestBody.create(JSON, "")

        if (put) {
            //SI ES PETICION PUT
            val request = Request.Builder()
                .url(url)
                .put(body)
                .addHeader(BuildConfig.SECRET_HEADER, apiKey)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("${response.code}")
                }
                return response.body?.string()
            }
        } else {
            //SI ES GET
            val request = Request.Builder()
                .url(url)
                .addHeader("X-SECRET-KEY", apiKey)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("${response.code}")
                }
                return response.body?.string()
            }
        }

    }

    /*********************************************************************
     * Get productos
     * Get a list a available productos to purchase
     ***********************************************************************************/
    private fun getProductos() {
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    ImmutableList.of(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("renovacion_mensual")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                )
                .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                            productDetailsList ->
            // check billingResult
            makePayment(productDetailsList[0])
            // process returned productDetailsList
        }
    }

    /******************************************************************
     * Make Payment
     * Lauch the billing flow
     ******************************************************************************/
    private fun makePayment(productDetails: ProductDetails?) {
        if (productDetails != null) {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                    .setProductDetails(productDetails)
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            // Launch the billing flow
            val billingResult = billingClient.launchBillingFlow(this, billingFlowParams)
        }
    }


    /**************************************************************************************************
     * Handle Purchase
     * After to success flow of billing process, verify and get token
     * Save token into database
     *************************************************************************************************/
    suspend fun handlePurchase(purchase: Purchase) {
        // Purchase retrieved from BillingClient#queryPurchasesAsync or your PurchasesUpdatedListener.
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build()
        val consumeResult = withContext(Dispatchers.IO) {
            billingClient.consumePurchase(consumeParams)
        }
        println("Este es la respuesta: " + consumeResult.billingResult.responseCode)
        println("Este es la respuesta: " + consumeResult.billingResult.debugMessage)
        println("Este es el token: " + consumeResult.purchaseToken)

        if (consumeResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK
            || consumeResult.billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
        ) {
            withContext(Dispatchers.IO) {
                updateDatabase(consumeResult.purchaseToken)
            }
        }
    }

    /*********************************************************************
     * Init Supabase
     * Configuration to Supabase Server
     *********************************************************************/
    private fun initSupabase() {
        supabase = createSupabaseClient(
            supabaseUrl = "https://simoaelxxamqdllinrir.supabase.co",
            supabaseKey = BuildConfig.SUPABASE_API_KEY
        ) {
            install(Postgrest)
        }
    }

    /**************************************************************
     * BillingData
     * It is a data class for Supabase Server
     *************************************************************/
    @Serializable
    data class BillingData(val Date: String, val User: String, val Phone: String, val Token: String)

    /************************************************************************************************************
     * Update Database
     * Update local database
     * Update Supabase Database
     ********************************************************************************************************/
    private suspend fun updateDatabase(token: String?) {
        val formato = SimpleDateFormat("dd/MM/yyyy")
        val today = Date()
        val calendario =
            Calendar.getInstance().apply { this.time = today; this.add(Calendar.DAY_OF_YEAR, 30) }
        val dateActual = formato.format(today)
        val dateExpired = formato.format(calendario.time)
        val sharedPreferences = getSharedPreferences("login_users", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Mi inventario") ?: ""
        val phone = sharedPreferences.getString("phone", "88888888") ?: ""

        //Update into Local Database
        val dataClient = DataClientEntity(id = 1, dateActual, dateExpired, true)
        database.dataClientDao.update(dataClient)

        //Update into SUPABASE
        val data = BillingData(dateActual, username, phone, token ?: "Invalid Token")
        try {
            supabase.from("BillingData").insert(data)
        } catch (e: Exception) {
            println(e.message)
        }
        //Back to HomePage
        withContext(Dispatchers.Main) {
            Toast.makeText(applicationContext, "La compra fue exitosa", Toast.LENGTH_SHORT).show()
            Intent(applicationContext, HomePage::class.java).apply { startActivity(this) }
        }
    }

}