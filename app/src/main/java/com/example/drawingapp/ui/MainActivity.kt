package com.example.drawingapp.ui

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.drawingapp.R

class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var mImageButtonCurrentPaint: ImageButton
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            if (it.resultCode == RESULT_OK && it.data != null)
                findViewById<ImageView>(R.id.iv_background).setImageURI(it.data?.data)
        }

    private val mGalleryAccess: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val isGranted = it.value

                if (isGranted) {
                    openGalleryLauncher.launch(
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                    )
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawing_view)
        drawingView.setSizeForBrush(10f)
        findViewById<ImageButton>(R.id.ib_brush).setOnClickListener {
            showBrushSizeDialog()
        }
        findViewById<ImageButton>(R.id.ib_gallery).setOnClickListener {
            requestStoragePermission()
        }
        findViewById<ImageButton>(R.id.ib_undo).setOnClickListener {
            drawingView.undo()
        }

        mImageButtonCurrentPaint =
            findViewById<LinearLayout>(R.id.ll_paint_colors)[0] as ImageButton
        mImageButtonCurrentPaint.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )
    }

    private fun requestStoragePermission() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            mGalleryAccess.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private fun showBrushSizeDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")

        val setOnClickListeners = fun(size: Float, id: Int) {
            brushDialog.findViewById<ImageButton>(id).setOnClickListener {
                drawingView.setSizeForBrush(size)
                brushDialog.dismiss()
            }
        }

        setOnClickListeners(10f, R.id.ib_small_brush)
        setOnClickListeners(20f, R.id.ib_medium_brush)
        setOnClickListeners(30f, R.id.ib_large_brush)

        brushDialog.show()
    }

    fun paintClicked(view: View) {
        if (view != mImageButtonCurrentPaint) {
            val imageButton = view as ImageButton
            drawingView.setColor(imageButton.tag.toString())

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_pressed
                )
            )

            mImageButtonCurrentPaint.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_normal
                )
            )

            mImageButtonCurrentPaint = view
        }
    }
}