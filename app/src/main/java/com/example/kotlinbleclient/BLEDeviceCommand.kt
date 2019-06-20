package com.example.kotlinbleclient

import android.util.Log
import java.util.*
import kotlin.collections.ArrayList

/**
 * 虾米BLE设备通讯指令管理单例类
 */
object BLEDeviceCommand {


    val COMMAND_OPEN_BIKE: Byte = 0x20//车辆启动:撤防+解锁后轮+点火
    val COMMAND_STOP_BIKE: Byte = 0x20 //车辆关闭:设防+上锁后轮+熄火
    val COMMAND_OPEN_BATLOCK: Byte = 0x24//打开车辆鞍锁(电池锁)
    val COMMAND_CLOSE_BATLOCK: Byte = 0x24//关闭车辆鞍锁(电池锁)
    val COMMAND_OPEN_WHELLLOCK: Byte = 0x25//控制后轮锁打开
    val COMMAND_CLOSE_WHELLLOCK: Byte = 0x25//控制后轮锁关闭
    val COMMAND_RESTART_BIKE: Byte = 0x26//重启车辆控制盒
    val COMMAND_OPEN_ELECTIC: Byte = 0x27//打开车辆电路(点火)
    val COMMAND_CLOSE_ELECTIC: Byte = 0x27//打开车辆电路(熄火)
    val COMMAND_TURNOFF_BIKE: Byte = 0x30//关机
    //val COMMAND_STOP_BIKE = 0x28//播放语音
    //val COMMAND_STOP_BIKE = 0x2C 启动操作:撤防+解锁后轮+点火
    //val COMMAND_STOP_BIKE = 0x2D 熄火:撤防+熄火
    //val COMMAND_STOP_BIKE = 0x2E 控制后轮解锁
    //val COMMAND_STOP_BIKE = 0x2F 控制后轮上锁
    //val COMMAND_STOP_BIKE = 0x34 控制电池仓解锁
    //val COMMAND_STOP_BIKE = 0x35 控制电池仓上锁
    /**
     * 获取指定类型BLE控制命令
     */
    fun parseBLEByte(byteType: Byte, tokenArray: ByteArray, orderArray: ByteArray): ByteArray {
        val byteList = mutableListOf<Byte>()
        byteList.add(0, byteType)
        byteList.add(1, (tokenArray.size + orderArray.size).toByte())
        byteList.add(2, tokenArray[0])
        byteList.add(3, tokenArray[1])
        byteList.add(4, tokenArray[2])
        byteList.add(5, tokenArray[3])
        if (orderArray.isEmpty()) {//如果命令数据为空则数据只由token组成
            byteList.add(6, getCalSum(byteList))
            Log.d("testByte", "AAAAAAAAAAAAAA" )
        } else {//如果命令数据不为空则数据由token组成和order组成
            byteList.add(6, orderArray[0])
            byteList.add(7,getCalSum(byteList))
            Log.d("testByte", "BBBBBBBBBBBBBBBBBBB")
        }
        for (index in byteList.indices) {
         //   Log.d("testByte", "index1111=" + byteList.get(index)+" index="+index)
        }
        for (index in byteList) {
          //  Log.d("testByte", "index222=" + index+" byteList="+byteList)//?????????什么情况
        }
        return byteList.toByteArray()
    }

//
//    /**
//     * 获取指定类型BLE控制命令
//     */
//    fun parseBLEShort(charArray: CharArray, tokenArray: CharArray, orderArray: CharArray): CharArray {
//        val byteList = mutableListOf<Byte>()
//        byteList.add(0, byteType)
//        byteList.add(1, (tokenArray.size + orderArray.size).toByte())
//        byteList.add(2, tokenArray[0])
//        byteList.add(3, tokenArray[1])
//        byteList.add(4, tokenArray[2])
//        byteList.add(5, tokenArray[3])
//        if (orderArray.isEmpty()) {//如果命令数据为空则数据只由token组成
//            byteList.add(6, getCalSum(byteList))
//        } else {//如果命令数据不为空则数据由token组成和order组成
//            byteList.add(6, orderArray[0])
//            //      byteList.add(7, getCalSum(byteList))
//            byteList.add(7,getCalSum(byteList))
//        }
//        for (index in byteList) {
//            Log.d("testByte", "index=" + index)
//        }
//        return byteList.to
//    }


    fun getCalSum(byteList: MutableList<Byte>): Byte {
        var sum: Int = 0
        for (index in byteList) {
            sum += index
        }
        Log.d("testByte", "sum11=" + sum)
        return sum.toByte()
    }
}