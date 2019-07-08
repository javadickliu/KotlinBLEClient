package com.example.kotlinbleclient

import android.bluetooth.*
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.ExpandableListView
import android.widget.TextView
import com.example.kotlinbleclient.adpter.MyExpandListViewAdapter
import java.util.ArrayList
import android.R.attr.data
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattDescriptor
import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.example.kotlinbleclient.adpter.RCAdapter
import com.example.kotlinbleclient.utils.ByteUtil
import com.example.kotlinbleclient.utils.SPUtils
import com.example.kotlinbleclient.view.CustomDialog
import kotlinx.android.synthetic.main.activity_data_control.*
import org.jetbrains.anko.toast


class DataControlActivity : AppCompatActivity() {
    private var deviceInfoTv: TextView? = null
    private var deviceStatusTv: TextView? = null
    private var myExpandListViewAdapter: MyExpandListViewAdapter? = null
    private var expandableListView: ExpandableListView? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var ifServiceConnect = false

    private lateinit var temp1: List<String>
    private lateinit var temp2: List<List<BluetoothGattCharacteristic>>
    private val updataUIHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            //      Log.d(TAG, "handleMessage() 刷新连接状态 =" + (Looper.getMainLooper() == Looper.myLooper()))
            if (msg.what == 0) {//刷新service
                Log.d(TAG, "handleMessage() 刷新设备列表")
                myExpandListViewAdapter!!.updataMyData(temp1, temp2)
            } else if (msg.what == 1) {
                Log.d(TAG, "handleMessage() 刷新至已连接状态")
                deviceStatusTv!!.text = "Status:" + "已连接"
            } else if (msg.what == 2) {
                Log.d(TAG, "handleMessage() 刷新至断开连接状态")
                deviceStatusTv!!.text = "Status:" + "断开连接"
            } else if (msg.what == 3) {
                Log.d(TAG, "handleMessage() 刷新至已连接状态")
                datacontrolactivity_servicestatus_tv.text = "Service Status:" + "已连接"
                datacontrolactivity_connect_progress.visibility = View.INVISIBLE
                datacontrolactivity_reconnectbtn.visibility = View.VISIBLE
            } else if (msg.what == 4) {
                Log.d(TAG, "handleMessage() 刷新至断开连接状态")
                datacontrolactivity_connect_progress.visibility = View.INVISIBLE
                datacontrolactivity_reconnectbtn.visibility = View.VISIBLE
                datacontrolactivity_servicestatus_tv.text = "Service Status:" + "断开连接"
            }else if (msg.what ==5) {
                val responseType=msg.data.getByte("key_result",-1)
                if(responseType.compareTo(0)==0){//成功
                    toast("命令发送成功")
                }else if(responseType.compareTo(1)==0){//Token校验错误
                    toast("Token校验错误")
                }else if(responseType.compareTo(2)==0){//请求内容错误
                    toast("请求内容错误")
                }else if(responseType.compareTo(4)==0){//操作失败
                    toast("操作失败")
                }else if(responseType.compareTo(5)==0){//命令不支持
                    toast("命令不支持")
                }else if(responseType.compareTo(6)==0){//车辆正在骑行中
                    toast("车辆正在骑行中")
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_control)
        setSupportActionBar(datacontrolactivity_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        datacontrolactivity_toolbar_back.setOnClickListener {
            finish()
        }
        val bluetoothDevice = intent.getParcelableExtra<Parcelable>("key_bluetoothdevice") as BluetoothDevice
        deviceInfoTv = findViewById(R.id.datacontrolactivity_deviceinfo_tv) as TextView
        deviceStatusTv = findViewById(R.id.datacontrolactivity_devicestatus_tv) as TextView
        val deviceInfo = "Address:" + bluetoothDevice.address + "\n" +
                "Name:" + bluetoothDevice.name
        deviceInfoTv!!.text = deviceInfo
        deviceStatusTv!!.text = "Status:" + "连接中"
        datacontrolactivity_servicestatus_tv.setText("Service Status:" + "连接中")
        datacontrolactivity_servicestoken_tv.setText(
            "Token(十进制):" + SPUtils.get(
                this@DataControlActivity,
                "token",
                "168428805"
            )
        )
        datacontrolactivity_opennbike.setOnClickListener(btnClickListenr)
        datacontrolactivity_stopbike.setOnClickListener(btnClickListenr)
        datacontrolactivity_openbatlock.setOnClickListener(btnClickListenr)
        datacontrolactivity_closebatlock.setOnClickListener(btnClickListenr)
        datacontrolactivity_openwheellock.setOnClickListener(btnClickListenr)
        datacontrolactivity_closewheellock.setOnClickListener(btnClickListenr)
        datacontrolactivity_openelecric.setOnClickListener(btnClickListenr)
        datacontrolactivity_closeelectric.setOnClickListener(btnClickListenr)
        expandableListView = findViewById(R.id.datacontrolactivity_bleservice_expandlv) as ExpandableListView
        myExpandListViewAdapter = MyExpandListViewAdapter()
        expandableListView!!.setAdapter(myExpandListViewAdapter)
        datacontrolactivity_changetoken_tv.isClickable = true
        datacontrolactivity_changetoken_tv.setOnClickListener {
            val customDialog = CustomDialog(this)
            customDialog.setConfirmClickListener(object : CustomDialog.ConfirmClickListener {
                override fun click(content: String) {
                    datacontrolactivity_servicestoken_tv.setText("Token(十进制):" + content)
                    SPUtils.put(this@DataControlActivity, "token", content)
                    customDialog.hide()
                }

            })
            customDialog.show()
        }
        //=======开启BLE 设备GATT======
        Log.d(
            TAG,
            "onCreate() address=" + bluetoothDevice.address + " name=" + bluetoothDevice.name + " UUID=" + bluetoothDevice.uuids
        )
        if (bluetoothDevice.bondState == BluetoothDevice.BOND_NONE) {//没有绑定过
            Log.d(TAG, "onCreate() 设备未连接过 开始建立GATT通路1")
            bluetoothGatt = bluetoothDevice.connectGatt(
                this@DataControlActivity,
                true,
                MyBluetoothGattCallback(),
                BluetoothDevice.TRANSPORT_LE
            )
            //todo 设置不自动重连速度是快但是经常连接上就马上断开
            //todo 设置自动重连不容易断 连接成功率高  而且不容易断
            //
        } else if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDING) {//绑定中
            Log.d(TAG, "onCreate() 设备连接中,不需要要再次连接")
        } else if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
            Log.d(TAG, "onCreate() 设备已经连接过,不需要要再次连接")
        }

