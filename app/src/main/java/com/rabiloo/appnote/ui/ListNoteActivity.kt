package com.rabiloo.appnote.ui

import android.content.res.AssetManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.io.note.key.KEY
import com.rabiloo.appnote.R
import com.rabiloo.appnote.adapter.note.AdapterNote
import com.rabiloo.appnote.adapter.note.OpenDialogFragment
import com.rabiloo.appnote.adapter.note.model.ModelItemNote
import com.rabiloo.appnote.fragment.DetailNoteDialogFragment
import com.rabiloo.appnote.network.utils.PCMFormat
import com.rabiloo.appnote.network.websocket.AsrWebSocketClient
import com.rabiloo.appnote.network.websocket.IResponseHandler
import com.vivian.timelineitemdecoration.itemdecoration.DotItemDecoration
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import java.io.*


class ListNoteActivity: AppCompatActivity(), OpenDialogFragment {
    //Recycler
    lateinit var recy_listNote: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_list_note)
        initView()
        initRecycler()
//        testWebsocket()
//        testAPI()
        val newFragment: DialogFragment = DetailNoteDialogFragment.newInstance()
        newFragment.show(supportFragmentManager, "dialog")
    }

    fun testAPI(){

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

    }

    fun initRecycler(){
        val itemNotes: ArrayList<ModelItemNote> = ArrayList()
        itemNotes.add(ModelItemNote("", "09:00 PM", "value equals to item interval value", "00:30"))
        itemNotes.add(
            ModelItemNote(
                "25/06/2021",
                "09:00 PM",
                "value equals to item interval value",
                "00:30"
            )
        )
        itemNotes.add(ModelItemNote("", "09:00 PM", "value equals to item interval value", "00:30"))
        itemNotes.add(ModelItemNote("", "09:00 PM", "value equals to item interval value", "00:30"))
        itemNotes.add(
            ModelItemNote(
                "25/06/2021",
                "09:00 PM",
                "value equals to item interval value",
                "00:30"
            )
        )
        itemNotes.add(
            ModelItemNote(
                "25/06/2021",
                "09:00 PM",
                "value equals to item interval value",
                "00:30"
            )
        )
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
        recy_listNote.adapter = AdapterNote(this, itemNotes, this)
    }

    override fun open() {
        val newFragment: DialogFragment = DetailNoteDialogFragment.newInstance()
        newFragment.show(supportFragmentManager, "dialog")
    }
}