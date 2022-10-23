package com.zktony.www.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.zktony.www.R
import java.lang.reflect.Field

/** 一个通用的下拉适配器,下拉的数据的类不一样，需要展示在界面上的字段也不一样的时候
 * @param context  上下文
 * @param data    下拉的数据源
 * @param visibleField 用来展示的字段
 * @param class1   数据源的类型
 */
class SpinnerAdapter<T>(
    val context: Context,
    val data: ArrayList<T>,
    private val visibleField: String,
    private val class1: Class<T>
) : BaseAdapter() {

    //下拉中每个item的显示的样子
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View =
            convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_spinner, parent, false)
        val textView = view.findViewById<TextView>(R.id.textView)
        //获取到当前位置的item数据
        val dic: T = getItem(position)
        //通过反射得到该字段的值,并且显示在textview上面
        val nameField: Field = class1.getDeclaredField(visibleField)
        nameField.isAccessible = true
        if (nameField.get(dic) == null) { //如果该字段的值为null的话
            val text = "null"
            textView.text = text
        } else {
            val text: String = nameField.get(dic) as String
            textView.text = text
        }
        return view
    }

    override fun getItem(position: Int): T {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return data.size
    }
}