package com.rabiloo.appnote.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.rabiloo.appnote.R


class DetailNoteDialogFragment: DialogFragment() {
    companion object {
        fun newInstance(): DetailNoteDialogFragment {
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
        return inflater.inflate(R.layout.fragment_detail_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
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
        //Margin top = height status bar
        val lp: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, height_statusBar, 0, 0)
        rl_top.layoutParams = lp
        more_Note = view.findViewById(R.id.more_Note)
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

            rl_control_editNote.visibility = View.GONE
            edit_timeDate_detail.visibility = View.GONE
            edt_editNote.visibility = View.GONE

            closeKeyboard()

        }
        //Undo
        ll_undo.setOnClickListener {

        }
        //onBackPress
        ll_back_detail.setOnClickListener {
            dialog?.let {
                if (it.isShowing){
                    it.dismiss()
                }
            }
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
    }

    fun handelEditNote(){
        rl_backtNote.visibility = View.GONE
        title_timeDate_detail.visibility = View.GONE
        txt_detailNote.visibility = View.GONE

        rl_control_editNote.visibility = View.VISIBLE
        edit_timeDate_detail.visibility = View.VISIBLE
        edt_editNote.visibility = View.VISIBLE
    }

    private fun closeKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}