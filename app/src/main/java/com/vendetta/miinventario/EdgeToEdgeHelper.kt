package com.vendetta.miinventario

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AppCompatActivity

/**
 * Applies edge-to-edge display (required for targetSdk 35+) and pads the root
 * content so system bars do not overlap UI.
 */
fun AppCompatActivity.enableEdgeToEdgeWithInsets(root: View = findViewById(android.R.id.content)) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        insets
    }
    root.requestApplyInsets()
}
