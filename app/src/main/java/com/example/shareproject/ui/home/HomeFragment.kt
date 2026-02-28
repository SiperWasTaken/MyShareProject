package com.example.shareproject.ui.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shareproject.R
import com.example.shareproject.data.model.Folder
import com.example.shareproject.ui.login.Login
import com.example.shareproject.utils.GeminiHelper
import com.example.shareproject.utils.OcrHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File

class HomeFragment : Fragment() {

    private lateinit var photoUri: Uri
    private val viewModel: HomeViewModel by viewModels()
    private var currentParentId: String? = null

    private val navigationStack = mutableListOf<String?>()


    private var backPressedTime: Long = 0

    private lateinit var folderAdapter: FolderAdapter
    private val foldersList = mutableListOf<Folder>()



    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                photoUri = createImageUri()
                takePicture.launch(photoUri)
            } else {
                Log.d("PERMISSION", "Permesso camera negato")
            }
        }

    // Fotocamera
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Log.d("UPLOAD", "Foto scattata: $photoUri")
                elaboraImmagineConMLKit(photoUri)
            }
        }

    // Galleria
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                Log.d("UPLOAD", "Immagine scelta: $it")
                elaboraImmagineConMLKit(it)
            }
        }

    // File
    private val pickDocument =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                Log.d("UPLOAD", "File scelto: $it")

                // TODO: NON FUNZIONA PER PDF!!!!

                elaboraImmagineConMLKit(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnCreateFolder = view.findViewById<Button>(R.id.btnCreateFolder)
        btnCreateFolder.setOnClickListener {
            showCreateFolderDialog()
        }

        val btnDeleteSelected = view.findViewById<Button>(R.id.btnDeleteSelected)

        viewModel.folders.observe(viewLifecycleOwner) { folders ->
            folders.forEach {
                Log.d("FOLDER", "${it.name} (id=${it.id}, parent=${it.parentId})")
            }
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.loadFolders(userId, currentParentId)
        }

        val btnCamera = view.findViewById<Button>(R.id.btnCamera)
        val btnGallery = view.findViewById<Button>(R.id.btnGallery)
        val btnFile = view.findViewById<Button>(R.id.btnFile)

        btnCamera.setOnClickListener {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }

        btnGallery.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        btnFile.setOnClickListener {
            pickDocument.launch(arrayOf("image/*", "application/pdf"))
        }

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerFolders)
        recycler.layoutManager = LinearLayoutManager(requireContext())


        folderAdapter = FolderAdapter(
            folders = foldersList,
            onClick = { item ->
                if (item.type == "folder") {
                    navigationStack.add(currentParentId)
                    currentParentId = item.id
                    viewModel.loadFolders(userId!!, currentParentId)
                } else {
                    android.widget.Toast.makeText(requireContext(), "Hai cliccato un file OCR!", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            onSelectionChanged = { selectedCount ->
                if (selectedCount > 0) {
                    btnDeleteSelected.visibility = View.VISIBLE
                    btnDeleteSelected.text = "Elimina ($selectedCount)"
                } else {
                    btnDeleteSelected.visibility = View.GONE
                }
            }
        )
        recycler.adapter = folderAdapter

        btnDeleteSelected.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle("Eliminare cartella?")
                .setMessage("Eliminare le cartelle selezionate?")
                .setPositiveButton("Sì") { _, _ ->

                    val idsToDelete = folderAdapter.selectedFolderIds.toList()

                    if (userId != null && idsToDelete.isNotEmpty()) {
                        viewModel.deleteFolders(idsToDelete, userId, currentParentId)
                    }

                    folderAdapter.clearSelection()
                }
                .setNegativeButton("Annulla", null)
                .show()
        }


        viewModel.folders.observe(viewLifecycleOwner) { folders ->
            foldersList.clear()
            foldersList.addAll(folders)
            folderAdapter.notifyDataSetChanged()
        }

        val onBackPressedCallback = object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (folderAdapter.isSelectionMode) {
                    folderAdapter.clearSelection()

                } else if (navigationStack.isNotEmpty()) {

                    currentParentId = navigationStack[navigationStack.size - 1]
                    navigationStack.removeAt(navigationStack.size - 1)
                    viewModel.loadFolders(userId!!, currentParentId)

                } else {

                    if (backPressedTime + 2000 > System.currentTimeMillis()) {

                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    } else {

                        android.widget.Toast.makeText(requireContext(), "Premi di nuovo per uscire", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    backPressedTime = System.currentTimeMillis()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    private fun createImageUri(): Uri {
        val file = File(
            requireContext().cacheDir,
            "temp_image_${System.currentTimeMillis()}.jpg"
        )

        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
    }

    private fun showCreateFolderDialog() {
        val editText = android.widget.EditText(requireContext())
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Nuova cartella")
            .setMessage("Inserisci il nome della cartella:")
            .setView(editText)
            .setPositiveButton("Crea") { _, _ ->
                val folderName = editText.text.toString().trim()
                if (folderName.isNotEmpty()) {
                    createFolder(folderName)
                }
            }
            .setNegativeButton("Annulla", null)
            .create()

        dialog.show()
    }

    private fun createFolder(name: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.createFolder(name, userId, currentParentId)
        }
    }

    private fun elaboraImmagineConMLKit(uri: Uri) {

        Log.d("OCR", "Inizio scansione dell'immagine...")

        OcrHelper.extractTextFromUri(
            context = requireContext(),
            imageUri = uri,
            onSuccess = { testoEstratto ->
                Log.d("OCR_SUCCESS", "Testo trovato dall'OCR:\n$testoEstratto")

                showOcrPreviewDialog(testoEstratto)

                /**
                // Lanciamo una Coroutine perché Gemini lavora tramite internet e ci mette un po'
                lifecycleScope.launch {
                    Log.d("APP", "Inviando il testo a Gemini per la standardizzazione...")

                    val risultatoJson = GeminiHelper.elaboraTestoConAI(testoEstratto)

                    if (risultatoJson != null) {

                        Log.d("GEMINI_SUCCESS", "Ecco il JSON pulito da Gemini:\n$risultatoJson")

                    } else {

                        Log.e("GEMINI_ERROR", "Gemini non è riuscito a rispondere.")

                    }
                }*/
            },
            onError = { eccezione ->

                Log.e("OCR_ERROR", "Errore durante la lettura del testo", eccezione)

            }
        )
    }


    private fun showOcrPreviewDialog(extractedText: String) {
        val context = requireContext()

        val layout = android.widget.LinearLayout(context)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val nameInput = android.widget.EditText(context)
        nameInput.hint = "Nome del file (es. Scontrino spesa)"
        layout.addView(nameInput)

        val textInput = android.widget.EditText(context)
        textInput.setText(extractedText)
        textInput.setLines(8)
        textInput.maxLines = 15
        textInput.isVerticalScrollBarEnabled = true
        textInput.gravity = android.view.Gravity.TOP
        layout.addView(textInput)

        AlertDialog.Builder(context)
            .setTitle("Salva Estrazione OCR")
            .setView(layout)
            .setPositiveButton("Salva") { _, _ ->
                val fileName = nameInput.text.toString().trim()
                val finalName = if (fileName.isNotEmpty()) fileName else "Scansione senza nome"
                val finalText = textInput.text.toString() // Prende il testo, incluse eventuali correzioni umane

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    viewModel.createFile(finalName, userId, currentParentId, finalText)
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}