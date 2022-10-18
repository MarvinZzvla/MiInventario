package com.vendetta.miinventario

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_recover_pass.*

class RecoverPass : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_pass)
        auth = Firebase.auth

        btnRecover.setOnClickListener {
            if (emailRecover.text.isNotEmpty()){
            recoverPass()}
            else{
                Toast.makeText(this,"Por favor ingrese su correo",Toast.LENGTH_SHORT).show()}
        }
    }

    fun recoverPass() {
        auth.setLanguageCode("es")
        auth.sendPasswordResetEmail(emailRecover.text.toString()).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(
                    this,
                    "Se ha enviado un correo para restablecer el password",
                    Toast.LENGTH_SHORT
                ).show()

                Intent(this,MainActivity::class.java).apply { startActivity(this) }

            } else {
                Toast.makeText(
                    this,
                    "Revisa si el correo esta bien escrito y vuelve a intentar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}