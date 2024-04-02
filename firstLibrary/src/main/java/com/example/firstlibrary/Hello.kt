package com.example.firstlibrary

import android.content.Context
import android.widget.Toast

class Hello(private val context: Context, val txt: String) {
    private val hello = "Class Hello"

    fun getHelloToast() {
        Toast.makeText(context, txt, Toast.LENGTH_LONG).show()
    }
}