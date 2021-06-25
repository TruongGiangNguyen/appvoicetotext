package com.rabiloo.appnote123.fragment

import android.app.Dialog
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rabiloo.appnote123.R
import com.rabiloo.appnote123.listener.CallFunctionListener
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.getContrastColor
import java.io.IOException
import java.util.*


class DetailNoteDialogFragment: DialogFragment(), MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {
    companion object {
        var dId = ""
        var dTime = ""
        var dDate = ""
        var dNote = ""
        var dDownLoadUri = ""

        fun newInstance(id: String, time: String, date: String, note: String, downLoadUri: String): DetailNoteDialogFragment {
            dId = id
            dTime = time
            dDate = date
            dNote = note
            dDownLoadUri = downLoadUri
            return DetailNoteDialogFragment()
        }
    }
    //Back
    lateinit var ll_back_detail: LinearLayout
    var height_statusBar: Int = 0
    //More
    lateinit var more_Note: ImageView
    //RelativeLayout
    lateinit var rl_top: RelativeLayout
    lateinit var rl_backtNote: RelativeLayout
    lateinit var rl_control_editNote: RelativeLayout
    //Textview & Edittext - title, note
    lateinit var title_timeDate_detail: TextView
    lateinit var edit_timeDate_detail: EditText
    lateinit var txt_detailNote: TextView
    lateinit var edt_editNote: EditText
    //LinearLayout
    lateinit var ll_undo: LinearLayout
    lateinit var ll_edit: LinearLayout
    //Play media
    lateinit var toggle_play_button: ImageView
    //Media
    private var mMediaPlayer:MediaPlayer? = null
    var isPlay = false
    //Textview & Seekbar - current, duration
    lateinit var txt_player_progress_current: TextView
    lateinit var txt_player_progress_max: TextView
    lateinit var seekbar: SeekBar
    private val mHandler: Handler = Handler()
    private var mRunnable: Runnable? = null
    private var progressTimer = Timer()
    private lateinit var callFunctionListener: CallFunctionListener

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
        if (activity is CallFunctionListener) callFunctionListener = (activity as CallFunctionListener?)!!
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
        return inflater.inflate(R.layout.fragment_detail_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initRunable()
        onClick()
    }

