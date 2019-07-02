package com.example.kotlinbleclient.view

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import com.example.kotlinbleclient.R
import kotlinx.android.synthetic.main.custom_content.*

class CustomDialog(context: Context?) : AlertDialog(context) {
    private var confirmClickListener:ConfirmClickListener?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_content)
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        customdialog_content_confirm_btn.setOnClickListener {
             if(confirmClickListener!=null){
                 confirmClickListener?.click(customdialog_content_ed.text.toString())
             }
        }
    }
    fun setConfirmClickListener(listener: ConfirmClickListener){
        this.confirmClickListener=listener
    }
    public interface ConfirmClickListener {
        fun click(content:String)
    }
}