package com.mut_jaeryo.mltest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mut_jaeryo.mltest.translate.TranslateFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, TranslateFragment.newInstance())
                .commitNow();
        }
    }
}
