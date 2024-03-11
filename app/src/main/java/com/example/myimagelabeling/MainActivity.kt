package com.example.myimagelabeling

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myimagelabeling.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val REQUEST_IMAGE_CAPTURE = 1

    private var imageBitmap: Bitmap? = null
    private var bitmap2: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.apply {

            captureImage.setOnClickListener {
                takeImage()
//                galleryLauncher.launch("image/*")
                textView.text = ""
            }

            detectImage.setOnClickListener {
                if (bitmap2 != null) {
                    processImage()
                } else {
                    Toast.makeText(this@MainActivity, "Select a photo first", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        val galleryUri = it
        try {
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, galleryUri)
            binding.imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun processImage() {

        val imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        val inputImage = InputImage.fromBitmap(bitmap2!!, 0)
        imageLabeler.process(inputImage).addOnSuccessListener { imageLabels ->
            var stringImageRecognition = ""
            for (imageLabel in imageLabels) {
                val stingLabel = imageLabel.text
                val floatConfidence = imageLabel.confidence
                val index = imageLabel.index
                stringImageRecognition += "$index\n $stingLabel:\n $floatConfidence "
            }
            binding.textView.text = stringImageRecognition
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            //set the selected image to ImageView
            binding.imageView.setImageURI(uri)
            val bitmapDrawable = binding.imageView.drawable as BitmapDrawable
            bitmap2 = bitmapDrawable.bitmap
        }
    }


    private fun takeImage() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        )
        intent.type = "image/*"
        intent.putExtra("crop", "true")
        intent.putExtra("scale", true)
        intent.putExtra("return-data", true)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)

    }




}