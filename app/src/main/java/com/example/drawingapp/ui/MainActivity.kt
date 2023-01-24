package com.example.drawingapp.ui

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.example.drawingapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var mImageButtonCurrentPaint: ImageButton
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            if (it.resultCode == RESULT_OK && it.data != null) findViewById<ImageView>(R.id.iv_background).setImageURI(
                it.data?.data
            )
        }

    private val mGalleryAccess: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (it.value) { //if the permission is granted
                    openGalleryLauncher.launch(
                        Intent(
                            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
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
        findViewById<ImageButton>(R.id.ib_save).setOnClickListener {
            lifecycleScope.launch {
                val flDrawingView:FrameLayout = findViewById(R.id.fl_drawing_view_container)
                val bitmap = getBitmapFrom(flDrawingView)
                saveBitmapFile(bitmap)
            }
        }

        mImageButtonCurrentPaint =
            findViewById<LinearLayout>(R.id.ll_paint_colors)[0] as ImageButton
        mImageButtonCurrentPaint.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )
    }

    private fun getBitmapFrom(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)

        if (view.background != null) view.background.draw(canvas)
        else canvas.drawColor(Color.WHITE)

        view.draw(canvas)

        return returnedBitmap
    }

    private suspend fun saveBitmapFile(bitmap: Bitmap) {
        try {
            var filePath: String?


            withContext(Dispatchers.IO) {
                val bytes = ByteArrayOutputStream()

                bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                filePath =
                    externalCacheDir?.absolutePath.toString() + File.separator + "DrawingApp" + System.currentTimeMillis() / 1000 + ".jpg"

                val fileOutput = FileOutputStream(filePath)

                fileOutput.write(bytes.toByteArray())
                fileOutput.close()
            }

            runOnUiThread {
                Toast.makeText(this, "File saved successfully on $filePath", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            mGalleryAccess.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
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
                    this, R.drawable.pallet_pressed
                )
            )

            mImageButtonCurrentPaint.setImageDrawable(
                ContextCompat.getDrawable(
                    this, R.drawable.pallet_normal
                )
            )

            mImageButtonCurrentPaint = view
        }
    }
}