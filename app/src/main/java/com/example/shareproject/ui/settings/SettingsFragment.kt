package com.example.shareproject.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.shareproject.R
import com.example.shareproject.ui.login.Login
import com.google.firebase.auth.FirebaseAuth

// Fragment per le impostazioni e logout
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Conferma Logout")
                .setMessage("Sei sicuro di voler uscire dall'applicazione?")
                .setPositiveButton("Sì") { _, _ ->

                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(requireContext(), Login::class.java)

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Annulla", null)
                .show()
        }
    }
}