    fun initView(view: View){
        rl_top = view.findViewById(R.id.rl_top)
        rl_control_editNote = view.findViewById(R.id.rl_control_editNote)
        title_timeDate_detail = view.findViewById(R.id.title_timeDate_detail)
        edit_timeDate_detail = view.findViewById(R.id.edit_timeDate_detail)
        txt_detailNote = view.findViewById(R.id.txt_detailNote)
        edt_editNote = view.findViewById(R.id.edt_editNote)
        ll_undo = view.findViewById(R.id.ll_undo)
        ll_edit = view.findViewById(R.id.ll_edit)
        ll_back_detail = view.findViewById(R.id.ll_back_detail)
        rl_backtNote = view.findViewById(R.id.rl_backtNote)
        toggle_play_button = view.findViewById(R.id.toggle_play_button)
        txt_player_progress_current = view.findViewById(R.id.txt_player_progress_current)
        txt_player_progress_max = view.findViewById(R.id.txt_player_progress_max)
        seekbar = view.findViewById(R.id.txt_player_seekbar)

        if (dDate != ""){
            title_timeDate_detail.text = "$dTime, Ngày $dDate"
            edit_timeDate_detail.setText("$dTime, Ngày $dDate")

        }else{
            title_timeDate_detail.text = dTime
            edit_timeDate_detail.setText(dTime)
        }
        txt_detailNote.text = dNote
        edt_editNote.setText(dNote)

        //Margin top = height status bar
        val lp: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, height_statusBar, 0, 0)
        rl_top.layoutParams = lp
        more_Note = view.findViewById(R.id.more_Note)
    }

    fun initRunable(){
        mRunnable = Runnable {
            if (mMediaPlayer != null) {
                val mCurrentPosition = mMediaPlayer!!.currentPosition
                txt_player_progress_current.text = milliSecondsToTimer(mCurrentPosition.toLong())
                seekbar.progress = mCurrentPosition / 1000
                mHandler.postDelayed(mRunnable!!, 100) //0.1s
            }
        }
    }

    fun onClick(){
        //More
        more_Note.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.bottom_sheet_modal, null)
            val edt_edit_note: LinearLayout = view.findViewById(R.id.edt_edit_note)
            val share_note: LinearLayout = view.findViewById(R.id.share_note)
            val delete_note: LinearLayout = view.findViewById(R.id.delete_note)
            val bottomSheetDialog = BottomSheetDialog(
                    requireActivity(),
                    R.style.AppBottomSheetDialogTheme
            )
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()
            onClickDialog(edt_edit_note, share_note, delete_note, bottomSheetDialog)

        }
        //Done edit note
        ll_edit.setOnClickListener {
            rl_backtNote.visibility = View.VISIBLE
            title_timeDate_detail.visibility = View.VISIBLE
            txt_detailNote.visibility = View.VISIBLE
            txt_detailNote.text = edt_editNote.text.toString()

            rl_control_editNote.visibility = View.GONE
            edit_timeDate_detail.visibility = View.GONE
            edt_editNote.visibility = View.GONE

            editNote()
            closeKeyboard()

        }
        //Undo
        ll_undo.setOnClickListener {
            if (dDate != ""){
                title_timeDate_detail.text = "$dTime, Ngày $dDate"
                edit_timeDate_detail.setText("$dTime, Ngày $dDate")

            }else{
                title_timeDate_detail.text = dTime
                edit_timeDate_detail.setText(dTime)
            }
            txt_detailNote.text = dNote
            edt_editNote.setText(dNote)
            edt_editNote.setSelection(edt_editNote.text.length)
        }
        //Play media
        toggle_play_button.setOnClickListener {
            if (txt_player_progress_current.text == txt_player_progress_max.text){
                seekbar.max = 0
            }
            if (seekbar.max == 0){
                playMedia()
            }else{
                togglePlayPause()
            }
        }
        //Seekbar
        seekbar.setOnSeekBarChangeListener(this)
        //onBackPress
        ll_back_detail.setOnClickListener {
            dialog?.let {
                if (it.isShowing){
                    it.dismiss()
                    callFunctionListener.run()
                }
            }
        }
    }

    private fun editNote(){
        val db = Firebase.firestore
        val dNoteDb = db.collection("DetailNote")
        dNoteDb.document(dId).update("note", edt_editNote.text.toString().trim()).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(requireContext(), "Chỉnh sửa thành công", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Chỉnh sửa lỗi, vui lòng thử lại", Toast.LENGTH_LONG).show()
        }
    }

    private fun togglePlayPause() {
        if (mMediaPlayer?.isPlaying == true) {
            pausePlayback()
        } else {
            resumePlayback()
        }
    }

    private fun pausePlayback() {
        mMediaPlayer?.pause()
        toggle_play_button.setImageDrawable(getToggleButtonIcon(false))

    }

    private fun resumePlayback() {
        mMediaPlayer?.start()
        toggle_play_button.setImageDrawable(getToggleButtonIcon(true))
        setupProgressTimer()
    }

    private fun setupProgressTimer() {
        progressTimer.cancel()
        progressTimer = Timer()
        progressTimer.scheduleAtFixedRate(getProgressUpdateTask(), 0, 1000)
    }

    private fun getProgressUpdateTask() = object : TimerTask() {
        override fun run() {
            Handler(Looper.getMainLooper()).post {
                if (mMediaPlayer != null) {
                    val mCurrentPosition = mMediaPlayer!!.currentPosition
                    txt_player_progress_current.text = milliSecondsToTimer(mCurrentPosition.toLong())
                    seekbar.progress = mCurrentPosition / 1000
                }
            }
        }
    }

    private fun getToggleButtonIcon(isPlaying: Boolean): Drawable? {
        val drawable = if (isPlaying) R.drawable.ic_pause_vector else R.drawable.ic_play_vector
        return context?.getAdjustedPrimaryColor()?.getContrastColor()?.let { resources.getColoredDrawableWithColor(drawable, it) }
    }

    fun playMedia(){
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()
            mMediaPlayer?.setOnCompletionListener(this)
        }
        try {
            mMediaPlayer?.reset()
            mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

            val descriptor: AssetFileDescriptor = requireActivity().assets.openFd("audio1.mp3")
//            mMediaPlayer?.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
            mMediaPlayer?.setDataSource(requireContext(), Uri.parse(dDownLoadUri))
            descriptor.close()

            mMediaPlayer?.prepare()
            mMediaPlayer?.setVolume(1f, 1f)

            mMediaPlayer?.setOnPreparedListener{
                toggle_play_button.setImageDrawable(getToggleButtonIcon(true))
                txt_player_progress_max.text = milliSecondsToTimer(getDuration().toLong())
                seekbar.max = getDuration() / 1000
                setupProgressTimer()
                play()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun play() {

        if (mMediaPlayer != null && !mMediaPlayer!!.isPlaying && (getCurrentPosition() != getDuration())) {
            mMediaPlayer!!.start()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        seekbar.progress = seekbar.max
        txt_player_progress_current.text = txt_player_progress_max.text
        progressTimer.cancel()
        toggle_play_button.setImageDrawable(getToggleButtonIcon(false))
        stopPlay()
    }

    fun stopPlay(){
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer?.reset()
                mMediaPlayer?.release()
                mMediaPlayer = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /*
     * Get current position of the played audio file
     */
    fun getCurrentPosition(): Int {
        return if (mMediaPlayer != null) {
            mMediaPlayer!!.currentPosition
        } else 0
    }

    /*
     * Get total duration of the audio file
     */
    fun getDuration(): Int {
        return if (mMediaPlayer != null) {
            mMediaPlayer!!.duration
        } else 0
    }

    /*
     * Set current position of the played audio file
     */
    fun setCurrentPosition(progress: Int) {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.seekTo(progress)
        }
    }

    fun milliSecondsToTimer(milliseconds: Long): String {
        var finalTimerString = ""
        var secondsString = ""
        var minutesString = ""

        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }
        minutesString = if (minutes < 10) {
            "0$minutes"
        }else{
            "" + minutes
        }
        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutesString:$secondsString"

        // return timer string
        return finalTimerString
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            updateAudioCompletedTime(progress * 1000)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    fun updateAudioCompletedTime(newTime: Int){
        if (mMediaPlayer != null) {
            mMediaPlayer?.seekTo(newTime)
        }
    }

    fun onClickDialog(
            edt_edit_note: LinearLayout,
            share_note: LinearLayout,
            delete_note: LinearLayout,
            bottomSheetDialog: BottomSheetDialog
    ){
        edt_edit_note.setOnClickListener {
            if (bottomSheetDialog.isShowing){
                bottomSheetDialog.dismissWithAnimation = true
                bottomSheetDialog.dismiss()
            }
            handelEditNote()
        }
        delete_note.setOnClickListener {
            if (bottomSheetDialog.isShowing){
                bottomSheetDialog.dismissWithAnimation = true
                bottomSheetDialog.dismiss()
            }
            AlertDialog.Builder(requireContext())
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes"
                ) { dialog, id ->
                    dialog.dismiss()
                    deleNote()
                }
                .setNegativeButton("No", null)
                .show()

        }
    }

    fun handelEditNote(){
        rl_backtNote.visibility = View.GONE
//        title_timeDate_detail.visibility = View.GONE
        txt_detailNote.visibility = View.GONE

        rl_control_editNote.visibility = View.VISIBLE
//        edit_timeDate_detail.visibility = View.VISIBLE
        edt_editNote.visibility = View.VISIBLE
    }

    private fun deleNote(){
        val db = Firebase.firestore
        val dNoteDb = db.collection("DetailNote")
        dNoteDb.document(dId).delete().addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(requireContext(), "Xóa thành công", Toast.LENGTH_LONG).show()
                dialog?.let {
                    if (it.isShowing){
                        it.dismiss()
                        callFunctionListener.run()
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Xóa thất bại, vui lòng thử lại", Toast.LENGTH_LONG).show()
        }
    }

    private fun closeKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroy() {
        //Stop timer()
        progressTimer.cancel()
        stopPlay()
        super.onDestroy()
    }

}