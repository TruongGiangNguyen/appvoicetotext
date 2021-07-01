package com.rabiloo.appnote123.ui

import android.content.res.AssetManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rabiloo.appnote123.R
import com.rabiloo.appnote123.adapter.note.AdapterNote
import com.rabiloo.appnote123.adapter.note.model.ModelItemNote
import com.rabiloo.appnote123.fragment.DetailNoteDialogFragment
import com.rabiloo.appnote123.key.KEY
import com.rabiloo.appnote123.listener.CallFunctionListener
import com.rabiloo.appnote123.listener.ItemDetailNoteListener
import com.rabiloo.appnote123.model.DetailNote
import com.rabiloo.appnote123.network.utils.PCMFormat
import com.rabiloo.appnote123.network.websocket.AsrWebSocketClient
import com.rabiloo.appnote123.network.websocket.IResponseHandler
import com.vivian.timelineitemdecoration.itemdecoration.DotItemDecoration
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import java.io.*


class ListNoteActivity: AppCompatActivity(), ItemDetailNoteListener, CallFunctionListener {
    companion object{
        var dateNote = ""
    }
    //Recycler
    lateinit var recy_listNote: RecyclerView
    var itemNotes: ArrayList<ModelItemNote> = ArrayList()
    lateinit var title_date_listNote: TextView
    lateinit var back_listNote: ImageView
    var idNote = ""
    var coutNote = 0
    private lateinit var adapter: AdapterNote


    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()

