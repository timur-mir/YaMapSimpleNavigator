package com.example.location.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import com.example.location.R
import com.example.location.data.roomrepo.getScaledBitmap
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PhotoFileFragment: DialogFragment() {
    private lateinit var exitButton: FloatingActionButton
    private lateinit var imView: ImageView
    var imagePath: String? = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dialogView: View = inflater.inflate(R.layout.photo_file_fragment, container, false)
        exitButton = dialogView.findViewById(R.id.exit)
        imView = dialogView.findViewById(R.id.image_photo) as ImageView
        imagePath = arguments?.getString("path")
        if (imagePath == null) {
        } else {
            imView.doOnLayout { measuredView ->
                val bitmap = getScaledBitmap(imagePath!!, measuredView.width, measuredView.height)
                imView.setImageBitmap(bitmap)

            }
            exitButton.setOnClickListener {
                dismiss()
            }

        }

        return dialogView
    }
}