        datacontrolactivity_reconnectbtn.setOnClickListener {
            if (ifServiceConnect) {
                toast("已连接成功,不需要重连")
            } else {
                toast("重连中.....")
                datacontrolactivity_connect_progress.visibility = View.VISIBLE
                datacontrolactivity_reconnectbtn.visibility = View.INVISIBLE
                bluetoothGatt?.close()
                bluetoothGatt?.disconnect()
                bluetoothGatt = bluetoothDevice.connectGatt(this@DataControlActivity, true, MyBluetoothGattCallback())
            }
        }


    }

    /**
     * 点击监听回调
     */
    private val btnClickListenr = object : View.OnClickListener {
        override fun onClick(v: View?) {
            if (!ifServiceConnect) {
                toast("蓝牙没有连接成功,请尝试连接")
                return
            }
            var commandByte: Byte? = null
            var tokenByte: ByteArray? = null
            var orderByte: ByteArray? = null
            val tokenString = SPUtils.get(this@DataControlActivity, "token", "168428805") as String
            tokenByte = ByteUtil.putInt(tokenString.toInt())
            //  toast("tokenString.toInt()="+tokenString.toInt())
            when (v?.id) {
                R.id.datacontrolactivity_opennbike -> {
                    commandByte = BLEDeviceCommand.COMMAND_OPEN_BIKE
                    //tokenByte = byteArrayOf(0x0a, 0x0a, 0x05, 0x05)
                    orderByte = byteArrayOf(0x00)
                }
                R.id.datacontrolactivity_stopbike -> {
                    commandByte = BLEDeviceCommand.COMMAND_STOP_BIKE
                    orderByte = byteArrayOf(0x01)
                }
                R.id.datacontrolactivity_openbatlock -> {
                    commandByte = BLEDeviceCommand.COMMAND_OPEN_BATLOCK
                    orderByte = byteArrayOf(0x00)
                }
                R.id.datacontrolactivity_closebatlock -> {
                    commandByte = BLEDeviceCommand.COMMAND_CLOSE_BATLOCK
                    orderByte = byteArrayOf(0x01)
                }
                R.id.datacontrolactivity_openwheellock -> {
                    commandByte = BLEDeviceCommand.COMMAND_OPEN_WHELLLOCK
                    orderByte = byteArrayOf(0x00)
                }
                R.id.datacontrolactivity_closewheellock -> {
                    commandByte = BLEDeviceCommand.COMMAND_CLOSE_WHELLLOCK
                    orderByte = byteArrayOf(0x01)
                }
                R.id.datacontrolactivity_openelecric -> {
                    commandByte = BLEDeviceCommand.COMMAND_OPEN_ELECTIC
                    orderByte = byteArrayOf(0x01)
                }
                R.id.datacontrolactivity_closeelectric -> {
                    commandByte = BLEDeviceCommand.COMMAND_CLOSE_ELECTIC
                    orderByte = byteArrayOf(0x00)
                }
            }
            val bluetoothGattService = bluetoothGatt?.getService(UUID_SERVICE)
            if (bluetoothGattService == null) {
                Log.d(TAG, "btnClickListenr() 连接指定UUID的Service失败")
            } else {
                Log.d(TAG, "btnClickListenr() 连接指定UUID的Service成功")
                val characteristic = bluetoothGattService.getCharacteristic(SERVER_TX_UUID)
                if (characteristic == null) {
                    Log.d(TAG, "btnClickListenr() 连接指定UUID的characteristics失败")
                } else {
                    Log.d(TAG, "btnClickListenr() 连接指定UUID的characteristics成功1 =" + characteristic.permissions)
                    characteristic.setValue(
                        BLEDeviceCommand.parseBLEByte(
                            commandByte!!,
                            tokenByte!!,
                            orderByte!!
                        )
                    )

                    bluetoothGatt?.writeCharacteristic(characteristic)
                    val notifyCharacteristic = bluetoothGattService.getCharacteristic(SERVER_NOTIFY_UUID)
                    bluetoothGatt?.setCharacteristicNotification(notifyCharacteristic, true)
                }

            }

        }

    }

    private fun parseBondState(state: Int): String {
        var result = "错误"
        if (state == BluetoothDevice.BOND_NONE) {
            result = "绑定none"
        } else if (state == BluetoothDevice.BOND_BONDING) {
            result = "绑定中"
        } else if (state == BluetoothDevice.BOND_BONDED) {
            result = "已经绑定"
        }
        return result
    }

    /**
     * 连接BLE设备结果
     */
    private inner class MyBluetoothGattCallback : BluetoothGattCallback() {
        override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status)
            Log.d(TAG, "onPhyUpdate: ")
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {//连接BLE设备GATT结果回调
            super.onConnectionStateChange(gatt, status, newState)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onConnectionStateChange()  连接/断开操作成功")
            } else {
                Log.d(TAG, "onConnectionStateChange() 连接/断开操作失败")
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "onConnectionStateChange()  当前状态:断开")
            } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "onConnectionStateChange()   当前状态:连接")
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "onConnectionStateChange()  连接GATT成功")
                updataUIHandler.sendEmptyMessageDelayed(1, 0)
                gatt.discoverServices()


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                ifServiceConnect = false
                updataUIHandler.sendEmptyMessageDelayed(2, 0)
                updataUIHandler.sendEmptyMessageDelayed(4, 0)
                Log.d(TAG, "onConnectionStateChange  从GATT连接断开 status=" + status)
                if (bluetoothGatt != null) {
                    bluetoothGatt?.close()
                    Thread.sleep(200)
                    Log.d(TAG, "onConnectionStateChange 重连")
                    //   bluetoothGatt?.connect()//todo 如果没有发送开锁命令成功且断开连接需要重连
                }
            }
        }

        /**
         * 连接蓝牙成功
         */
        override fun onServicesDiscovered(
            gatt: BluetoothGatt,
            status: Int
        ) {//BLE设备的Service连接成功,调用这个回调才表示蓝牙真正建立连接,gatt.discoverServices()
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {//发现Service成功
                ifServiceConnect = true
                //               Log.d(TAG, "onServicesDiscovered() 发现BLE设备的Service 且建立GATT连接成功");
//                val bluetoothGattService = gatt.getService(UUID_SERVICE)
//                if (bluetoothGattService == null) {
//                    Log.d(TAG, "onServicesDiscovered() 连接指定UUID的Service失败")
//                } else {
//                    Log.d(TAG, "onServicesDiscovered() 连接指定UUID的Service成功")
//                    val characteristic = bluetoothGattService.getCharacteristic(UUID_CHARACTERISTIC_WRITE)
//                    if (characteristic == null) {
//                        Log.d(TAG, "onServicesDiscovered() 连接指定UUID的characteristics失败")
//                    } else {
//                        Log.d(TAG, "onServicesDiscovered() 连接指定UUID的characteristics成功1 =" + characteristic.permissions)
//                        val orderByte: ByteArray = byteArrayOf(0x09)
//                        characteristic.setValue(orderByte)response
//                //        val descriptor = characteristic.getDescriptor(UUID_DESCRIPTOR_NOTIFY)
//                    //    descriptor.setValue(orderByte)
//                 //       Log.d(TAG, "onServicesDiscovered() 连接指定UUID的descriptor结果=" + descriptor)
//                     //   descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)//这句话好像设不设置都能触发notify啊
//              //          descriptor.setValue(orderByte)
//                        gatt.setCharacteristicNotification(characteristic,true)
//                        gatt.writeCharacteristic(characteristic)
//                 //       gatt.writeDescriptor(descriptor)
//                    }
//
//                }

//                // =========虾米智联写数据===
                updataUIHandler.sendEmptyMessageDelayed(3, 0)
                Log.d(TAG, "onServicesDiscovered() 发现BLE设备的Service 且建立GATT连接成功")
//                val bluetoothGattService = gatt.getService(UUID_SERVICE)
//                if (bluetoothGattService == null) {
//                    Log.d(TAG, "onServicesDiscovered() 连接指定UUID的Service失败")
//                } else {
//                    Log.d(TAG, "onServicesDiscovered() 连接指定UUID的Service成功")
//                    val characteristic = bluetoothGattService.getCharacteristic(SERVER_RX_UUID)
//                    if (characteristic == null) {
//                        Log.d(TAG, "onServicesDiscovered() 连接指定UUID的characteristics失败")
//                    } else {
//                        Log.d(TAG, "onServicesDiscovered() 连接指定UUID的characteristics成功1 =" + characteristic.permissions)
//                        val tokenByte: ByteArray = ByteUtil.putInt(123)
//                        val orderByte: ByteArray = byteArrayOf(0x00)
//                        //    characteristic.setValue(orderByte)
//                        val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
////                        for(index in descriptor){
////                            Log.d(TAG, "onServicesDiscovered() 连接指定UUID的descriptor结果=" + index.uuid)
////                        }
//                        //          descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)//这句话好像设不设置都能触发notify啊
//                        descriptor.setValue(
//                            BLEDeviceCommand.parseBLEByte(
//                                BLEDeviceCommand.COMMAND_OPEN_BIKE,
//                                tokenByte,
//                                orderByte
//                            )
//                        )
//                        gatt.setCharacteristicNotification(characteristic, true)
//                        gatt.writeDescriptor(descriptor)
//                    }
//
//                }


                //    =========虾米智联写数据===测试没问题
//                val characteristic = gatt.getService(UUID_SERVICE).getCharacteristic(SERVER_TX_UUID)
//                val tokenByte: ByteArray = ByteUtil.putInt(123)
//                val orderByte: ByteArray = byteArrayOf(0x01)
//                val isSetValue = characteristic.setValue(
//                    BLEDeviceCommand.parseBLEByte(
//                        BLEDeviceCommand.COMMAND_OPEN_BIKE,
//                        tokenByte,
//                        orderByte
//                    )
//                )
//                //    val isSetValue = characteristic.setValue(tokenByte)
//                //  characteristic.setValue()
//                //       Log.d(TAG, "find service ok: isSetValue1 =" + isSetValue)
//                gatt.writeCharacteristic(characteristic)
//                // gatt.readCharacteristic(characteristic)
//             //   gatt.getService(UUID_SERVICE).
//             //   characteristic.getDescriptor().


//            //=================已经蓝牙设备UUID的基础上往蓝牙设备写数据 自己测试===
//                val characteristic = gatt.getService(UUID_SERVICE).getCharacteristic(SERVER_RX_UUID)
//            //    val descriptor = characteristic.getDescriptor(SERVER_RX_UUID)
//                val defaultDescriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
//                Log.d(TAG, "222222222 defaultDescriptor="+defaultDescriptor )
//                //                // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);//和通知类似,但服务端不主动发数据,只指示客户端读取数据
//                    defaultDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
//
//                gatt.writeDescriptor(defaultDescriptor)//使能Serivce的某个Characteristic的某个descriptor 才能收到回调
//                gatt.setCharacteristicNotification(characteristic, true)//设置读
//                //todo  writeDescriptor和writeCharacteristic两个同时调用只能生效一个不能同时调用
//                try {
//                    Thread.sleep(1000)//延时
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
//                characteristic.setValue(BLEDeviceCommand.getBikeInfo())
//
//
//                gatt.writeCharacteristic(characteristic)
//                Log.d(TAG, "!!!!!!!!!!!!!! " )

                //====================
/*                Log.d(TAG, "onLeScan211: services size=" + gatt.services.size)
                val bluetoothGattServices = gatt.services
                val gorupList = ArrayList<String>()
                val childList = ArrayList<List<BluetoothGattCharacteristic>>()
                for (i in bluetoothGattServices.indices) {
                    gorupList.add("未知的服务")
                }
                for (i in bluetoothGattServices.indices) {
                    val childTemp = ArrayList<BluetoothGattCharacteristic>()
                    for (j in 0 until bluetoothGattServices[i].characteristics.size) {
                        childTemp.add(bluetoothGattServices[i].characteristics[j])
                        if (j == bluetoothGattServices[i].characteristics.size - 1) {
                            Log.d(TAG, "onServicesDiscovered: 发现新的characteristics 加入集合")
                            childList.add(childTemp)
                        }
                    }
                }
                //                for (int i = 0; i < childList.size(); i++) {
                //                    Log.d(TAG, "onServicesDiscovered: 22222");
                //                    for (int j = 0; j < childList.get(i).size(); j++) {
                //                        Log.d(TAG, "onServicesDiscovered: 33333  value=" + childList.get(i).get(j));
                //                    }
                //                }
                temp1 = gorupList
                temp2 = childList
                updataUIHandler.sendEmptyMessageDelayed(0, 0)*/
//                for (index in bluetoothGattServices) {
//                    Log.d(TAG, "onServicesDiscovered: 服务连接成功 index=")
//                }
                //       Log.d(TAG, "onServicesDiscovered: GATT 服务连接成功 status=$status")
            } else {
                Log.d(TAG, "onServicesDiscovered: GATT 服务连接失败 status=$status")
                ifServiceConnect = false
                updataUIHandler.sendEmptyMessageDelayed(4, 0)
            }

        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {//往Service写数据的结果回调
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.d(
                TAG,
                "onCharacteristicRead: 收到BLE设备发送的数据 characteristic=" + characteristic.value[0] + "size=" + characteristic.value.size
            )
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {//从Service读数据的结果回调
            super.onCharacteristicWrite(gatt, characteristic, status)
            for (index in characteristic.value) {
                Log.d(TAG, "onCharacteristicWrite: index= " + index)
            }
            Thread.sleep(500)
            val bluetoothGattService = bluetoothGatt?.getService(UUID_SERVICE)
            val readCharacteristic = bluetoothGattService?.getCharacteristic(SERVER_RX_UUID)
            bluetoothGatt?.readCharacteristic(readCharacteristic)
            Log.d(TAG, "onCharacteristicWrite: response= " + characteristic.value.size + " status=" + status)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {//BLE服务端数据读取成功会回调;一般在此接收BLE设备的回调数据
            super.onCharacteristicChanged(gatt, characteristic)
            val valueStr = characteristic.value

            val resultMessage=Message.obtain()
            val resultBundle=Bundle()
            resultBundle.putByte("key_result",valueStr[2])
            resultMessage.data=resultBundle
            resultMessage.what=5
            updataUIHandler.sendMessage(resultMessage)

//            for (index in characteristic.value) {
//                Log.d(TAG, "onCharacteristicChanged111: index= " + index)
//            }
            Log.d(TAG, "onCharacteristicChanged:")
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
            Log.d(TAG, "onDescriptorRead: ")
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            //     Log.d(TAG, "onDescriptorWrite: descriptor="+descriptor.value[0])
            for (index in descriptor.value) {
                Log.d(TAG, "onDescriptorWrite: index= " + index)
            }
            Log.d(TAG, "onDescriptorWrite: ")
        }
    }


    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        if (bluetoothGatt != null) {
            Log.d(TAG, "onDestroy  disconnect")
            bluetoothGatt?.close()
            bluetoothGatt?.disconnect()
        }
    }

    companion object {
        private val TAG = "DataControlActivity"
//        private val UUID_SERVICE =
//            java.util.UUID.fromString("0783B03E-8535-B5A0-7140-A304D2495CB7")//蓝牙串口的通用UUID,UUID是什么东西
//        private val UUID_CHARACTERISTIC_READ = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FC")
//        private val UUID_CHARACTERISTIC_WRITE = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FD")
//        private val UUID_DESCRIPTOR = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FE")
//        private val UUID_DESCRIPTOR_NOTIFY = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FF")

        private val UUID_SERVICE =
            java.util.UUID.fromString("0783B03E-8535-B5A0-7140-A304D2495CB7")//蓝牙串口的通用UUID,UUID是什么东西
        private val SERVER_TX_UUID = java.util.UUID.fromString("0783B03E-8535-B5A0-7140-A304D2495CBA")//写CHARACTERISTIC
        private val SERVER_RX_UUID = java.util.UUID.fromString("0783B03E-8535-B5A0-7140-A304D2495CB9")//读CHARACTERISTIC
        private val SERVER_NOTIFY_UUID =
            java.util.UUID.fromString("0783B03E-8535-B5A0-7140-A304D2495CB8")//读CHARACTERISTIC
        private val CLIENT_CHARACTERISTIC_CONFIG =
            java.util.UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")//默认de

    }
}