       /* val window = window;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.statusBarColor = Color.TRANSPARENT; }*/
        setContentView(R.layout.activity_list_note)
        initView()
        getExTras()
        /*val newFragment: DialogFragment = DetailNoteDialogFragment.newInstance()
        newFragment.show(supportFragmentManager, "dialog")*/
    }

    fun getExTras(){
        val bundle = intent.extras
        idNote = bundle?.getString(KEY.ID).toString()
        dateNote = bundle?.getString(KEY.DATE).toString()
        coutNote = bundle?.getInt(KEY.COUTNOTE)!!
        title_date_listNote.text = dateNote
        getDataDetailNote(idNote)
    }

   fun getDataDetailNote(idNote: String?) {
       itemNotes.clear()
       val db = Firebase.firestore
       val detailNoteDB = db.collection("DetailNote")
       detailNoteDB.whereEqualTo("idNote", idNote).get()
           .addOnSuccessListener { result ->
               for (document in result) {
                   val dNote = document.toObject(DetailNote::class.java)
                   val iNote = ModelItemNote(dNote.idDetailNote, dNote.dateFeature, dNote.time, dNote.note, dNote.durationRecord, dNote.downloadUri)
                   itemNotes.add(iNote)
               }
               initRecycler()
           }
           .addOnFailureListener { exception ->
               Toast.makeText(this, "Dữ liệu lấy về bị lỗi", Toast.LENGTH_LONG).show()
           }
   }

    fun testWebsocket(){
        val handler: IResponseHandler<WebSocketFrame?> = object : IResponseHandler<WebSocketFrame?> {
            override fun onMessage(msg: WebSocketFrame?) {
                if (msg is TextWebSocketFrame) {
                    val textFrame = msg as TextWebSocketFrame
                    Log.i("WEBSOCKET", "Json ${textFrame.text()}")
                } else {
                    Log.i("WEBSOCKET", "No Json ${msg.toString()}")
                }
            }

            override fun onFailure(cause: Throwable?) {
                Log.i("WEBSOCKET", "Fail - ${cause?.message.toString()}")
            }

            override fun onComplete() {
                Log.i("WEBSOCKET", "completed")
            }

        }

        val am: AssetManager = this.assets
        val inputStream = am.open("audio1.mp3")
        try{
            val bi: BufferedInputStream = BufferedInputStream(inputStream)
            val client = AsrWebSocketClient.newBuilder()
                .setSampleRate(16000f)
                .setAudioFormat(PCMFormat.S16LE)
                .setChannels(1)
                .setHandler(handler)
                .setToken(KEY.TOKEN_WEBSOCKET)
                .setUrl(KEY.URL_WEBSOCKET_API)
                .build()
            client.recognize(bi)
        }catch (e: Exception){
            Log.i("TAG", e.message.toString())
        }
    }

    fun getURLForResource(resourceId: Int): Uri? {
        //use BuildConfig.APPLICATION_ID instead of R.class.getPackage().getName() if both are not same
        return Uri.parse("android.resource://" + R::class.java.getPackage().name + "/" + resourceId)
    }

    fun initView(){
        recy_listNote = findViewById(R.id.recy_listNote)
        title_date_listNote = findViewById(R.id.title_date_listNote)
        back_listNote = findViewById(R.id.back_listNote)
        back_listNote.setOnClickListener {
            this.onBackPressed()
        }
    }

    fun initRecycler(){
        recy_listNote.layoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )
        val mItemDecoration = DotItemDecoration.Builder(this)
            .setOrientation(DotItemDecoration.VERTICAL) //if you want a horizontal item decoration,remember to set horizontal orientation to your LayoutManager
            .setItemStyle(DotItemDecoration.STYLE_DRAW)
            .setTopDistance(15f) //dp
            .setItemInterVal(10f) //dp
            .setItemPaddingLeft(10f) //default value equals to item interval value
            .setItemPaddingRight(10f) //default value equals to item interval value
            .setDotColor(R.color.blue_inactive)
            .setDotRadius(2) //dp
            .setDotPaddingTop(0)
            .setDotInItemOrientationCenter(false) //set true if you want the dot align center
            .setLineColor(Color.RED)
            .setLineWidth(1f) //dp
            .setEndText("END")
            .setTextColor(Color.BLACK)
            .setTextSize(10f) //sp
            .setDotPaddingText(2f) //dp.The distance between the last dot and the end text
            .setBottomDistance(20f) //you can add a distance to make bottom line longer
            .create()
        /*mItemDecoration.setSpanIndexListener { view, spanIndex ->
            Log.i("Info", "view:$view  span:$spanIndex")
            view.setBackgroundResource(if (spanIndex == 0) R.drawable.pop_left else R.drawable.pop_right)
        }*/
        recy_listNote.addItemDecoration(mItemDecoration)
        adapter = AdapterNote(this, itemNotes, this)
        recy_listNote.adapter = adapter
    }

    override fun openDialogDetail(id: String, time: String, date: String, note: String, downLoadUri: String) {
        val newFragment: DialogFragment = DetailNoteDialogFragment.newInstance(id, time, date, note, downLoadUri)
        newFragment.show(supportFragmentManager, "dialog")
    }

    override fun deleItem(id: String, posi: Int) {
        AlertDialog.Builder(this)
            .setMessage("Bạn muốn xóa ghi chú này?")
            .setCancelable(false)
            .setPositiveButton("Đồng ý"
            ) { dialog, idD ->
                dialog.dismiss()
                deleteNote(id, posi)
               }
            .setNegativeButton("Không", null)
            .show()
    }

    private fun deleteNote(id: String, posi: Int) {
        val db = Firebase.firestore
        val dNoteDb = db.collection("DetailNote")
        val dNote = db.collection("Note")
        dNoteDb.document(id).delete().addOnCompleteListener {
            if (it.isSuccessful){
                val cout = coutNote - 1
                dNote.document(idNote).update("coutNote", cout)
                Toast.makeText(this, "Xóa thành công", Toast.LENGTH_LONG).show()
                itemNotes.removeAt(posi)
                adapter.notifyItemRemoved(posi);
                adapter.notifyItemRangeChanged(posi, itemNotes.size);

            }
        }.addOnFailureListener {
            Toast.makeText(this, "Xóa thất bại, vui lòng thử lại", Toast.LENGTH_LONG).show()
        }
    }

    override fun run() {
        itemNotes.clear()
        val db = Firebase.firestore
        val detailNoteDB = db.collection("DetailNote")
        detailNoteDB.whereEqualTo("idNote", idNote).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val dNote = document.toObject(DetailNote::class.java)
                    val iNote = ModelItemNote(dNote.idDetailNote, dNote.dateFeature, dNote.time, dNote.note, dNote.durationRecord, dNote.downloadUri)
                    itemNotes.add(iNote)
                }
               adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Dữ liệu lấy về bị lỗi", Toast.LENGTH_LONG).show()
            }
    }
}