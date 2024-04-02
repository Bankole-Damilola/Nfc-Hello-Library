package com.example.nfcapp.utils

class TagWrapper(private val tagId: String) {
    val techList = TagTechList()

    val id: String get() = tagId
}