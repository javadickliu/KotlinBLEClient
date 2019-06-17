package com.example.kotlinbleclient

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinbleclient.adpter.RCAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    val UUID_SERVICE: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    val UUID_CHARACTERISTIC_READ: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FC")
    val UUID_CHARACTERISTIC_WRITE: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FD")
    val UUID_DESCRIPTOR: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FE")
    val UUID_DESCRIPTOR_NOTIFY: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FF")


    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var deviceList: ArrayList<BluetoothDevice>
    lateinit var rcAdapter: RCAdapter
    var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //==位置权限校验==
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {//如果该权限没有获得权限
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
        initView()
    }

    /**
     * 初始化View
     */
    fun initView(): Unit {
        mainactivity_startscan_btn.setOnClickListener(clickListener)
        mainactivity_bledevice_rc.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        mainactivity_bledevice_rc.layoutManager = LinearLayoutManager(this)
        deviceList = ArrayList<BluetoothDevice>()
        rcAdapter = RCAdapter(deviceList)
        rcAdapter.setItemClickListener(itemClickListenr)
        mainactivity_bledevice_rc.adapter = rcAdapter


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {//请求开启蓝牙的回调结果
            if (resultCode == Activity.RESULT_OK) {//用户允许打开蓝牙
                toast("允许打开蓝牙")
            } else if (resultCode == Activity.RESULT_CANCELED) {//打开蓝牙失败或者用户拒绝打开蓝牙
                toast("拒绝打开蓝牙")
            }
        }
    }

    /**
     *view点击事件监听
     */
    private val clickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.mainactivity_startscan_btn -> {
                val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                bluetoothAdapter = bluetoothManager.adapter
                if (bluetoothAdapter == null) {
                    toast("设备不支持蓝牙")
                } else {
                    if (!bluetoothAdapter.isEnabled()) {//蓝牙未打开
                        toast("蓝牙未打开,请打开蓝牙")
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, 1)
                    } else {//已经打开蓝牙
                        val ifStartLeScan = bluetoothAdapter.startLeScan(callback)
                        if (ifStartLeScan) {
                            toast("BLE已经打开成功,开始扫描")
                            //    Log.d(TAG, "onCreate: 开始扫描")
                        } else {
                            //             Log.d(TAG, "onCreate: 禁止扫描")
                        }
                    }
                }
            }
        }
    }


    /**
     * 搜索BLE设备结果
     */
    internal val callback: BluetoothAdapter.LeScanCallback =
        BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address
            if (deviceName != null) {//判断是否该设备已经搜索到
                var ifFindDevie = false
                if (deviceList.size == 0) {
                    //     Log.d(TAG, "onReceive: 初次添加蓝牙设备到集合1")
                    deviceList.add(device)
                    rcAdapter.setmDatas(deviceList)
                    rcAdapter.notifyDataSetChanged()//刷新list
                }
                for (i in deviceList.indices) {//去重
                    if (deviceList.get(i).address == deviceHardwareAddress) {
                        ifFindDevie = true
                    }
                    if (i == deviceList.size - 1 && !ifFindDevie) {//没有发现过的设备添加到list
                        deviceList.add(device)
                        //   Log.d(TAG, "onReceive: 新设备添加到list devicename=" + deviceName + " size=" + deviceList.size)
                        rcAdapter.setmDatas(deviceList)
                        rcAdapter.notifyDataSetChanged()
                    }
                }
            }
        }


    /**
     * 点击监听回调
     */
    private val itemClickListenr = object : RCAdapter.ItemClickListenr {
        override fun onClick(view: View, positon: Int) {
            if (bluetoothAdapter != null) {
                //       Log.d(TAG, "onClick: 开始连接BLE设备停止扫描")
                bluetoothAdapter.stopLeScan(callback)
            }
            val intent = Intent(this@MainActivity, DataControlActivity::class.java)
            intent.putExtra("key_bluetoothdevice", rcAdapter.getmDatas()!![positon])
            startActivity(intent)
            //            RCAdapter rcAdapter = (RCAdapter) recyclerView.getAdapter();
            //   BluetoothGatt bluetoothGatt = rcAdapter.getmDatas().get(positon).connectGatt(MainActivity.this, false, new MyBluetoothGattCallback());
            //设置自动重连反而没有MyBluetoothGattCallback回调
        }
    }

}


