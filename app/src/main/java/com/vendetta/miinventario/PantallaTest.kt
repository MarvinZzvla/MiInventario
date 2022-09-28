package com.vendetta.miinventario

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_pantalla_test.*
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class PantallaTest : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_test)

        btnShare.setOnClickListener {
            shareButton()
            //https://www.youtube.com/watch?v=2m28IQz5LpY apache tutorial
        }

    }

    fun shareButton(){
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, Uri.EMPTY)
            type = "application/*"
        }

        Intent.createChooser(intent,null).apply { startActivity(this) }
    }

}