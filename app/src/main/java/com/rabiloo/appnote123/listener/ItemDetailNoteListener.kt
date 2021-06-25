package com.rabiloo.appnote123.listener

interface ItemDetailNoteListener {
    fun openDialogDetail(id: String, time: String, date: String, note: String, downLoadUri: String)
    fun deleItem(id: String, posi: Int)
}