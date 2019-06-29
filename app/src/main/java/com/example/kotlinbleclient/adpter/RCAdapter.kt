package com.example.kotlinbleclient.adpter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinbleclient.R

/**
 * rcview适配器
 */
class RCAdapter(private var mDatas: List<BluetoothDevice>?,val context:Context) : RecyclerView.Adapter<RCAdapter.VH>() {
    private var listenr: ItemClickListenr? = null

    fun getmDatas(): List<BluetoothDevice>? {
        return mDatas
    }

    fun setmDatas(mDatas: List<BluetoothDevice>) {
        this.mDatas = mDatas
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rc_item, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val itemContent = "设备名称:" + mDatas!![position].name + "\n" + "mac地址:" + mDatas!![position].address
        holder.title.text = itemContent
        holder.bg.tag = position
        Log.d(TAG, "onBindViewHolder: mDatas.size=" + mDatas!!.size)
    }

    override fun getItemCount(): Int {
        return mDatas!!.size
    }

    fun setItemClickListener(listener: ItemClickListenr) {//定义方法传递监听对象
        this.listenr = listener
    }

    interface ItemClickListenr {
        //监听接口
        fun onClick(view: View, positon: Int)
    }


    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView
        val bg: LinearLayout
        init {
            title = v.findViewById(R.id.mainactivity_recyclerview_item_bluetooth_name) as TextView
            bg = v.findViewById(R.id.rc_item_bg) as LinearLayout
            bg.setOnClickListener(MyViewClickListener())
        }
    }

    private inner class MyViewClickListener : View.OnClickListener {

        override fun onClick(v: View) {
            if (listenr != null) {
                listenr!!.onClick(v, v.tag as Int)//通知监听者
            }
        }
    }

    companion object {
        private val TAG = "RCAdapter"
    }

}