package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.vendetta.miinventario.databinding.ActivityFeedbackPageBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class FeedbackPage : AppCompatActivity() {
    private lateinit var binding:ActivityFeedbackPageBinding
    private lateinit var supabase: SupabaseClient

    @Serializable
    data class ReportEntitySupabase (
        val id: Int = 0,
        val description:String,
        val username: String,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Initialize supabase
        initSupabase()
        binding.btnSubmitFeedback.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
            sendMessage()
            }
        }
    }
    private fun initSupabase() {
        supabase = createSupabaseClient(
            supabaseUrl = "https://simoaelxxamqdllinrir.supabase.co",
            supabaseKey = BuildConfig.SUPABASE_API_KEY
        ) {
            install(Postgrest)
        }
    }

    private suspend fun sendMessage() {
        val sharedPreferences = getSharedPreferences("login_users", Context.MODE_PRIVATE)
        val description =  binding.suggestTextFeedbackPage.text.toString()
        val username = sharedPreferences.getString("username","NULL")
        val user = ReportEntitySupabase(description = description, username = username!!)
        try {
            supabase.from("Feedback").insert(user)
            withContext(Dispatchers.Main){
            Toast.makeText(applicationContext,"Mensaje enviado exitosamente",Toast.LENGTH_SHORT).show()
            Intent(applicationContext,HomePage::class.java).apply { startActivity(this) }
            }
        }
        catch (e:Exception){
            println(e.message)
            withContext(Dispatchers.Main){
            Toast.makeText(applicationContext,"Ocurrio un error ${e.message}",Toast.LENGTH_SHORT).show()
            }
        }
    }

}