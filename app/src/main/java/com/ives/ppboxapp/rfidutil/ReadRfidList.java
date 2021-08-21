package com.ives.ppboxapp.rfidutil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ives.ppboxapp.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ReadRfidList extends BaseAdapter {

    private List<RfidView> mData;
    private Context context;
    private int currentItem = -1;//listview中显示位置，取默认值为-1。

    public ReadRfidList(List<RfidView> mData, Context context) {
        this.mData = mData;
        this.context = context;
    }

    public void clear() {
        if (mData != null){
            mData.clear();
        }
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_listtest,null);
            holder = new ViewHolder();
            holder.listItemID = (TextView)convertView.findViewById(R.id.RFIDitem);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        //赋值
        holder.listItemID.setText(mData.get(position).getName());
        notifyDataSetChanged();
        return convertView;
    }

    /**添加数据**/
    public void additem(String data) {
        if (mData == null) {
            mData = new LinkedList<>();
        } else {
            Boolean td = false;
            for (RfidView item : mData) {
                if (item.getName().equals(data)) {
                    item.setNumber(item.getNumber() + 1);
                   // notifyDataSetChanged();
                    td = true;
                    break;
                }
            }
            if (td == false) {
                mData.add(new RfidView(data, 1));
            }
            notifyDataSetChanged();
        }

    }


    public static List<RfidView> removeDuplicate(List<RfidView> list)
    {
        Set set = new LinkedHashSet<RfidView>();
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }

     public List<String> getitems(){
        List<String> list = new ArrayList<String>();
         if (mData != null){
             for (RfidView view : mData){
                 list.add(view.getName());
             }
         }
        return list;
     }


    public class ViewHolder{
        TextView listItemID;
    }
}
