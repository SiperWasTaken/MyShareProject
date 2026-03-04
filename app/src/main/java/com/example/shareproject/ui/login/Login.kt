package com.example.shareproject.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.example.shareproject.R
import com.example.shareproject.ui.registrazione.Registrazione
import com.example.shareproject.ui.home.HomeActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Activity per il login con email o Google
class Login : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.Companion.create(this)


        findViewById<View>(R.id.GoogleLogin).setOnClickListener {
            lifecycleScope.launch {
                signInWithGoogle()
            }
        }


    }

    // Verifica le credenziali e fa il login
    fun checkLogin(view: View){

        val email = findViewById<EditText>(R.id.editTextTextEmail).text.toString()
        val password = findViewById<EditText>(R.id.editTextTextPassword).text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()) {

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {

                if(it.isSuccessful){

                    Toast.makeText(this, "Utente registrato", Toast.LENGTH_SHORT).show()

                    goHome()

                }else{

                    Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()

                }
            }
        }else{

            Toast.makeText(this, "Inserisci email e password", Toast.LENGTH_SHORT).show()

        }
    }

    private fun goHome(){
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    fun goRegistrazione(view: View){
        val intent = Intent(this, Registrazione::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }


    // Gestisce il login con Google
    private suspend fun signInWithGoogle() {

        val serverClientId = getString(R.string.default_web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(serverClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {

            val result = credentialManager.getCredential(
                request = request,
                context = this
            )

            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {

                val googleIdTokenCredential =
                    GoogleIdTokenCredential.Companion.createFrom(credential.data)

                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Gmail e password errati", Toast.LENGTH_SHORT).show()
        }
    }

    // Autentica l'utente su Firebase con Google
    private suspend fun firebaseAuthWithGoogle(idToken: String) {

        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

        try {

            firebaseAuth.signInWithCredential(firebaseCredential).await()

            Toast.makeText(this, "Login riuscito!", Toast.LENGTH_SHORT).show()
            goHome()

        } catch (e: Exception) {
            Toast.makeText(this, "Errore Firebase", Toast.LENGTH_SHORT).show()
        }
    }
}