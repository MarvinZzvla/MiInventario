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
                    Toast.makeText(this, "Ha ocurrido un error inesperado: " + billingResult.responseCode + " " + billingResult.debugMessage, Toast.LENGTH_SHORT).show()
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
                    if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
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