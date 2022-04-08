package com.mirea.kotov.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    //region Constants

    companion object{
        const val REQUEST_CODE_PERMISSION_CAMERA = 100
        const val CAMERA_REQUEST = 0
    }

    //endregion

    //region Private properties

    val TAG: String = MainActivity.javaClass.simpleName
    private var imageView: ImageView? = null
    private var button: Button? = null
    private var isWork = false
    private var imageUri: Uri? = null

    //endregion

    //region start for result

    val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data

            imageView!!.setImageURI(imageUri)
        }
    }

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //region Defining views

        imageView = findViewById(R.id.imageView)
        button = findViewById(R.id.button)

        //endregion

        //region Checking permissions

        val cameraPermissionStatus =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        val storagePermissionStatus =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (cameraPermissionStatus == PackageManager.PERMISSION_GRANTED && storagePermissionStatus == PackageManager.PERMISSION_GRANTED) {
            isWork = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSION_CAMERA
            )
        }

        //endregion

        //region Click handlers

        button!!.setOnClickListener{buttonOnClick()}

        //endregion
    }

    private fun buttonOnClick(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (/*cameraIntent.resolveActivity(packageManager) != null &&  (только для эмулятора)*/
            isWork) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            // генерирование пути к файлу на основе authorities
            val authorities = applicationContext.packageName + ".fileprovider"
            imageUri = FileProvider.getUriForFile(this, authorities, photoFile!!)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startForResult.launch(cameraIntent)
        }
    }

    /**
     * Производится генерирование имени файла на основе текущего времени и создание файла
     * в директории Pictures на ExternelStorage.
     * class.
     * @return File возвращается объект File .
     * @exception IOException если возвращается ошибка записи в файл
     */
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_" + timeStamp + "_"
        val storageDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDirectory)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        if (requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
            isWork = (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        }
    }
}