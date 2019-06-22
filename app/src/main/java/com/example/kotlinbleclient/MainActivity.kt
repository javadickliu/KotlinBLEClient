package com.example.kotlinbleclient

import android.Manifest
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
        //==位置权限校验==
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {//如果该权限没有获得权限
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
        initView()
        //   serviceUuids.set(0, UUID.fromString("0783B03E-8535-B5A0-7140-A304D2495CB7"))
        serviceUuids[0] = UUID.fromString("0783B03E-8535-B5A0-7140-A304D2495CB7")
        myLeScanCallback = MyLeScanCallback()

//        val tokenByte: ByteArray= byteArrayOf(0x00,0x00,0x00,0x7b)

//        val orderByte: ByteArray = byteArrayOf(0x01)
//        val string = "0a"
//        val byteArray = string.toByteArray()

        //       val byteArray:ByteArray = Util.intToByteArray(150)
//        val byteArray:ByteArray = byteArrayOf(0x2.toByte())//TODO C部分是0-255,我们发负的过去她自己会转换
//        for (index in byteArray) {
//            Log.d("test", "byte=" + index)
//        }


//           //val intByte: ByteArray= byteArrayOf()
//        val tokenByte= ByteUtil.putInt(133555435)//todo int自动转四字节byte数组
//       // val tokenByte: ByteArray= byteArrayOf(0x00,0x00,0x00,0x10)//todo int转4字节16进制
//        val orderByte: ByteArray= byteArrayOf(0x01)
//     //   val intByte: IntArray= intArrayOf(0x00,0x00,0x00,0x10)
//
//       val byteArray=  BLEDeviceCommand.parseBLEByte(BLEDeviceCommand.COMMAND_OPEN_BIKE, tokenByte, orderByte)
//        for (index in byteArray.indices) {
//            Log.d("test", "byte=" +byteArray[index]+"  indices="+index )//????
//        }


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
     * 初始化View
     */
    fun initView(): Unit {
        mainactivity_startscan_btn.setOnClickListener(clickListener)
        mainactivity_openbike.setOnClickListener(clickListener)
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
                    toast("设备不支持蓝牙1")
                } else {
                    if (!bluetoothAdapter.isEnabled()) {//蓝牙未打开
                        toast("蓝牙未打开,请打开蓝牙")
                        Log.d(TAG, "蓝牙未打开,请打开蓝牙")
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, 1)

//                        val boolean= bluetoothAdapter.enable()
//                        Log.d(TAG, "蓝牙未打开 隐式打开蓝牙=boolean "+boolean)
                    } else {//已经打开蓝牙
                        ifAlreadyFind = true
                        val ifStartLeScan = bluetoothAdapter.startLeScan(myLeScanCallback)
                        if (ifStartLeScan) {
                            toast("BLE已经打开成功,开始扫描")
                        } else {
                            toast("开始扫描失败")
                        }



                     //   bluetoothAdapter.
//                        bluetoothAdapter.cancelDiscovery()
//                        bluetoothAdapter.stopLeScan()
                      //  bluetoothAdapter.setName()//修改本地蓝牙可见的时候的名字

//                        if (BluetoothAdapter.checkBluetoothAddress("76:48:88:41:90:03")) {
//                           val bluetoothDevice= bluetoothAdapter.getRemoteDevice("76:48:88:41:90:03")//通过
//                            Log.d(TAG, "MAC地址符合标准")
//                            if(bluetoothDevice.name!=null){
//
//                                Log.d(TAG, "找到指定MAC地址BLE设备 设备名称="+bluetoothDevice.name)
//                            }else{
//                                Log.d(TAG, "未找到指定MAC地址BLE设备")
//                            }
//                        }else{
//                            Log.d(TAG, "MAC地址不符合标准")
//                        }


                    }
                }
            }
//            R.id.mainactivity_openbike -> {//开锁
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

    /**
     * 搜索BLE设备结果
     */
    inner class MyLeScanCallback : BluetoothAdapter.LeScanCallback {
        override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {




            val deviceName = device!!.name
            if (deviceName != null) {
                Log.d(TAG, "找到指定蓝牙设备 deviceName= " + deviceName)
            }
            if (deviceName != null && deviceName.equals("魅蓝") && ifAlreadyFind) {//找到指定名称的蓝牙设备xbcx-test
                bluetoothAdapter.stopLeScan(myLeScanCallback)
                ifAlreadyFind = false
                Log.d(TAG, "找到指定蓝牙设备 type= "+   device.type)
                //=====================device
//                device.bondState//绑定状态               //可以利用Bluetooth获取到的BLE设备的信息
//                device.address//mac地址
//                device.name//蓝牙名字
             //type
//                device.uuids//设备的UUID
//                device.connectGatt()
//                device.createBond()
//                device.createInsecureRfcommSocketToServiceRecord()
//                device.fetchUuidsWithSdp()
//                device.createRfcommSocketToServiceRecord()
//                device.setPairingConfirmation()
//                device.setPin()

                //====================
                val intent = Intent(this@MainActivity, DataControlActivity::class.java)
                intent.putExtra("key_bluetoothdevice", device)
                startActivity(intent)
            }


        }

    }
//     val callback: BluetoothAdapter.LeScanCallback =
//        BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
//
//            val deviceName = device.name
//            if(deviceName!=null&&deviceName.equals("xbcx-test")&&ifAlreadyFind){//找到指定名称的蓝牙设备
//            //   bluetoothAdapter.stopLeScan(callback)
//                ifAlreadyFind=false
//                Log.d(TAG, "找到指定蓝牙设备 ")
//                val intent = Intent(this@MainActivity, DataControlActivity::class.java)
//                intent.putExtra("key_bluetoothdevice", device)
//                startActivity(intent)
//            }
//
//
//
//
//
//
////            val deviceName = device.name
////            val deviceHardwareAddress = device.address
////
////            Log.d("MainActivity", "onReceive: 新设备t devicename1=" + deviceName + " deviceHardwareAddress=" + deviceHardwareAddress)
////            if (deviceName != null) {//判断是否该设备已经搜索到
////                var ifFindDevie = false
////                val parcelUuid = device.uuids
////                Log.d("MainActivity", "onReceive:  parcelUuid=" + parcelUuid.size)
////                if (deviceList.size == 0) {
////                    //     Log.d(TAG, "onReceive: 初次添加蓝牙设备到集合1")
////                    deviceList.add(device)
////                    rcAdapter.setmDatas(deviceList)
////                    rcAdapter.notifyDataSetChanged()//刷新list
////                }
////                for (i in deviceList.indices) {//去重
////                    if (deviceList.get(i).address == deviceHardwareAddress) {
////                        ifFindDevie = true
////                    }
////                    if (i == deviceList.size - 1 && !ifFindDevie) {//没有发现过的设备添加到list
////                        deviceList.add(device)
////                        //   Log.d(TAG, "onReceive: 新设备添加到list devicename=" + deviceName + " size=" + deviceList.size)
////                        rcAdapter.setmDatas(deviceList)
////                        rcAdapter.notifyDataSetChanged()
////                    }
////                }
////            }
//        }


    /**
     * 点击监听回调
     */
    private val itemClickListenr = object : RCAdapter.ItemClickListenr {
        override fun onClick(view: View, positon: Int) {
            if (bluetoothAdapter != null) {
                //       Log.d(TAG, "onClick: 开始连接BLE设备停止扫描")
                bluetoothAdapter.stopLeScan(myLeScanCallback)
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


