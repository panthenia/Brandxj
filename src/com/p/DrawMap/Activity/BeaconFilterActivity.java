package com.p.DrawMap.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by p on 2015/4/2.
 */
public class BeaconFilterActivity extends Activity {
    BeaconFilterAdapter major_adapter = null;
    BeaconFilterAdapter uuid_adapter = null;
    BeaconFilterAdapter type_adapter = null;
    ListView major_listView = null;
    ListView uuid_listView = null;
    ListView type_listView = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("白名单(无任何条件时不执行过滤)");
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
        major_listView = (ListView) findViewById(R.id.major_filters);
        uuid_listView = (ListView) findViewById(R.id.uuid_filters);
        type_listView = (ListView) findViewById(R.id.type_filters);

        major_listView.setOnItemLongClickListener(new OnDeleteListItemListener());
        uuid_listView.setOnItemLongClickListener(new OnDeleteListItemListener());
        type_listView.setOnItemLongClickListener(new OnDeleteListItemListener());

        major_adapter = new BeaconFilterAdapter(this,"major");
        major_adapter.setData(PublicData.getInstance().majorFilters);
        major_listView.setAdapter(major_adapter);
        PublicData.getInstance().setListViewHeightBasedOnChildren(major_listView);

        uuid_adapter = new BeaconFilterAdapter(this,"uuid");
        uuid_adapter.setData(PublicData.getInstance().uuidFilters);
        uuid_listView.setAdapter(uuid_adapter);
        PublicData.getInstance().setListViewHeightBasedOnChildren(uuid_listView);


        type_adapter = new BeaconFilterAdapter(this,"type");
        type_adapter.setData(PublicData.getInstance().typeFilters);
        type_listView.setAdapter(type_adapter);
        PublicData.getInstance().setListViewHeightBasedOnChildren(type_listView);



    }

    class OnDeleteListItemListener implements AdapterView.OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final BeaconFilterAdapter beaconFilterAdapter = (BeaconFilterAdapter) parent.getAdapter();
            final String filter = (String) beaconFilterAdapter.getItem(position);
            beaconFilterAdapter.removeItem(position);
            final String adpType = beaconFilterAdapter.getType();
            new AlertDialog.Builder(BeaconFilterActivity.this)
                    .setTitle("是否删除该项？")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (adpType.equals("major")) {
                                PublicData.getInstance().majorFilters.remove(filter);
                                PublicData.getInstance().removeFilterFromDb(filter, PublicData.MAJOR_FILTER);
                                beaconFilterAdapter.notifyDataSetChanged();
                            } else if (adpType.equals("uuid")) {
                                PublicData.getInstance().uuidFilters.remove(filter);
                                PublicData.getInstance().removeFilterFromDb(filter, PublicData.UUID_FILTER);
                                beaconFilterAdapter.notifyDataSetChanged();
                            } else {
                                PublicData.getInstance().typeFilters.remove(filter);
                                PublicData.getInstance().removeFilterFromDb(filter, PublicData.TYPE_FILTER);
                                beaconFilterAdapter.notifyDataSetChanged();
                            }

                        }
                    })
                    .create().show();
            return true;
        }
    }
    public void onAddFilterClicked(View v){
        String alertInfo = null;
        BeaconFilterAdapter tadptor = null;
        ArrayList<String> tfilterData = null;
        int tfilterType = 0;
        switch (v.getId()){
            case R.id.major_add:
                alertInfo = "Major(精确匹配)";
                tadptor = major_adapter;
                tfilterType = PublicData.MAJOR_FILTER;
                tfilterData = PublicData.getInstance().majorFilters;
                break;
            case R.id.uuid_add:
                alertInfo = "UUID(精确匹配)";
                tadptor = uuid_adapter;
                tfilterType = PublicData.UUID_FILTER;
                tfilterData = PublicData.getInstance().uuidFilters;
                break;
            case R.id.type_add:
                tadptor = type_adapter;
                tfilterType = PublicData.TYPE_FILTER;
                tfilterData = PublicData.getInstance().typeFilters;
                alertInfo = "beacon类型(模糊)";
                break;
        }
        final BeaconFilterAdapter adptor = tadptor;
        final ArrayList<String> filterData = tfilterData;
        final int filterType = tfilterType;
        View inflatev = LayoutInflater.from(BeaconFilterActivity.this).inflate(R.layout.new_filter, null);
        final EditText nval = (EditText) inflatev.findViewById(R.id.filter_val);
        new AlertDialog.Builder(BeaconFilterActivity.this)
                .setTitle("添加允许的"+alertInfo)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(inflatev)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newVal = nval.getText().toString();
                        Log.d("newval",newVal);
                        adptor.addItem(newVal);
                        filterData.add(newVal);
                        PublicData.getInstance().saveFilter2Db(newVal,filterType);
                    }
                })
                .create().show();
    }
}