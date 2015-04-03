package com.p.DrawMap.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.p.DrawMap.DataType.BeaconFilter;
import com.p.DrawMap.Utils.BeaconFilterAdapter;
import com.p.DrawMap.DataType.PublicData;
import com.p.DrawMap.R;

/**
 * Created by p on 2015/4/2.
 */
public class BeaconFilterActivity extends Activity {
    BeaconFilterAdapter adapter = null;
    ListView beaconList = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("白名单");
        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if (title != null) {
                title.setTextColor(Color.WHITE);
            }
        }
        getActionBar().setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.backcolor_norock));
        setContentView(R.layout.whitelist_layout);
        beaconList = (ListView) findViewById(R.id.white_list);
        beaconList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final BeaconFilterAdapter beaconFilterAdapter = (BeaconFilterAdapter) parent.getAdapter();
                final BeaconFilter filter = (BeaconFilter) beaconFilterAdapter.getItem(position);

                new AlertDialog.Builder(BeaconFilterActivity.this)
                        .setTitle("是否删除该项？")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PublicData.getInstance().beaconFilters.remove(filter);
                                beaconFilterAdapter.setData(PublicData.getInstance().beaconFilters);
                                PublicData.getInstance().removeFilterFromDb(filter);
                                beaconFilterAdapter.notifyDataSetChanged();
                            }
                        })
                        .create().show();
                return true;
            }
        });
        adapter = new BeaconFilterAdapter(this);
        adapter.setData(PublicData.getInstance().beaconFilters);
        beaconList.setAdapter(adapter);

    }
    public void onAddFilterClicked(View v){
        View inflatev = LayoutInflater.from(BeaconFilterActivity.this).inflate(R.layout.new_filter, null);
        final EditText emajor = (EditText) inflatev.findViewById(R.id.filter_major);
        final EditText euuid = (EditText) inflatev.findViewById(R.id.filter_uuid);
        new AlertDialog.Builder(BeaconFilterActivity.this)
                .setTitle("添加过滤白名单")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(inflatev)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String major = emajor.getText().toString();
                        String uuid = euuid.getText().toString();
                        BeaconFilter filter = new BeaconFilter();
                        filter.major = major;
                        filter.uuid = uuid;
                        PublicData.getInstance().beaconFilters.add(filter);
                        PublicData.getInstance().saveFilter2Db(filter);
                        adapter.setData(PublicData.getInstance().beaconFilters);
                        adapter.notifyDataSetChanged();
                    }
                })
                .create().show();
    }
}