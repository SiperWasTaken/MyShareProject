package com.example.shareproject.data.repository

import com.example.shareproject.data.model.Folder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FolderRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun createFolder(folder: Folder) {
        val docRef = db.collection("folders").document()
        val newFolder = folder.copy(id = docRef.id)
        docRef.set(newFolder).await()
    }

    suspend fun getFolders(userId: String, parentId: String?): List<Folder> {
        return db.collection("folders")
            .whereEqualTo("userId", userId)
            .whereEqualTo("parentId", parentId)
            .get()
            .await()
            .toObjects(Folder::class.java)
    }

    suspend fun deleteFolders(folderIds: List<String>) {
        val allIdsToDelete = mutableSetOf<String>()

        for (id in folderIds) {
            allIdsToDelete.add(id)
            val descendants = getDescendantFolderIds(id)
            allIdsToDelete.addAll(descendants)
        }

        val collection = db.collection("folders")

        allIdsToDelete.chunked(500).forEach { chunk ->
            val batch = db.batch()
            for (id in chunk) {
                batch.delete(collection.document(id))
            }
            batch.commit().await()
        }
    }


    private suspend fun getDescendantFolderIds(parentId: String): List<String> {
        val descendants = mutableListOf<String>()

        val children = db.collection("folders")
            .whereEqualTo("parentId", parentId)
            .get()
            .await()

        for (childDoc in children.documents) {
            val childId = childDoc.id
            descendants.add(childId)

            descendants.addAll(getDescendantFolderIds(childId))
        }

        return descendants
    }
}