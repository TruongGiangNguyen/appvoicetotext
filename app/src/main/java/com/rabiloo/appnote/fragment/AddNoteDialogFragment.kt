package com.rabiloo.appnote.fragment

import android.app.Dialog
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import com.io.note.key.KEY
import com.rabiloo.appnote.R
import com.rabiloo.appnote.network.utils.PCMFormat
import com.rabiloo.appnote.network.websocket.IResponseHandler
import com.rabiloo.appnote.network.websocket.WebSocketClient
import com.rabiloo.appnote.websocketAPI.Text
import com.simplemobiletools.commons.extensions.getFormattedDuration
import com.visualizer.amplitude.AudioRecordView
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.util.CharsetUtil
import kotlinx.coroutines.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AddNoteDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(): AddNoteDialogFragment {
            return AddNoteDialogFragment()
        }
    }

    lateinit var start_record: Button
    lateinit var stop_record: Button
    lateinit var recorder_visualizer: AudioRecordView
    lateinit var toggle_recording_button: ImageView
    lateinit var rl_back: RelativeLayout
    lateinit var txt_recording_duration: TextView
    lateinit var title_timeDate: TextView
    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.MY_DIALOG)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog_add_note, container, false)
    }

    var streaming = true
    var microphone: AudioRecord? = null
    var ws: WebSocketClient? = null
    var timer: Timer? = null
    var isClick = "STOP"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        updateRecordingDuration(0)
        updateTimeDate()
    }

    fun initView(view: View) {
        rl_back = view.findViewById(R.id.rl_back)
        title_timeDate = view.findViewById(R.id.title_timeDate)
        toggle_recording_button = view.findViewById(R.id.toggle_recording_button)
        txt_recording_duration = view.findViewById(R.id.txt_recording_duration)
        recorder_visualizer = view.findViewById(R.id.recorder_visualizer)
        recorder_visualizer.recreate()
        start_record = view.findViewById(R.id.start_record)
        stop_record = view.findViewById(R.id.stop_record)
        val handler: IResponseHandler<WebSocketFrame?> = object :
                IResponseHandler<WebSocketFrame?> {
            override fun onMessage(msg: WebSocketFrame?) {
                if (msg is TextWebSocketFrame) {
                    val textFrame = msg as TextWebSocketFrame
                    val gson: Gson = Gson()
                    val textWsAPI = gson.fromJson(textFrame.text() , Text::class.java)
//                    Log.d("WEBSOCKET", textFrame.text())
                    Log.d("WEBSOCKET", textWsAPI.result.hypotheses.last().transcript)
                } else {
                    Log.d("WEBSOCKET", msg.toString())
                }
            }

            override fun onFailure(cause: Throwable?) {
                Log.d("WEBSOCKET", "Fail - ${cause?.printStackTrace()}")
            }

            override fun onComplete() {
                Log.d("WEBSOCKET", "completed")
            }
        }
        /*val client = AsrWebSocketClient.newBuilder()
            .setSampleRate(16000f)
            .setAudioFormat(PCMFormat.S16LE)
            .setHandler(handler)
            .setChannels(1)
            .setToken(KEY.TOKEN)
            .setUrl(KEY.URL_WEBSOCKET_API)
            .build()*/
        rl_back.setOnClickListener {
            dialog?.dismiss()
        }
        ws = WebSocketClient(generateQueryStringUri(KEY.URL_WEBSOCKET_API, 16000f, PCMFormat.S16LE, 1, KEY.TOKEN))
        toggle_recording_button.setOnClickListener {
            isClick = if (isClick == "STOP"){
                "RUN"
            }else{
                "STOP"
            }
            if (isClick == "RUN"){
                timer = Timer()
                updateAudioVisualizer()
                var duration: Int = 0
                timer?.scheduleAtFixedRate(object : TimerTask(){
                    override fun run() {
                        duration ++
                        updateRecordingDuration(duration)
                    }

                }, 0, 1000)
                CoroutineScope(Dispatchers.IO).launch {
                    streaming = true
                    val sampleRate = 16000
                    val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO
                    val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
                    try {
                        var minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                            minBufferSize = sampleRate * 2;
                        }
                        microphone = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufferSize)
                        if (microphone?.state != AudioRecord.STATE_INITIALIZED) {
                            Log.e("AUDIO", "Audio Record can't initialize!");
                            return@launch
                        }
                        microphone?.startRecording()
                        while (streaming) {
                            try {
                                val buffer = ByteArray(sampleRate * PCMFormat.S16LE.sampleSize / 16 ) // div 32 S16 16bit
                                microphone?.read(buffer, 0, buffer.size)
//                            ws?.addHandler(handler)
//                            ws?.sendBinaryMessage(buffer, 0, buffer.size)
                                delay(250)
                            }catch (e: Exception){
                                Log.d("TAG", e.message.toString())
                            }
                        }
                    } catch (io: IOException) {
                        Log.d("IOException", io.printStackTrace().toString())
                    } finally {
                        microphone?.stop()
                        microphone?.release()
//                    ws?.sendBinaryMessage("EOS".toByteArray(CharsetUtil.UTF_8))
                    }
                }
            }else{
                timer?.cancel()
                ws?.sendBinaryMessage("EOS".toByteArray(CharsetUtil.UTF_8))
                microphone?.stop()
                microphone?.release()
            }

        }

        stop_record.setOnClickListener {
            streaming = false
            if (microphone != null && ws != null && timer != null) {
                timer?.cancel()
                ws?.sendBinaryMessage("EOS".toByteArray(CharsetUtil.UTF_8))
                microphone?.stop()
                microphone?.release()
            }
        }
    }

    fun updateAudioVisualizer() {
//        val currentMaxAmplitude = MediaRecorder().getMediaRecorder().getMaxAmplitude()
        timer?.scheduleAtFixedRate(object : TimerTask(){
            override fun run() {
                recorder_visualizer.update((1000..8000).random())
            }

        }, 0, 60)
    }
    private fun updateRecordingDuration(duration: Int) {
        txt_recording_duration.text = duration.getFormattedDuration()
    }

    fun updateTimeDate(){
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val nameDayEL = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
        val nameDayVN = when(nameDayEL){
            "Monday" ->{
                "Thu hai"
            }
            "Tuesday" ->{
                "Thu ba"
            }
            "Wednesday" ->{
                "Thu tu"
            }
            "Thursday" ->{
                "Thu nam"
            }
            "Friday" ->{
                "Thu sau"
            }
            "Saturday" ->{
                "Thu bay"
            }
            "Sunday" ->{
                "Chu nhat"
            }
            else -> ""
        }
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        title_timeDate.text = "$currentTime, $nameDayVN $currentDate"
    }
    private fun generateQueryStringUri(url: String, sampleRate: Float, audioFormat: PCMFormat, channels: Int, token: String?, ): String {
        return "$url?content-type=audio/x-raw,+layout=(string)interleaved,+rate=(int)${sampleRate.toInt()},+format=(string)$audioFormat,+channels=(int)$channels&token=$token"
    }
}