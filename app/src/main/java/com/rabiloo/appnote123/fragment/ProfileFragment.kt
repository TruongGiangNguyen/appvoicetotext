package com.io.note.fragment

import android.Manifest
import android.app.Dialog
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.github.zagum.speechrecognitionview.RecognitionProgressView
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.rabiloo.appnote123.R
import com.rabiloo.appnote123.key.KEY
import com.rabiloo.appnote123.model.feature.Application
import com.rabiloo.appnote123.model.feature.Contact
import com.rabiloo.appnote123.ui.LoginActivity
import com.shaishavgandhi.loginbuttons.GoogleButton
import java.util.*
import java.util.regex.Pattern


class ProfileFragment : Fragment() {
    private val permissions = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.VIBRATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    private var speechRecognizer: SpeechRecognizer? = null

    private var btnListen: ImageButton? = null
    private var recognitionProgressView: RecognitionProgressView? = null
    private lateinit var signOutGg: GoogleButton

    var contacts: List<Contact>? = null
    var apps: List<Application>? = null

    private var isRecognitionSpeech = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val inflatedView = inflater.inflate(R.layout.fragment_profile, container, false)
        return inflatedView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        onClick()
        requestPermission()
    }

    /**
     * Check permission
     */
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val remainingPermissions: MutableList<String> = ArrayList()
            for (permission in permissions) {
                if (checkSelfPermission(
                        requireContext(),
                        permission
                    ) !== android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    remainingPermissions.add(permission)
                }
            }
            if (remainingPermissions.size > 0) {
                requestPermissions(remainingPermissions.toTypedArray(), 101)
            }else{
               init()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }

    fun initView(view: View) {
        btnListen = view.findViewById(R.id.btnListen)
        recognitionProgressView = view.findViewById(R.id.recognition_view)
        signOutGg = view.findViewById(R.id.sign_out_gg)

    }

    fun init() {
        contacts = GetContactsIntoArrayList()
        apps = getAllApplicationInPhone()
        isRecognitionSpeech = true
        setUiRecognition()
    }

    fun onClick() {
        btnListen!!.setOnClickListener {
            startRecognition() }

        signOutGg.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val mGoogleSignInClient: GoogleSignInClient
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(KEY.WEB_CLIENT_ID)
                .requestEmail()
                .build()
            mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            mGoogleSignInClient.signOut().addOnSuccessListener {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        }
    }

    private fun startRecognition() {
        btnListen!!.visibility = View.GONE
        recognitionProgressView!!.play()
        recognitionProgressView!!.visibility = View.VISIBLE
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, requireContext().packageName)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi")
        speechRecognizer!!.startListening(intent)
    }

    /**
     * get list contact in phone
     */
    fun GetContactsIntoArrayList(): List<Contact>? {
        val contacts: MutableList<Contact> = ArrayList()
        val cursor = requireContext().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        var name: String
        var phonenumber: String
        var nameNumber: String
        if (cursor != null) {
            while (cursor.moveToNext()) {
                name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                nameNumber = name
                name = name.toLowerCase()
                phonenumber =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                Log.d("CONTACT", "$name : $phonenumber")
                contacts.add(Contact(name, phonenumber, nameNumber))
            }
        }
        cursor?.close()
        return contacts
    }

    /**
     * get All Application in Phone
     */
    private fun getAllApplicationInPhone(): List<Application>? {
        val apps: MutableList<Application> = ArrayList()
        apps.add(Application("facebook", "com.facebook.katana"))
        apps.add(Application("youtube", "com.google.android.youtube"))
        apps.add(Application("instagram", "com.instagram.android"))
        apps.add(Application("nhạc", "com.zing.mp3"))
        apps.add(Application("messenger", "com.facebook.orca"))
        apps.add(Application("map", "com.google.android.apps.maps"))
        apps.add(Application("bản đồ", "com.google.android.apps.maps"))
        val manager: PackageManager = requireContext().packageManager
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)
        val availableActivities = manager.queryIntentActivities(i, 0)
        for (resolveInfo in availableActivities) {
            var name = resolveInfo.loadLabel(manager) as String
            name = name.toLowerCase()
            val packageName = resolveInfo.activityInfo.packageName
            val app = Application(name, packageName)
            apps.add(app)
        }
        return apps
    }

    private fun setUiRecognition() {
        // setup Speech Recognition
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        recognitionProgressView!!.setSpeechRecognizer(speechRecognizer)
        recognitionProgressView!!.setRecognitionListener(object : RecognitionListenerAdapter() {
            override fun onResults(results: Bundle) {
                finishRecognition()
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches!![0]
                processing_text(text)
            }
        })
        recognitionProgressView!!.setOnClickListener {
            finishRecognition()
            speechRecognizer?.stopListening()
        }
        val colors = intArrayOf(
            ContextCompat.getColor(requireContext(), R.color.color1),
            ContextCompat.getColor(requireContext(), R.color.color2),
            ContextCompat.getColor(requireContext(), R.color.color3),
            ContextCompat.getColor(requireContext(), R.color.color4),
            ContextCompat.getColor(requireContext(), R.color.color5)
        )
        val heights = intArrayOf(60, 76, 58, 80, 55)
        recognitionProgressView!!.setColors(colors)
        recognitionProgressView!!.setBarMaxHeightsInDp(heights)
        recognitionProgressView!!.setCircleRadiusInDp(6) // kich thuoc cham tron
        recognitionProgressView!!.setSpacingInDp(2) // khoang cach giua cac cham tron
        recognitionProgressView!!.setIdleStateAmplitudeInDp(8) // bien do dao dong cua cham tron
        recognitionProgressView!!.setRotationRadiusInDp(40) // kich thuoc vong quay cua cham tron
        recognitionProgressView!!.play()
    }

    private fun finishRecognition() {
        btnListen!!.visibility = View.VISIBLE
        recognitionProgressView!!.stop()
        recognitionProgressView!!.play()
        recognitionProgressView!!.visibility = View.GONE
    }

    private fun processing_text(text: String) {
        var text = text
        text = text.toLowerCase()
        if (text.contains("gọi")) {
            // Call phone
            call(text)
        } else if (text.contains("tìm")) {
            // Search text in Google map or Google search
            search(text)
        } else if (text.contains("mở")) {
            // Lauching application
            app(text)
        } else {
            search_google(text)
        }
    }

    private fun call(text: String) {
        if (text.contains("cho")) {
            val string_start = "cho"
            val start = text.indexOf(string_start) + string_start.length + 1
            val end = text.length
            val name = text.substring(start, end)
            Log.d("name: ", name)
            val nameCall = ArrayList<String>()
            val phoneCall = ArrayList<String>()
            for (contact in contacts!!) {
                Log.d("contact_name: ", contact.name)
                if (contact.name.contains(name) || name.contains(contact.name)) {
                    nameCall.add(contact.nameNumber)
                    phoneCall.add(contact.phone)
                }
            }
            if (nameCall.size > 1) {
                val adapter: ArrayAdapter<*> =
                    ArrayAdapter<String>(requireContext(), R.layout.contact, nameCall)

                val dialog = Dialog(requireContext())
                dialog.setContentView(R.layout.list_contact)
                val listView = dialog.findViewById<ListView>(R.id.contact_list)
                listView.adapter = adapter
                listView.onItemClickListener =
                    AdapterView.OnItemClickListener { parent, view, position, id ->
                        dialog.dismiss()
                        phone_call_number(phoneCall[position])
                    }
                dialog.setCancelable(false)
                dialog.show()
            } else if (nameCall.size == 1) {
                phone_call_number(phoneCall[0])
            } else {
                val textSpeech = "Không tìm thấy tên người liên hệ trong danh bạ"
                Toast.makeText(requireContext(), textSpeech, Toast.LENGTH_SHORT).show()
            }
        } else if (text.contains("taxi")) {
            phone_call_number("024 3232 3232")
        } else {
            val t = text.split(" ").toTypedArray()
            var number = ""
            for (i in t) {
                val pattern = Pattern.compile("\\d*")
                val matcher = pattern.matcher(i)
                Log.d("Text: ", i)
                if (matcher.matches()) {
                    number += i
                }
            }
            phone_call_number(number)
        }
    }

    private fun search(text: String) {
        if (text.contains("đường")) {
            val string_start = "đến"
            val start = text.indexOf(string_start) + string_start.length
            val end = text.length
            val location = text.substring(start, end)
            navigation(location)
        } else if (text.contains("gần")) {
            val string_start = "tìm"
            val string_end = "gần"
            val start = text.indexOf(string_start) + string_start.length
            val end = text.indexOf(string_end)
            val location = text.substring(start, end)
            search_location(location)
        } else {
            val string_start = "tìm"
            val start = text.indexOf(string_start) + string_start.length
            val end = text.length
            val key = text.substring(start, end)
            search_google(key)
        }
    }

    private fun app(text: String) {
        if (text.contains("chụp") || text.contains("camera")) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivity(intent)
        } else if (text.contains("ảnh") || (text.contains("thư viện"))) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_VIEW
            startActivity(intent)
        } else {
            val pachageName: String = existApplication(text)
            lauch_application(pachageName)
        }
    }

    private fun existApplication(name: String): String {
        for (i in apps!!.indices) {
            if (name.contains(apps!![i].name)) {
                return apps!![i].packageName
            }
        }
        return ""
    }

    //Feature
    private fun phone_call_number(number: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$number")
        startActivity(callIntent)
    }

    private fun navigation(location: String) {
        var location = location
        location = location.replace(" ", "+")
        val gmmIntentUri = Uri.parse("google.navigation:q=$location")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun search_location(location: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=$location")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun search_google(key: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, key)
        startActivity(intent)
    }

    private fun lauch_application(idApp: String) {
        val lauch: Intent? = requireContext().packageManager.getLaunchIntentForPackage(idApp)
        if (lauch != null) {
            startActivity(lauch)
        } else {
//            startActivity(
//                Intent(Intent.ACTION_VIEW).setData(
//                    Uri.parse(
//                        "https://play.google.com/store/apps/details?id=$idApp"
//                    )
//                )
//            )
            Toast.makeText(
                requireContext(),
                "Ứng dụng không tồn tại, vui lòng thử lại.",
                Toast.LENGTH_LONG
            ).show()
        }
    }


}

