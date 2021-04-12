package com.rabiloo.appnote.adapter.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.rabiloo.appnote.R
import com.rabiloo.appnote.adapter.home.model.ModelItemHome

class AdapterNoteHome(val context: Context, val items: ArrayList<ModelItemHome>): RecyclerView.Adapter<AdapterNoteHome.ViewHodel>() {
    class ViewHodel(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val swipe_home: SwipeLayout = itemView.findViewById(R.id.swipe_home)

        val imgDelete_home: ImageView = itemView.findViewById(R.id.imgDelete_home)

        val txt_date_home: TextView = itemView.findViewById(R.id.txt_date_home)
        val txt_countNote_home: TextView = itemView.findViewById(R.id.txt_countNote_home)
        val txt_time_home: TextView = itemView.findViewById(R.id.txt_time_home)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHodel {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false)
        return ViewHodel(view)
    }

    override fun onBindViewHolder(holder: ViewHodel, position: Int) {
        holder.swipe_home.showMode = SwipeLayout.ShowMode.LayDown
        // Onclick item
        holder.swipe_home.surfaceView.setOnClickListener {
            println("Onclick $position")
        }
        //Delete
        holder.imgDelete_home.setOnClickListener {
            println("Detele $position")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}