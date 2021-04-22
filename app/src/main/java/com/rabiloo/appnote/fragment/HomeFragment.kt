package com.io.note.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rabiloo.appnote.R
import com.rabiloo.appnote.adapter.home.AdapterNoteHome
import com.rabiloo.appnote.adapter.home.model.ModelItemHome
import com.rabiloo.appnote.audio.AudioRecorder
import com.rabiloo.appnote.fragment.AddNoteDialogFragment
import com.rabiloo.appnote.fragment.DetailNoteDialogFragment

class HomeFragment : Fragment(), View.OnClickListener{
    //RecyclerView
    lateinit var recycler: RecyclerView

    //Add member
    lateinit var fab_record: FloatingActionButton

    //Sharepreferences
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val inflatedView = inflater.inflate(R.layout.fragment_home, container, false)
        return inflatedView
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initSharedPreferences()
        initRecycler()
        val firstTime = sharedPreferences.getBoolean("firstapp", true)
        if (firstTime) {
            val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
            editor.putBoolean("firstapp", false)
            editor.commit()
        }

    }

    override fun onResume() {
        super.onResume()
    }

    fun initView(view: View) {
        recycler = view.findViewById(R.id.recycler)
        fab_record = view.findViewById(R.id.fab_record)
        fab_record.setOnClickListener(this)
    }


    fun initSharedPreferences() {
        sharedPreferences = requireActivity().getSharedPreferences("GROUP", Context.MODE_PRIVATE)
        val nameGroup = sharedPreferences.getString("NAMEGROUP", "Group")
    }

    fun initRecycler(){
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = layoutManager
        val items: ArrayList<ModelItemHome> = ArrayList()
        items.add(ModelItemHome("1","1",1))
        items.add(ModelItemHome("1","1",1))
        items.add(ModelItemHome("1","1",1))
        items.add(ModelItemHome("1","1",1))
        recycler.adapter = AdapterNoteHome(requireContext(), items)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(
            savedInstanceState
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 102 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            recordAudio()
        }else{
            Toast.makeText(requireContext(), "Permission is denied.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.fab_record -> {
                openPemission()
            }
        }
    }

    fun openPemission(){
        val PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
        if (!hasPermissions(requireContext(), *PERMISSIONS)) {
            requestPermissions(PERMISSIONS, 102)
        } else {
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//            intent.type = "video/*"
//            startActivityForResult(intent, 200)
            recordAudio()
        }
    }

    fun recordAudio(){
        val newFragment: DialogFragment = AddNoteDialogFragment.newInstance()
        newFragment.show(requireActivity().supportFragmentManager, "dialog")
    }

}
