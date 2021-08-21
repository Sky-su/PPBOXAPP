package com.ives.ppboxapp.rfidutil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ives.ppboxapp.MainActivity;
import com.ives.ppboxapp.R;
import com.ives.ppboxapp.utils.SoundPoolHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ReadRfidListon extends BaseAdapter {

    private List<String> mData;
    private Context context;
    private int currentItem = -1;//listview中显示位置，取默认值为-1。
    private SoundPoolHelper soundPoolHelper = null;


    public ReadRfidListon(List<String> mData, Context context) {
        this.mData = mData;
        this.context = context;
        soundPoolHelper = new SoundPoolHelper(4, SoundPoolHelper.TYPE_MUSIC)
                .setRingtoneType(SoundPoolHelper.RING_TYPE_MUSIC)
                //加载默认音频，因为上面指定了，所以其默认是：RING_TYPE_MUSIC
                .loadDefault(context)
                .load(context, "happy1", R.raw.duka3);
    }

    public void sound(){
        soundPoolHelper.play("happy1",false);

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
        holder.listItemID.setText(mData.get(position));
        notifyDataSetChanged();
        return convertView;
    }

    /**添加数据**/
    public void additem(String data) {
        if (mData == null) {
            mData = new LinkedList<>();
        } else {
            Boolean td = false;
            for (String item : mData) {
                if (item.equals(data)) {
                    td = true;
                    break;
                }
            }
            if (td == false) {
                mData.add(data);
                Collections.sort(mData);
                soundPoolHelper.play("happy1",false);
                notifyDataSetChanged();
            }
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
        return mData;
     }


    public class ViewHolder{
        TextView listItemID;
    }
}
