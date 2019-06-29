package com.example.kotlinbleclient

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleBaseCallback
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.example.kotlinbleclient.adpter.RCAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.util.*
import kotlin.collections.ArrayList
import com.clj.fastble.scan.BleScanRuleConfig


class MainActivity : AppCompatActivity() {

    private val UUID_SERVICE =
        java.util.UUID.fromString("0783B03E-8535-B5A0-7140-A304D2495CB7")//蓝牙串口的通用UUID,UUID是什么东西
    private val SERVER_TX_UUID = java.util.UUID.fromString("0783B03E-8535-B5A0-7140-A304D2495CBA")//写CHARACTERISTIC
    private val UUID_SERVICE_STRING = "0783B03E-8535-B5A0-7140-A304D2495CB7"
    private val SERVER_TX_UUID_STRING = "0783B03E-8535-B5A0-7140-A304D2495CBA"
    private val SERVER_RX_UUID = java.util.UUID.fromString("0783B03E-8535-B5A0-7140-A304D2495CB9")//读CHARACTERISTIC
    private val CLIENT_CHARACTERISTIC_CONFIG = java.util.UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")//默认de

    companion object {
        private val TAG = "MainActivity"
    }

    internal var serviceUuids = arrayOfNulls<UUID>(1)
    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var deviceList: ArrayList<BluetoothDevice>
    lateinit var rcAdapter: RCAdapter
    var recyclerView: RecyclerView? = null

    var myLeScanCallback: MyLeScanCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myLeScanCallback = MyLeScanCallback()

        checkPermission()
        initView()
        checkBLE()
        serviceUuids[0] = UUID.fromString("0783B03E-8535-B5A0-7140-A304D2495CB7")


//=====================
        //=====FastBLe开源库使===
//        BleManager.getInstance().init(application)
//        BleManager.getInstance()
//            .enableLog(false)//关闭日志
//            .setReConnectCount(1, 5000)//设置重连次数和间隔
//            .setOperateTimeout(5000);//操作超时时间
//        val scanRuleConfig = BleScanRuleConfig.Builder()
//     //       .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
//            .setDeviceName(true, "xbcx-test")         // 只扫描指定广播名的设备，可选
//       //     .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
//            .setAutoConnect(true)      // 连接时的autoConnect参数，可选，默认false
//            .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
//            .build()
//        BleManager.getInstance().initScanRule(scanRuleConfig)
//        BleManager.getInstance().scan(object : BleScanCallback() {
//            override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
//                toast("蓝牙扫描扫描结束 size="+scanResultList?.size)
//                Log.d(TAG, "蓝牙扫描结束")
//                val  bleDevice=scanResultList!![0]
//
//            }
//
//            override fun onScanStarted(success: Boolean) {
//                toast("开启蓝牙扫描")
//            }
//
//            override fun onScanning(bleDevice: BleDevice?) {//已经去重
//                BleManager.getInstance().connect(bleDevice,object : BleGattCallback(){//连接设备回调
//                override fun onStartConnect() {
//                    Log.d(TAG, "开始连接指定蓝牙设备")
//                }
//
//                    override fun onDisConnected(
//                        isActiveDisConnected: Boolean,
//                        device: BleDevice?,
//                        gatt: BluetoothGatt?,
//                        status: Int
//                    ) {
//                        Log.d(TAG, "断开连接 ")
//                    }
//
//                    override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
//                        Log.d(TAG, "连接指定蓝牙成功 连接指定GATT成功 开始读写")
//                        //    val characteristic = gatt!!.getService(UUID_SERVICE).getCharacteristic(SERVER_TX_UUID)
//                        val tokenByte: ByteArray= ByteUtil.putInt(123)
//                        val orderByte: ByteArray= byteArrayOf(0x00)
//                        val byteArray=BLEDeviceCommand.parseBLEByte(BLEDeviceCommand.COMMAND_OPEN_BIKE,tokenByte,orderByte)
//                        // gatt.writeCharacteristic(characteristic)
//
//                        BleManager.getInstance().write(bleDevice,UUID_SERVICE_STRING,SERVER_TX_UUID_STRING,byteArray,object :
//                            BleWriteCallback(){
//                            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
//                                Log.d(TAG, " 写数据成功")
//
//                            }
//
//                            override fun onWriteFailure(exception: BleException?) {
//                                Log.d(TAG, "写数据失败 原因:"+exception.toString())
//                            }
//                        })
//                    }
//
//                    override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
//                        Log.d(TAG, "连接指定蓝牙设备失败")
//                    }
//
//                })
//            }
//
//            override fun onLeScan(bleDevice: BleDevice?) {
//                super.onLeScan(bleDevice)
//            }
//
//        })
    }
//================FastBLe开源库用===================


