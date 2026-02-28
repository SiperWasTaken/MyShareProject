package com.example.shareproject.data.model

data class Folder(
    val id: String = "",
    val name: String = "",
    val userId: String = "",
    val parentId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val ocrText: String = "",
    val type: String = "folder"
)