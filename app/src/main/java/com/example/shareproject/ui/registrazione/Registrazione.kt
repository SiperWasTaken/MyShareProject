package com.example.shareproject.ui.registrazione

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.shareproject.R
import com.example.shareproject.ui.login.Login
import com.google.firebase.auth.FirebaseAuth

class Registrazione : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrazione)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

    }



    fun registraUtente(view: View){

        val email = findViewById<EditText>(R.id.editTextTextEmail).text.toString()
        val password = findViewById<EditText>(R.id.editTextTextPassword).text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()){

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

                if(it.isSuccessful){

                    Toast.makeText(this, "Utente registrato", Toast.LENGTH_SHORT).show()

                    goLogin()

                }else{

                    Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()

                }
            }

        }else{

            Toast.makeText(this, "Inserisci email e password", Toast.LENGTH_SHORT).show()

        }

    }

    private fun goLogin(){
        val intent = Intent(this, Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}