//    /**
//     *view点击事件监听
//     */
//    private class MyBleBaseCallBack(): BleBaseCallback(){
//        override onSca
//    }


    //=========================================

    /**
     * 校验权限
     */
    fun checkPermission() {
        //==位置权限校验==
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {//如果该权限没有获得权限
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
    }

    /**
     * 校验蓝牙是否打开
     */
    fun checkBLE() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled()) {//蓝牙未打开
            //    toast("蓝牙未打开,请打开蓝牙")
            Log.d(TAG, "蓝牙未打开,请打开蓝牙")
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
//           val boolean= bluetoothAdapter.enable()
//           Log.d(TAG, "蓝牙未打开 隐式打开蓝牙=boolean "+boolean)
        } else {//已经打开蓝牙
            mainactivity_swiperefresh.isRefreshing=true
            uiHandler.sendEmptyMessageDelayed(0,15000)
            val ifStartLeScan = bluetoothAdapter.startLeScan(myLeScanCallback)
            if (ifStartLeScan) {
                toast("BLE已经打开成功,开始扫描")
            } else {
                toast("设备蓝牙状态异常,开始扫描失败")
            }
        }
    }

    /**
     * 初始化View
     */
    fun initView(): Unit {
        mainactivity_bledevice_rc.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        mainactivity_bledevice_rc.layoutManager = LinearLayoutManager(this)
        deviceList = ArrayList<BluetoothDevice>()
        rcAdapter = RCAdapter(deviceList,this)
        rcAdapter.setItemClickListener(itemClickListenr)
        mainactivity_bledevice_rc.adapter = rcAdapter
      //  mainactivity_swiperefresh.isRefreshing=true
        mainactivity_swiperefresh.setOnRefreshListener {//下拉刷新
      //      uiHandler.sendEmptyMessageDelayed(0,7000)
            deviceList.clear()
            rcAdapter.setmDatas(deviceList)
            rcAdapter.notifyDataSetChanged()
            checkBLE()
        }
    }

    /**
     * 刷新停止搜索BLE设备UI
     */
    @SuppressLint("HandlerLeak")
    private val uiHandler=object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            mainactivity_swiperefresh.isRefreshing=false
            bluetoothAdapter.stopLeScan(myLeScanCallback)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {//请求开启蓝牙的回调结果
            if (resultCode == Activity.RESULT_OK) {//用户允许打开蓝牙
                val ifStartLeScan = bluetoothAdapter.startLeScan(myLeScanCallback)
                if (ifStartLeScan) {
                    toast("BLE已经打开成功,开始扫描")
                    mainactivity_swiperefresh.isRefreshing=true
                    uiHandler.sendEmptyMessageDelayed(0,15000)
                } else {
                    toast("设备蓝牙状态异常,开始扫描失败1111")
                }

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
 //           R.id.mainactivity_openbike -> {//开锁
//                val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//                bluetoothAdapter = bluetoothManager.adapter
//                if (bluetoothAdapter == null) {
//                    toast("设备不支持蓝牙")
//                } else {
//                    if (!bluetoothAdapter.isEnabled()) {//蓝牙未打开
//                        toast("蓝牙未打开,请打开蓝牙")
//                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                        startActivityForResult(enableBtIntent, 1)
//                    } else {//已经打开蓝牙
//                        ifAlreadyFind = true
//                        val ifStartLeScan = bluetoothAdapter.startLeScan(myLeScanCallback)
//                        if (ifStartLeScan) {
//                            toast("BLE已经打开成功,开始扫描")
//                        } else {
//                            toast("BLE扫描启动失败")
//                        }
//                    }
//                }
//            }
        }

    }

    var ifAlreadyFind = false

///**
//     * 搜索BLE设备结果
//     */
//    inner class MyLeScanCallback : BluetoothAdapter.LeScanCallback {
//        override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
//
//
//            val deviceName = device!!.name
//            if (deviceName != null) {
//                Log.d(TAG, "找到指定蓝牙设备 deviceName= " + deviceName)
//            }
//            if (deviceName != null && deviceName.equals("魅蓝") && ifAlreadyFind) {//找到指定名称的蓝牙设备xbcx-test
//                bluetoothAdapter.stopLeScan(myLeScanCallback)
//                ifAlreadyFind = false
//                Log.d(TAG, "找到指定蓝牙设备 type= " + device.type)
//                //=====================device
////                device.bondState//绑定状态               //可以利用Bluetooth获取到的BLE设备的信息
////                device.address//mac地址
////                device.name//蓝牙名字
//                //type
////                device.uuids//设备的UUID
////                device.connectGatt()
////                device.createBond()
////                device.createInsecureRfcommSocketToServiceRecord()
////                device.fetchUuidsWithSdp()
////                device.createRfcommSocketToServiceRecord()
////                device.setPairingConfirmation()
////                device.setPin()
//
//                //====================
//                val intent = Intent(this@MainActivity, DataControlActivity::class.java)
//                intent.putExtra("key_bluetoothdevice", device)
//                startActivity(intent)
//            }
//
//
//        }
//
//    }
    /**
     * 搜索BLE设备结果
     */
    inner class MyLeScanCallback : BluetoothAdapter.LeScanCallback {
        override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
            val deviceName = device?.name
            val deviceHardwareAddress = device?.address

            Log.d(
                "MainActivity",
                "onReceive: 新设备t devicename1=" + deviceName + " deviceHardwareAddress=" + deviceHardwareAddress
            )
            if (deviceName != null) {//判断是否该设备已经搜索到
                var ifFindDevie = false
                if (deviceList.size == 0) {
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
                        rcAdapter.setmDatas(deviceList)
                        rcAdapter.notifyDataSetChanged()
                    }
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
                Log.d(TAG, "onClick: 开始连接BLE设备 停止扫描BLE")
                bluetoothAdapter.stopLeScan(myLeScanCallback)
            }
            val intent = Intent(this@MainActivity, DataControlActivity::class.java)
            intent.putExtra("key_bluetoothdevice", rcAdapter.getmDatas()!![positon])
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() 停止搜索")
        bluetoothAdapter.stopLeScan(myLeScanCallback)
        bluetoothAdapter
    }

}


