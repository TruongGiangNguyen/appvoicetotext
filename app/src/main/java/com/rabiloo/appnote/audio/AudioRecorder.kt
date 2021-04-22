package com.rabiloo.appnote.audio

import android.content.Context
import android.media.MediaPlayer

import android.media.MediaRecorder
import android.os.Environment
import com.rabiloo.appnote.network.websocket.AsrWebSocketClient
import java.io.File
import java.io.IOException


class AudioRecorder(val context: Context) {
    var path: String
    val recorder = MediaRecorder()

    @Throws(IOException::class)
    fun start(client : AsrWebSocketClient) {
        val state = Environment.getExternalStorageState()
        if (state != Environment.MEDIA_MOUNTED) {
            throw IOException(
                "SD Card is not mounted.  It is " + state
                        + "."
            )
        }

        // make sure the directory we plan to store the recording in exists
        val directory: File = File(path).getParentFile()
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Path to file could not be created.")
        }
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder.setOutputFile(path)
        recorder.prepare()
        recorder.start()
        /*if (File(path).length().toInt() > 0){
            client.recognize(File(path))
        }*/
    }
    @Throws(IOException::class)
    fun createPath(): String{
        val fileFolder = File(context!!.getExternalFilesDir(null), "Audios")
        if (!fileFolder.exists()) {
            fileFolder.mkdirs()
        }

        val file = File("${fileFolder.absolutePath}/Audios${System.currentTimeMillis()}.3gp")
        return file.absolutePath
    }
    @Throws(IOException::class)
    fun stop() {
        recorder.stop()
        recorder.release()
    }

    @Throws(IOException::class)
    fun playarcoding(path: String?) {
        val mp = MediaPlayer()
        mp.setDataSource(path)
        mp.prepare()
        mp.start()
        mp.setVolume(10f, 10f)
    }

    init {
        path = createPath()
    }

}