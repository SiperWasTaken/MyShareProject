package com.example.shareproject.utils

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

// Helper per estrarre testo dalle immagini tramite OCR
object OcrHelper {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Estrae il testo da un'immagine
    fun extractTextFromUri(
        context: Context,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {

            val image = InputImage.fromFilePath(context, imageUri)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->

                    onSuccess(visionText.text)
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
        } catch (e: Exception) {
            onError(e)
        }
    }
}