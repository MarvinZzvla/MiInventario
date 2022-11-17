package com.vendetta.miinventario

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_info_screen.*

class InfoScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_screen)

        btnAction.text = intent.getStringExtra("btnText")
        msgInfo.text = intent.getStringExtra("msg")

        btnAction.setOnClickListener {
            Intent(Intent.ACTION_VIEW, Uri.parse(intent.getStringExtra("link"))).apply { startActivity(this) }
        }

    }
}