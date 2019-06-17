package com.example.kotlinbleclient.adpter

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.example.kotlinbleclient.R
import java.util.ArrayList

class MyExpandListViewAdapter : BaseExpandableListAdapter() {
    private var groups: MutableList<String> = ArrayList()

    private var children: MutableList<List<BluetoothGattCharacteristic>> = ArrayList()

    fun getGroups(): List<String> {
        return groups
    }

    fun setGroups(groups: MutableList<String>) {
        this.groups = groups
    }

    fun getChildren(): List<List<BluetoothGattCharacteristic>> {
        return children
    }

    fun setChildren(children: MutableList<List<BluetoothGattCharacteristic>>) {
        this.children = children
    }

    fun updataMyData(groups: List<String>, children: List<List<BluetoothGattCharacteristic>>) {
        this.groups.clear()
        this.children.clear()
        notifyDataSetChanged()
        this.groups.addAll(groups)
        this.children.addAll(children)
        notifyDataSetChanged()
        //   notifyDataSetInvalidated();
    }

    override fun getGroupCount(): Int {
        Log.d(TAG, "getGroupCount: ")
        return groups.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        Log.d(TAG, "getChildrenCount: ")
        return children[groupPosition].size
    }

    override fun getGroup(groupPosition: Int): Any {
        Log.d(TAG, "getGroup: ")
        return groups[groupPosition]
        //  return groups[groupPosition];
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        Log.d(TAG, "getChild: ")
        return children[groupPosition][childPosition]
        //   return children[groupPosition][childPosition];
    }

    override fun getGroupId(groupPosition: Int): Long {
        Log.d(TAG, "getGroupId: ")
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        Log.d(TAG, "getChildId: ")
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        Log.d(TAG, "hasStableIds: ")
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        Log.d(TAG, "getGroupView: ")
        val groupViewHolder: GroupViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.context).inflate(R.layout.item_expand_group, parent, false)
            groupViewHolder = GroupViewHolder()
            groupViewHolder.tvTitle = convertView!!.findViewById(R.id.label_expand_group) as TextView
            convertView.tag = groupViewHolder
        } else {
            groupViewHolder = convertView.tag as GroupViewHolder
        }
        groupViewHolder.tvTitle!!.text = groups[groupPosition]
        return convertView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val childViewHolder: ChildViewHolder
        Log.d(TAG, "getChildView: size=" + groups.size)
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.context).inflate(R.layout.item_expand_child, parent, false)
            childViewHolder = ChildViewHolder()
            childViewHolder.tvTitle = convertView!!.findViewById(R.id.label_expand_child) as TextView
            childViewHolder.bleDeviceProperties =
                convertView.findViewById(R.id.label_expand_child_bleDeviceProperties) as TextView
            convertView.tag = childViewHolder
        } else {
            childViewHolder = convertView.tag as ChildViewHolder
        }
        childViewHolder.tvTitle!!.text = "UUID:" + children[groupPosition][childPosition].uuid
        childViewHolder.bleDeviceProperties!!.text = "Properties:" + children[groupPosition][childPosition].uuid
        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        Log.d(TAG, "isChildSelectable: ")
        return true
    }

    internal class GroupViewHolder {
        var tvTitle: TextView? = null
    }

    internal class ChildViewHolder {
        var tvTitle: TextView? = null
        var bleDeviceProperties: TextView? = null
    }

    companion object {
        private val TAG = "MyExpandListViewAdapter"
    }
}