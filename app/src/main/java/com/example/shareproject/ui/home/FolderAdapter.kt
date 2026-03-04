package com.example.shareproject.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shareproject.R
import com.example.shareproject.data.model.Folder

// Adapter per visualizzare cartelle e file in RecyclerView
class FolderAdapter(
    private val folders: List<Folder>,
    private val onClick: (Folder) -> Unit,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    val selectedFolderIds = mutableSetOf<String>()
    var isSelectionMode = false

    // ViewHolder per gli elementi della lista
    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtFolderName)
        val chkSelect: CheckBox = itemView.findViewById(R.id.chkSelect)
        val imgIcon: android.widget.ImageView = itemView.findViewById(R.id.imgIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.txtName.text = folder.name

        if (folder.type == "folder") {
            holder.imgIcon.setImageResource(R.drawable.ic_folder)
        } else {
            holder.imgIcon.setImageResource(R.drawable.ic_file)
        }

        holder.chkSelect.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.chkSelect.isChecked = selectedFolderIds.contains(folder.id)

        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) {
                isSelectionMode = true
                toggleSelection(folder.id)
            }
            true
        }


        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                toggleSelection(folder.id)
            } else {
                onClick(folder)
            }
        }

        holder.chkSelect.setOnClickListener {
            toggleSelection(folder.id)
        }
    }

    override fun getItemCount(): Int = folders.size


    private fun toggleSelection(folderId: String) {
        if (selectedFolderIds.contains(folderId)) {
            selectedFolderIds.remove(folderId)
        } else {
            selectedFolderIds.add(folderId)
        }

        if (selectedFolderIds.isEmpty()) {
            isSelectionMode = false
        }

        notifyDataSetChanged()

        onSelectionChanged(selectedFolderIds.size)
    }

    fun clearSelection() {
        selectedFolderIds.clear()
        isSelectionMode = false
        notifyDataSetChanged()
        onSelectionChanged(0)
    }
}