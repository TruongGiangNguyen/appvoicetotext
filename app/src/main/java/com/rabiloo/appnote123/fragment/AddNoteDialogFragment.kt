package com.rabiloo.appnote123.fragment

import android.app.Dialog
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.android.gms.tasks.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.kaopiz.kprogresshud.KProgressHUD
import com.rabiloo.appnote123.R
import com.rabiloo.appnote123.key.KEY
import com.rabiloo.appnote123.listener.CallFunctionListener
import com.rabiloo.appnote123.model.DetailNote
import com.rabiloo.appnote123.model.Note
import com.rabiloo.appnote123.network.utils.PCMFormat
import com.rabiloo.appnote123.network.websocket.IResponseHandler
import com.rabiloo.appnote123.network.websocket.WebSocketClient
import com.rabiloo.appnote123.utils.DateString
import com.rabiloo.appnote123.utils.SharedPer
import com.rabiloo.appnote123.websocketAPI.Text
import com.simplemobiletools.commons.extensions.getFormattedDuration
import com.visualizer.amplitude.AudioRecordView
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.util.CharsetUtil
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AddNoteDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(): AddNoteDialogFragment {
            return AddNoteDialogFragment()
        }
    }

    lateinit var recorder_visualizer: AudioRecordView
    lateinit var toggle_recording_button: ImageView
    lateinit var rl_back: RelativeLayout
    lateinit var txt_recording_duration: TextView
    lateinit var title_timeDate: TextView
    lateinit var recording_done: ImageView
    lateinit var kProgressHUD: KProgressHUD

    //Edittext note
    private lateinit var edt_addNote: EditText
    var height_statusBar: Int = 0
    var streaming = true
    var microphone: AudioRecord? = null
    var ws: WebSocketClient? = null
    var timer: Timer? = null
    var isClick = "STOP"
    var valueResponse: String = ""
    var duration: Int = 0
    var recorder: MediaRecorder? = null
    var mFile: File? = null
    var currentDate: String? = null
    var currentTime: String? = null

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
        height_statusBar = getStatusBarHeight()

    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog_add_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        updateRecordingDuration(0)
        updateTimeDate()
    }

    var cout = 0
    var checkDate = ""
    var noteAdd = ""
    var isStop = true
    fun initView(view: View) {
        rl_back = view.findViewById(R.id.rl_back)
        val lp: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, height_statusBar, 0, 0)
        rl_back.layoutParams = lp
        recording_done = view.findViewById(R.id.recording_done)
        title_timeDate = view.findViewById(R.id.title_timeDate)
        edt_addNote = view.findViewById(R.id.edt_addNote)
        toggle_recording_button = view.findViewById(R.id.toggle_recording_button)
        txt_recording_duration = view.findViewById(R.id.txt_recording_duration)
        recorder_visualizer = view.findViewById(R.id.recorder_visualizer)
        recorder_visualizer.recreate()

        val handler: IResponseHandler<WebSocketFrame?> = object :
            IResponseHandler<WebSocketFrame?> {
            override fun onMessage(msg: WebSocketFrame?) {
                if (msg is TextWebSocketFrame) {
                    cout++
                    val textFrame = msg as TextWebSocketFrame
                    val gson: Gson = Gson()
                    val textWsAPI = gson.fromJson(textFrame.text(), Text::class.java)
                    valueResponse = textWsAPI.result.hypotheses.last().transcript.trim()
                    CoroutineScope(Dispatchers.Main).launch {
                        edt_addNote.setText(valueResponse + "...")
                        if (valueResponse == "ok" && isStop){
                            isStop = false
                            showProgress()
                            stop()
                            stopRecording()
                            checkDateNote(checkDate, noteAdd)
                        }
                    }
                    if (cout == 3){
                        checkDate = valueResponse
                        valueResponse = ""
                    }
                    if (cout > 3 && cout % 3 == 0 && valueResponse != "ok"){
                        noteAdd += valueResponse
                    }
                    textWsAPI.result.hypotheses.forEach {
                        Log.d("WEBSOCKET", it.transcript)
                    }

                } else {
                    Log.d("WEBSOCKET", msg.toString())
                }
            }

            override fun onFailure(cause: Throwable?) {
                Log.d("WEBSOCKET", "Fail - ${cause?.message}")
            }

            override fun onComplete() {
                Log.d("WEBSOCKET", "completed")
            }
        }
        rl_back.setOnClickListener {
            dialog?.dismiss()
        }
       ws = WebSocketClient(
            generateQueryStringUri(
                KEY.URL_WEBSOCKET_API,
                16000f,
                PCMFormat.S16LE,
                1,
                KEY.TOKEN_WEBSOCKET
            )
        )
        toggle_recording_button.setOnClickListener {
            isClick = if (isClick == "STOP") {
                "RUN"
            } else {
                "STOP"
            }
            if (isClick == "RUN") {
                audioRecording()
                checkDate = ""
                noteAdd = ""
                recording_done.visibility = View.GONE
                timer = Timer()
                updateAudioVisualizer()

                timer?.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        duration++
                        updateRecordingDuration(duration)
                    }

                }, 0, 1000)
                CoroutineScope(Dispatchers.IO).launch {
                    streaming = true
                    val sampleRate = 16000
                    val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO
                    val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
                    try {
                        var minBufferSize = AudioRecord.getMinBufferSize(
                            sampleRate,
                            channelConfig,
                            audioFormat
                        )
                        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                            minBufferSize = sampleRate * 2;
                        }
                        microphone = AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            sampleRate,
                            channelConfig,
                            audioFormat,
                            minBufferSize
                        )
                        if (microphone?.state != AudioRecord.STATE_INITIALIZED) {
                            Log.e("AUDIO", "Audio Record can't initialize!");
                            return@launch
                        }
                        microphone?.startRecording()
                        while (streaming) {
                            try {
                                val buffer =
                                    ByteArray(sampleRate * PCMFormat.S16LE.sampleSize / 32) // div 32 S16 16bit
                                var nRead: Int = 0
                                ws?.addHandler(handler)
                                withContext(Dispatchers.IO) {
                                    while (microphone?.read(buffer, 0, buffer.size).also {
                                            if (it != null) {
                                                nRead = it
                                            }
                                        } !== -1) {
                                        ws?.sendBinaryMessage(buffer, 0, nRead)
                                        delay(250)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.d("TAG", e.message.toString())
                            }
                        }
                    } catch (io: IOException) {
                        Log.d("IOException", io.printStackTrace().toString())
                    } finally {
                        ws?.sendBinaryMessage("EOS".toByteArray(CharsetUtil.UTF_8))
                    }
                }
            } else {
                showProgress()
                stop()
                stopRecording()
                checkDateNote(checkDate, noteAdd)
            }

        }
    }

    private fun stop() {
        streaming = false
        timer?.cancel()
        ws?.sendBinaryMessage("EOS".toByteArray(CharsetUtil.UTF_8))
        if (ws != null && timer != null && microphone != null) {
            microphone?.stop()
            microphone?.release()

        }
        Log.d("WEBSOCKETNOTE", "Cout = $cout ----- Date: $checkDate ---- Note: $noteAdd")
    }

    fun updateAudioVisualizer() {
//        val currentMaxAmplitude = MediaRecorder().getMediaRecorder().getMaxAmplitude()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                recorder_visualizer.update((1000..8000).random())
            }

        }, 0, 60)
    }

    private fun updateRecordingDuration(duration: Int) {
        txt_recording_duration.text = duration.getFormattedDuration()
    }

    fun updateTimeDate() {
        currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val nameDayEL = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
        currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        title_timeDate.text = "$currentTime, $nameDayEL $currentDate"
    }

    private fun generateQueryStringUri(
        url: String,
        sampleRate: Float,
        audioFormat: PCMFormat,
        channels: Int,
        token: String?
    ): String {
        return "$url?content-type=audio/x-raw,+layout=(string)interleaved,+rate=(int)${sampleRate.toInt()},+format=(string)$audioFormat,+channels=(int)$channels&token=$token"
    }

    fun audioRecording() {
        val file: File = File(requireContext().getExternalFilesDir(null)?.absolutePath)
        mFile = File(file, "audio"+System.currentTimeMillis()+".wav")
        if (!file.exists()) {
            file.mkdirs()
        }

        recorder = MediaRecorder()
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(mFile?.absolutePath)

            prepare()
            start()
        }
    }

    private fun stopRecording() {
        // stop recording and free up resources
        recorder?.apply {
            stop()
            release()
            recorder = null
        }
    }

    private fun checkDateNote(checkDate: String, noteAdd: String) {
        if (checkDate.isEmpty() && noteAdd.isEmpty()){
            Toast.makeText(requireContext(), "Không có dữ liệu, vui lòng thử lại", Toast.LENGTH_LONG).show()
            stopProgess()
            return
        }
        if (checkDate == KEY.TOMORROW){
            upLoadFile(DateString.getDateTomorrow(), noteAdd)
        }else if (checkDate == KEY.DAYAFTERTOMORROR1 || checkDate == KEY.DAYAFTERTOMORROW){
            upLoadFile(DateString.getDateAfterTomorrow(), noteAdd)
        }else if (checkDate.contains(KEY.DAY) && (checkDate.contains(KEY.MONTH))){
            val day = DateString.getDayOrMonth(checkDate, KEY.DAY)
            val month = DateString.getDayOrMonth(checkDate, KEY.MONTH)
            if (day == "-1" || month == "-1"){
                stopProgess()
                Toast.makeText(requireContext(), "Có lỗi push dữ liệu, vui lòng thử lại", Toast.LENGTH_LONG).show()
                return
            }
            val date = DateString.getDate(day, month)
            upLoadFile(date, noteAdd)
        }else{
            val note = if (noteAdd.isEmpty()){
                checkDate
            }else{
                "$checkDate $noteAdd"
            }
            currentDate?.let { upLoadFile(it, note) }

        }
    }

    fun upLoadFile(dateAdd: String, noteAdd: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val file: Uri = Uri.fromFile(mFile)
        val dat = currentDate?.replace("/", "-")
        val riversRef: StorageReference = storageRef.child("${SharedPer.getEmail(requireContext())}/$dat/audio$currentTime${System.currentTimeMillis()}.wav")
        val uploadTask = riversRef.putFile(file)

        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot?, Task<Uri?>?> { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            riversRef.downloadUrl
        }).addOnCompleteListener(OnCompleteListener<Uri?> { task ->
            if (task.isSuccessful) {
                val downloadUri: Uri? = task.result
                pushNote(downloadUri.toString(), dateAdd, noteAdd)
                Log.d("downloadUri", downloadUri.toString())
            } else {
                Toast.makeText(requireContext(), "Upload file record bị lỗi, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                stopProgess()
            }
        })
    }

    fun pushNote(downloadUri: String, dateAdd: String, noteAdd: String) {
        val db = Firebase.firestore
        val noteDb = db.collection("Note")
        noteDb.whereEqualTo("date", dateAdd).limit(1).get().addOnSuccessListener {
            if (it.isEmpty){
                val note: Note = if (dateAdd == currentDate){
                    Note("", dateAdd, currentTime, 1 )
                }else{
                    Note("", dateAdd, "--:--", 1 )
                }
                noteDb
                    .add(note)
                    .addOnSuccessListener { d ->
                        Log.d("TAG", "DocumentSnapshot Note: ${d.id}")
                        db.collection("Note").document(d.id).update("idNote", d.id).addOnSuccessListener {
                            pushDetailNote(db, d.id, downloadUri, noteAdd, dateAdd)
                        }.addOnFailureListener {
                            stopProgess()
                            Toast.makeText(requireContext(), "Push dữ liệu vào db lỗi, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        stopProgess()
                        Toast.makeText(requireContext(), "Push dữ liệu vào db lỗi, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                    }
            }else{
                val note = it.documents[0].toObject(Note::class.java)!!
                val coutNote = note.coutNote + 1
                noteDb
                    .document(note.idNote)
                    .update("coutNote", coutNote)
                    .addOnSuccessListener {
                        pushDetailNote(db, note.idNote, downloadUri, noteAdd, dateAdd)
                    }
                    .addOnFailureListener {
                        stopProgess()
                        Toast.makeText(requireContext(), "Push dữ liệu vào db lỗi, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            stopProgess()
            Toast.makeText(requireContext(), "Push dữ liệu vào db lỗi, vui lòng thử lại", Toast.LENGTH_SHORT).show()
        }
    }

    fun pushDetailNote(db: FirebaseFirestore, id: String, downloadUri: String, noteAdd: String, dateAdd: String
    ) {
        val noteDuration = duration - 1
        val detailNote: DetailNote = if (dateAdd == currentDate){
            DetailNote("", id, "",noteAdd, currentTime, noteDuration.toString(), downloadUri)
        }else{
            DetailNote("", id, currentDate,noteAdd, "--:--", noteDuration.toString(), downloadUri)
        }
        db.collection("DetailNote")
            .add(detailNote)
            .addOnSuccessListener { d ->
                Log.d("TAG", "DocumentSnapshot DetailNote: ${d.id}")
                db.collection("DetailNote").document(d.id).update("idDetailNote", d.id).addOnSuccessListener {
                    stopProgess()
                }.addOnFailureListener {
                    stopProgess()
                    Toast.makeText(requireContext(), "Push dữ liệu vào db lỗi, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                stopProgess()
                Toast.makeText(requireContext(), "Push dữ liệu vào db lỗi, vui lòng thử lại", Toast.LENGTH_SHORT).show()
            }
    }

    fun showProgress(){
       kProgressHUD =  KProgressHUD.create(requireContext())
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel("Please wait")
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.6f)
            .show();
    }

    fun stopProgess(){
        dialog?.dismiss()
        kProgressHUD.dismiss()
    }
}