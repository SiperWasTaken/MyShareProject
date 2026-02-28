package com.example.shareproject.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shareproject.data.model.Folder
import com.example.shareproject.data.repository.FolderRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = FolderRepository()

    private val _folders = MutableLiveData<List<Folder>>()
    val folders: LiveData<List<Folder>> = _folders

    fun createFolder(name: String, userId: String, parentId: String?, ocrText: String = "") {
        viewModelScope.launch {
            val folder = Folder(
                name = name,
                userId = userId,
                parentId = parentId,
                ocrText = ocrText
            )
            repository.createFolder(folder)
            loadFolders(userId, parentId)
        }
    }

    fun loadFolders(userId: String, parentId: String?) {
        viewModelScope.launch {
            val list = repository.getFolders(userId, parentId)
            _folders.value = list
        }
    }

    fun createFile(name: String, userId: String, parentId: String?, ocrText: String) {
        viewModelScope.launch {
            val fileItem = Folder(
                name = name,
                userId = userId,
                parentId = parentId,
                ocrText = ocrText,
                type = "file"
            )
            repository.createFolder(fileItem)
            loadFolders(userId, parentId)
        }
    }

    fun deleteFolders(folderIds: List<String>, userId: String, parentId: String?) {
        viewModelScope.launch {
            try {
                repository.deleteFolders(folderIds)

                loadFolders(userId, parentId)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
