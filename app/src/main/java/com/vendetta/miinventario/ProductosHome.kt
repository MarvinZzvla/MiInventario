package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.SearchView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vendetta.miinventario.recycler.Productos
import com.vendetta.miinventario.recycler.ProductosAdapter
import kotlinx.android.synthetic.main.activity_productos_home.*

private var database = ""
private lateinit var auth: FirebaseAuth
private var list = arrayListOf<QuerySnapshot>()
var productosProviderList = arrayListOf<Productos>()

class ProductosHome : AppCompatActivity() {
    var searchTxt = ""
    @RequiresApi(Build.VERSION_CODES.N)
    private val fireData = Firebase.firestore
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos_home)
        auth = Firebase.auth
        banner_productos.loadAd(AdRequest.Builder().build())
        loadPreferences()
        getProductos()
        searchBar()


        btnCrearProducto.setOnClickListener {

            Intent(this,NuevoProducto::class.java).apply { startActivity(this) }
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun searchBar(){
    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

        override fun onQueryTextSubmit(query: String?): Boolean {
            searchTxt = query?:""
            searchTxt = searchTxt.lowercase()
            providerProductos()
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
                searchTxt = newText?:""
                searchTxt = searchTxt.lowercase()
                providerProductos()

            return false
        }

    })
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun getProductos(){
        fireData.collection("db1").document(database).collection("Productos").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (snapshot != null) {
                list.clear()
                list.add(snapshot)
                providerProductos()
            }
        }
    }
    fun providerProductos(){
        productosProviderList.clear()
        for(producto in list[0].documents){
            val p = producto.data

            productosProviderList.add(Productos(p?.get("name").toString(),p?.get("precio").toString(),
                "Cantidad: ${p?.get("cantidad").toString()}"))
        }
/** Buscador implementado **/
        if(searchTxt.isNotEmpty()){
            var tmplist = arrayListOf<Productos>()
            for (p in productosProviderList){
                if(p.name.lowercase().contains(searchTxt)){
                    tmplist.add(p)
                }
            }
            productosProviderList.clear()
            productosProviderList.addAll(tmplist)
        }
        /**Ordenar lista ***/
        productosProviderList.sortBy {
            it.name
        }


        initRecycleView()
    }

    fun initRecycleView(){
        val recyclerView = recycleProductos
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProductosAdapter(productosProviderList,database)
    }


    fun loadPreferences(){
        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            database = this.getString("database","null").toString()
        }
        }

    override fun onBackPressed() {
        super.onBackPressed()
        Intent(this,HomePage::class.java).apply { startActivity(this) }
    }
}