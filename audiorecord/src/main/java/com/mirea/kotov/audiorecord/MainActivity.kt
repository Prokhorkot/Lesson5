package com.mirea.kotov.audiorecord

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File


class MainActivity : AppCompatActivity() {
    companion object{
        private val TAG: String = MainActivity.javaClass.simpleName
        private val REQUEST_CODE_PERMISSION = 100
        private var mediaRecorder: MediaRecorder? = MediaRecorder()
        private var audioFile: File? = null

        fun hasPermissions(context: Context, permissions: Array<String>): Boolean{
            if(context != null && permissions != null){
                for(permission in permissions){
                    if(ActivityCompat.checkSelfPermission(context, permission) !=
                        PackageManager.PERMISSION_GRANTED){
                        return false
                    }
                }
            }
            return true
        }
    }

    //region Views

    private var buttonPlay: Button? = null
    private var buttonStop: Button? = null


    //endregion

    private val PERMISSIONS: Array<String> = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO)
    private var isWork: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //region Defining Views

        buttonPlay = findViewById(R.id.buttonPlay)
        buttonStop = findViewById(R.id.buttonStop)

        //endregion

        //region Button handlers

        buttonPlay?.setOnClickListener{onButtonPlayClick()}
        buttonStop?.setOnClickListener{onButtonStopClick()}

        //endregion

        isWork = hasPermissions(this, PERMISSIONS)

        if(!isWork){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE_PERMISSION){
            isWork = (grantResults.size > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
        }
    }

    private fun onButtonPlayClick(){
        try {
            buttonPlay!!.setEnabled(false)
            buttonStop!!.setEnabled(true)

            buttonStop!!.requestFocus()

            startRecording()
        } catch (e: Exception) {
            Log.e(TAG, "Caught io exception " + e.message)
        }
    }

    private fun onButtonStopClick(){
        buttonPlay!!.setEnabled(true)
        buttonStop!!.setEnabled(false)

        buttonPlay!!.requestFocus()

        stopRecording()
        processAudioFile()
    }


    private fun startRecording() {
        val state = Environment.getExternalStorageState()

        if (Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state) {
            Log.d(TAG, "sd-card success")

            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            if (audioFile == null) {

                audioFile = File(
                    getExternalFilesDir(
                        Environment.DIRECTORY_MUSIC
                    ), "mirea.3gp"
                )
            }

            mediaRecorder!!.setOutputFile(audioFile!!.getAbsolutePath())
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()

            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        if (mediaRecorder != null){
            Log.d(TAG, "stopRecording")

            mediaRecorder!!.stop()
            mediaRecorder!!.reset()
            mediaRecorder!!.release()

            Toast.makeText(this, "You are not recording right now!",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun processAudioFile() {
        Log.d(TAG, "processAudioFile")

        val values = ContentValues(4)
        val current = System.currentTimeMillis()

        values.put(MediaStore.Audio.Media.TITLE, "audio" + audioFile!!.name)
        values.put(MediaStore.Audio.Media.DATE_ADDED, (current / 1000).toInt())
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp")
        values.put(MediaStore.Audio.Media.DATA, audioFile!!.absolutePath)

        val contentResolver = contentResolver

        Log.d(TAG, "audioFile: " + audioFile!!.canRead())

        val baseUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val newUri: Uri? = contentResolver.insert(baseUri, values)

        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri))
    }

}