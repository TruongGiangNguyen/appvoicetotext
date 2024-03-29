package com.rabiloo.appnote123.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.quinny898.library.persistentsearch.SearchBox
import com.rabiloo.appnote123.R
import com.rabiloo.appnote123.adapter.home.AdapterNoteHome
import com.rabiloo.appnote123.adapter.home.model.ModelItemHome
import com.rabiloo.appnote123.datepickerdialog.DatePicker
import com.rabiloo.appnote123.key.KEY
import com.rabiloo.appnote123.listener.ItemNoteHomeListener
import com.rabiloo.appnote123.model.DetailNote
import com.rabiloo.appnote123.model.Note
import com.rabiloo.appnote123.ui.ListNoteActivity
import com.rabiloo.appnote123.utils.DateString
import com.rabiloo.appnote123.utils.SharedPer
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment(), View.OnClickListener, ItemNoteHomeListener {
    //RecyclerView
    lateinit var recycler: RecyclerView
    //Add member
    lateinit var fab_record: FloatingActionButton
    //Sharepreferences
    lateinit var sharedPreferences: SharedPreferences
    //SearchView
    lateinit var search_home: SearchBox
    lateinit var search_view: SearchView
    //DatePickerDialog
    lateinit var datePicker: DatePicker
    //Caculate
    lateinit var calendar_home: ImageView
    lateinit var swipeToRefresh: SwipeRefreshLayout
    lateinit var adapter: AdapterNoteHome

    val items: ArrayList<ModelItemHome> = ArrayList()
    var isSwipe = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val inflatedView = inflater.inflate(R.layout.fragment_home, container, false)
        return inflatedView
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        configSearchView()
        getData()
    }

    fun initView(view: View) {
        search_home = view.findViewById(R.id.search_home)
        calendar_home = view.findViewById(R.id.calendar_home)
        recycler = view.findViewById(R.id.recycler)
        fab_record = view.findViewById(R.id.fab_record)
        swipeToRefresh = view.findViewById(R.id.swipeToRefresh)
        search_view = view.findViewById(R.id.search_view)
        swipeToRefresh.setOnRefreshListener {
            isSwipe = true
            getData()
        }
        fab_record.setOnClickListener(this)
        calendar_home.setOnClickListener(this)
    }

    fun configSearchView() {
        search_home.enableVoiceRecognition(this)
        search_home.setLogoText(getString(R.string.hint_search))
        search_home.setLogoTextColor(R.color.black)
        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // filter recycler view when query submitted
                adapter.filter?.filter(query)
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                // filter recycler view when text is changed
                adapter.filter?.filter(query)
                return false
            }
        })
    }

    fun getData() {
        items.clear()
        val db = Firebase.firestore
        val noteDB = db.collection("Note")
        noteDB.whereEqualTo("email", SharedPer.getEmail(requireContext())).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val note = document.toObject(Note::class.java)
                    val nameDayEL = DateString.getDayofWeek(note.date)
                    val date = "$nameDayEL, Ngày ${note.date}"
                    val mItem =
                        ModelItemHome(note.idNote, date, note.timeCreate, note.coutNote, note.date)
                    items.add(mItem)
                    Log.d("TAG", "${document.id} => ${document.data}")
                }
                sortDate()
                if (!isSwipe) {
                    initRecycler()
                } else {
                    dataChangeRecy()
                }

            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Dữ liệu lấy về bị lỗi", Toast.LENGTH_LONG).show()
            }
    }

    fun sortDate() {
        items.sortWith(Comparator { item1, item2 ->
            val date1 = stringToDate(item1.sortDate)
            val date2 = stringToDate(item2.sortDate)
            if (date1 != null && date2 != null) {
                val b1: Boolean
                val b2: Boolean
                if (false) {
                    b1 = date2.after(date1)
                    b2 = date2.before(date1)
                } else {
                    b1 = date1.after(date2)
                    b2 = date1.before(date2)
                }
                if (b1 != b2) {
                    if (b1) {
                        return@Comparator -1
                    }
                    if (!b1) {
                        return@Comparator 1
                    }
                }
            }
            0
        })
    }

    fun initRecycler() {
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = layoutManager
        adapter = AdapterNoteHome(requireContext(), items, this)
        recycler.adapter = adapter
    }

    fun dataChangeRecy() {
        adapter.notifyDataSetChanged()
        swipeToRefresh.isRefreshing = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 102 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recordAudio()
        } else {
            Toast.makeText(requireContext(), "Permission is denied.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_record -> {
                openPemission()
            }
            R.id.calendar_home -> {
                openDatePickerDialog()
            }
        }
    }

    fun openPemission() {
        val PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
        if (!hasPermissions(requireContext(), *PERMISSIONS)) {
            requestPermissions(PERMISSIONS, 102)
        } else {
            recordAudio()
        }
    }

    fun openDatePickerDialog() {
        val dateListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            var monthNew = ""
            var dayNew = ""
            var monthNormal = month + 1
            if (monthNormal < 10) {
                monthNew = "0$monthNormal"
            } else {
                monthNew = "$monthNormal"
            }
            if (dayOfMonth < 10) {
                dayNew = "0$dayOfMonth"
            } else {
                dayNew = "$dayOfMonth"
            }
            val date = "$dayNew/$monthNew/$year"
            findDatePickerDialog(date)
            println("y = $year, m = $monthNew, d = $dayOfMonth")
        }
        datePicker = DatePicker(requireContext(), dateListener)
        datePicker.open()
    }

    fun findDatePickerDialog(date: String) {
        val db = Firebase.firestore
        val noteD = db.collection("Note")
        noteD.whereEqualTo("date", date).limit(1).get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(
                        requireContext(),
                        "Không có dữ liệu ngày $date",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    items.clear()
                    for (document in result) {
                        val note = document.toObject(Note::class.java)
                        val nameDayEL =
                            SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(note.date))
                        val date = "$nameDayEL, Ngày ${note.date}"
                        val mItem = ModelItemHome(
                            note.idNote,
                            date,
                            note.timeCreate,
                            note.coutNote,
                            note.date
                        )
                        items.add(mItem)
                    }
                    sortDate()
                    adapter.setFind(true)
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Dữ liệu tìm kiếm bị lỗi", Toast.LENGTH_LONG)
                    .show()
            }
    }

    fun recordAudio() {
        val newFragment: DialogFragment = AddNoteDialogFragment.newInstance()
        newFragment.show(requireActivity().supportFragmentManager, "dialog")
    }

    override fun onClickItem(id: String, date: String, countNote: Int) {
        val intent = Intent(requireContext(), ListNoteActivity::class.java)
        intent.putExtra(KEY.ID, id)
        intent.putExtra(KEY.DATE, date)
        intent.putExtra(KEY.COUTNOTE, countNote)
        startActivity(intent)
    }

    override fun deleteItem(id: String, position: Int) {
        AlertDialog.Builder(requireContext())
            .setMessage("Bạn muốn xóa ngày ghi chú này?")
            .setCancelable(false)
            .setPositiveButton(
                "Đồng ý"
            ) { dialog, idD ->
                dialog.dismiss()

                val db = Firebase.firestore
                val dNote = db.collection("Note")
                val dNoteDb = db.collection("DetailNote")

                dNote.document(id).delete().addOnCompleteListener {
                    if (it.isSuccessful) {
                        dNoteDb.whereEqualTo("idNote", id).get().addOnSuccessListener { result ->
                            if (result.isEmpty) {
                                items.removeAt(position)
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, items.size);
                            } else {
                                for (document in result) {
                                    val dNote = document.toObject(DetailNote::class.java)
                                    dNoteDb.document(dNote.idDetailNote).delete()
                                }
                                items.removeAt(position)
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, items.size);
                            }
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Xóa thất bại, vui lòng thử lại",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton("Không", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode === SearchBox.VOICE_RECOGNITION_CODE && resultCode === Activity.RESULT_OK) {
            val matches: ArrayList<String> =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
            val text = matches[0]
            var d = ""
            var m = ""
            if (text.contains("ngày") && text.contains("tháng") && text.contains("năm")) {
                val day = DateString.getDayOrMonthYear(text, DateString.day)
                val month = DateString.getDayOrMonthYear(text, DateString.month)
                val year = DateString.getDayOrMonthYear(text, DateString.year)
                if (day.toInt() < 10) {
                    d = "0$day"
                } else {
                    d = day
                }
                if (month.toInt() < 10) {
                    m = "0$month"
                } else {
                    m = month
                }
                val dayFull = DateString.getDateYear(d, m, year)
                findVoiceYear(dayFull)
            } else if (text.contains("ngày") && text.contains("tháng")) {
                val day = DateString.getDayOrMonthVoiceGG(text, DateString.day)
                val month = DateString.getDayOrMonthVoiceGG(text, DateString.month)
                if (day.toInt() < 10) {
                    d = "0$day"
                } else {
                    d = day
                }
                if (month.toInt() < 10) {
                    m = "0$month"
                } else {
                    m = month
                }
                val dayFull = DateString.getDate(d, m)
                findVoiceYear(dayFull)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Đầu vào không đúng, vui lòng thử lại",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun findVoiceYear(dayFull: String) {
        val db = Firebase.firestore
        val noteD = db.collection("Note")
        noteD.whereEqualTo("date", dayFull).limit(1).get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(
                        requireContext(),
                        "Không có dữ liệu ngày $dayFull",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    items.clear()
                    for (document in result) {
                        val note = document.toObject(Note::class.java)
                        val nameDayEL =
                            SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(note.date))
                        val date = "$nameDayEL, Ngày ${note.date}"
                        val mItem = ModelItemHome(
                            note.idNote,
                            date,
                            note.timeCreate,
                            note.coutNote,
                            note.date
                        )
                        items.add(mItem)
                    }
                    sortDate()
                    adapter.setFind(true)
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Dữ liệu tìm kiếm bị lỗi", Toast.LENGTH_LONG)
                    .show()
            }
    }

    fun stringToDate(strDate: String?): Date? {
        if (strDate == null) return null
        val format = SimpleDateFormat("dd/MM/yyyy")
        var date: Date? = null
        try {
            date = format.parse(strDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date
    }

}
