package com.example.location.presentation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import com.example.location.R
import com.example.location.data.roomrepo.getScaledBitmap
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
private const val REQUEST_PHOTO = 1
class PhotoFileFragment: DialogFragment() {
    private lateinit var exitButton: FloatingActionButton
    private lateinit var sendButton: FloatingActionButton
    private lateinit var imView: ImageView
    private lateinit var photoFile: File
    private var photoUri: Uri? = null
    var imagePath: String = ""
    var imageName: String = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dialogView: View = inflater.inflate(R.layout.photo_file_fragment, container, false)
        exitButton = dialogView.findViewById(R.id.exit)
        sendButton = dialogView.findViewById(R.id.send)
        imView = dialogView.findViewById(R.id.image_photo) as ImageView
        imageName = requireArguments().getString("photo").toString()
        imagePath = requireArguments().getString("path").toString()

        return dialogView
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val filesDir =requireActivity().filesDir
        if (imagePath == null) {
        } else {
            imView.doOnLayout { measuredView ->
                val bitmap = getScaledBitmap(imagePath!!, measuredView.width, measuredView.height)
                imView.setImageBitmap(bitmap)

            }
        }

        sendButton.setOnClickListener{
            try {
                photoFile = File(filesDir, imageName)
                photoUri = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.example.location.fileprovider",
                    photoFile
                )
                var arrayUri: ArrayList<out Parcelable>? = null
                arrayUri = mutableListOf(
                    photoUri
                ) as ArrayList<out Parcelable>

                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND_MULTIPLE
                    sendIntent.putParcelableArrayListExtra(
                        Intent.EXTRA_STREAM,
                        arrayUri
                    )
                    sendIntent.type = "image/jpeg"
                    val chooserIntent =
                        Intent.createChooser(sendIntent, getString(R.string.sendingPhoto))
                    // sendIntent.setPackage("com.whatsapp")
                    startActivityForResult(chooserIntent, REQUEST_PHOTO)

            }
            catch (e:Exception){
                e.printStackTrace()
            }

        }
        exitButton.setOnClickListener {
            dismiss()
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
//            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_PHOTO -> {

            }
        }
    }
}