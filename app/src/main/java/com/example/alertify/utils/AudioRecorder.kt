package com.example.alertify.utils


import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import java.io.File



class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun startRecording() {

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)

        outputFile = File(
            dir,
            "SOS_${System.currentTimeMillis()}.mp4a"
        )

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile!!.absolutePath)
            prepare()
            start()
        }
    }

    fun stopRecording(): File? {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        return outputFile
    }
}