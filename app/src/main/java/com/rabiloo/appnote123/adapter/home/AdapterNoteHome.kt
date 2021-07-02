package com.rabiloo.appnote123.adapter.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.rabiloo.appnote123.R
import com.rabiloo.appnote123.adapter.home.model.ModelItemHome
import com.rabiloo.appnote123.listener.ItemNoteHomeListener
import com.rabiloo.appnote123.model.feature.Contact


class AdapterNoteHome(val context: Context, var items: ArrayList<ModelItemHome>, val itemNoteHomeListener: ItemNoteHomeListener): RecyclerView.Adapter<AdapterNoteHome.ViewHodel>(), Filterable {
    class ViewHodel(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val swipe_home: SwipeLayout = itemView.findViewById(R.id.swipe_home)

        val imgDelete_home: ImageView = itemView.findViewById(R.id.imgDelete_home)

        val txt_date_home: TextView = itemView.findViewById(R.id.txt_date_home)
        val txt_countNote_home: TextView = itemView.findViewById(R.id.txt_countNote_home)
        val txt_time_home: TextView = itemView.findViewById(R.id.txt_time_home)
    }


    private var isFind = false
    private var itemsFilter: ArrayList<ModelItemHome> = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHodel {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_home, parent, false)
        return ViewHodel(view)
    }

    fun setFind(isFind: Boolean) {
        this.isFind = isFind
    }

    override fun onBindViewHolder(holder: ViewHodel, position: Int) {
        holder.swipe_home.showMode = SwipeLayout.ShowMode.LayDown

        val item = items[position]
        holder.txt_date_home.text = item.date
        holder.txt_countNote_home.text = item.countNote.toString() + " ghi chú"
        holder.txt_time_home.text = item.time

        if (isFind){
            holder.txt_date_home.setTextColor(context.getColor(R.color.yellow))
            isFind = !isFind
        }else{
            holder.txt_date_home.setTextColor(context.getColor(R.color.black))
            holder.txt_date_home.text = item.date
        }
        // Onclick item
        holder.swipe_home.surfaceView.setOnClickListener {
            itemNoteHomeListener.onClickItem(item.idNote, item.date, item.countNote)
        }
        //Delete
        holder.imgDelete_home.setOnClickListener {
            itemNoteHomeListener.deleteItem(item.idNote, position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getFilter(): Filter? {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    items = itemsFilter
                } else {
                    val filteredList: ArrayList<ModelItemHome> = ArrayList()
                    for (row in itemsFilter) {
                        if (row.date.toLowerCase().contains(charString.toLowerCase()) || row.date.contains(charSequence)) {
                            filteredList.add(row)
                        }
                    }
                    items = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = items
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                items = filterResults.values as ArrayList<ModelItemHome>
                notifyDataSetChanged()
            }
        }
    }
}