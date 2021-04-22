package com.rabiloo.appnote.adapter.note

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.rabiloo.appnote.R
import com.rabiloo.appnote.adapter.note.model.ModelItemNote

class AdapterNote(val context: Context, val itemNotes: ArrayList<ModelItemNote>, val openDialogFragment: OpenDialogFragment): RecyclerView.Adapter<AdapterNote.ViewHolder>() {
    val TYPE0 = 0
    val TYPE1 = 1 // Date



    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val dateNote = itemView.findViewById<TextView>(R.id.txt_date_listNote)
        val timeNote = itemView.findViewById<TextView>(R.id.txt_time_listNode)
        val contentNote = itemView.findViewById<TextView>(R.id.txt_contentNode)
        val timeRecord = itemView.findViewById<TextView>(R.id.txt_timeRecord)
    }

    override fun getItemViewType(position: Int): Int {
        if(itemNotes[position].date == ""){
            return TYPE0
        }else{
            return TYPE1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View? = null
        when(viewType){
            TYPE0 -> {
                view = LayoutInflater.from(context).inflate(
                    R.layout.item_recycler_list_note,
                    parent,
                    false
                )
            }
            TYPE1 -> {
                view = LayoutInflater.from(context).inflate(
                    R.layout.item_recycler_list_note_date,
                    parent,
                    false
                )
            }
        }
        return ViewHolder(view!!)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.dateNote.text = itemNotes[position].date
        holder.timeNote.text = itemNotes[position].time
        holder.contentNote.text = itemNotes[position].content
        holder.timeRecord.text = itemNotes[position].timeRecord

        holder.itemView.setOnClickListener {
            openDialogFragment.open()
        }
    }

    override fun getItemCount(): Int {
        return itemNotes.size
    }
}