package com.example.shareproject.data.model

// Classe che rappresenta una cartella o file nel sistema
data class Folder(
    val id: String = "",
    val name: String = "",
    val userId: String = "",
    val parentId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val ocrText: String = "",
    val type: String = "folder"
)