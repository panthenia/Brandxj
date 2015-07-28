package com.p.DrawMap.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.p.DrawMap.DataType.BeaconFilter;
import com.p.DrawMap.R;

import java.util.ArrayList;

/**
 * Created by p on 2015/4/2.
 */
public class BeaconFilterAdapter extends BaseAdapter {
    Context context = null;
    ArrayList<String> data = new ArrayList<String>();
    LayoutInflater inflater = null;
    String filterType = null;
    public BeaconFilterAdapter(Context context,String type){
        context = context;
        filterType = type;
        inflater = LayoutInflater.from(context);
    }
    public void setData(ArrayList<String> data){
        this.data.clear();
        this.data.addAll(data);
    }
    public String getType(){
        return filterType;
    }
    public void removeItem(int pos){
        this.data.remove(pos);
    }
    public void addItem(String item){
        data.add(item);
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("newval-slash","---------------------");
        Log.d("newval-getview-postion",String.valueOf(position));
        ViewHoldler holdler = null;
        //回收旧的view
        if (convertView != null){
            holdler = (ViewHoldler) convertView.getTag();
            holdler.content.setText(data.get(position));
        }else{
            convertView = inflater.inflate(R.layout.whitelist_item,null);
            holdler = new ViewHoldler();
            holdler.content = (TextView)convertView.findViewById(R.id.major_content);
            Log.d("newval-getview-data",data.get(position));
            holdler.content.setText(data.get(position));
            convertView.setTag(holdler);
        }

        return convertView;
    }
    class ViewHoldler{
        TextView content;
    }
}
