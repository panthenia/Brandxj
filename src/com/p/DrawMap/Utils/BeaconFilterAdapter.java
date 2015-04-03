package com.p.DrawMap.Utils;

import android.content.Context;
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
    ArrayList<BeaconFilter> data = new ArrayList<BeaconFilter>();
    LayoutInflater inflater = null;
    public BeaconFilterAdapter(Context context){
        context = context;
        inflater = LayoutInflater.from(context);
    }
    public void setData(ArrayList<BeaconFilter> data){
        this.data.clear();
        this.data.addAll(data);
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
        ViewHoldler holdler = null;
        //回收旧的view
        if (convertView != null){
            holdler = (ViewHoldler) convertView.getTag();
        }else{
            convertView = inflater.inflate(R.layout.whitelist_item,null);
            holdler = new ViewHoldler();
            holdler.uuid = (TextView) convertView.findViewById(R.id.white_uuid);
            holdler.major = (TextView) convertView.findViewById(R.id.white_major);
            convertView.setTag(holdler);
        }
        holdler.major.setText("Major:"+data.get(position).major);
        holdler.uuid.setText("Uuid:"+data.get(position).uuid);
        return convertView;
    }
    class ViewHoldler{
        TextView major;
        TextView uuid;
    }
}
