package com.example.location.presentation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.location.R
import com.example.location.data.roomrepo.getScaledBitmap
import com.example.location.databinding.MarkListBinding
import com.example.location.domain.Mark
import java.io.File

class ShowAllMarksAdapter:ListAdapter<Mark,MarksHolder>(DiffUtilCallbackMarks()){
    var onItemLook: ((String) -> Unit)? = null
    var onItemDelete: ((Int) -> Unit)?= null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarksHolder {
        return MarksHolder(
          MarkListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MarksHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            markDelete.bringToFront()
            pointMark.text="Широта:${item.coordinateLat}  Долгота:${item.coordinateLong}"
                val bitmap = getScaledBitmap(getPathPhoto(item.photoFileName), 600, 800)
               placePic.setImageBitmap(bitmap)
                markDelete.setOnClickListener{
                    onItemDelete?.let { it1 -> it1(item.id) }
                }

        }
        holder.binding.root.setOnClickListener{
            onItemLook?.let { it1 -> it1(item.photoFileName) }
        }

    }
    fun getPathPhoto(fileName: String): String{
        val filesDir = ApplicationMapKit.applicationContext().filesDir
     val  photoFile = File(filesDir, fileName)
        return photoFile.path
    }

}
    class MarksHolder(val binding:MarkListBinding):RecyclerView.ViewHolder(binding.root)

    class DiffUtilCallbackMarks : DiffUtil.ItemCallback<Mark>() {
        override fun areItemsTheSame(oldItem: Mark, newItem: Mark): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Mark, newItem: Mark): Boolean =
            oldItem == newItem

    }


