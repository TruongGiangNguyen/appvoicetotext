package com.rabiloo.appnote123.listener

interface ItemNoteHomeListener {
    fun onClickItem(id: String, date: String, countNote: Int)

    fun deleteItem(id: String, position: Int)
}