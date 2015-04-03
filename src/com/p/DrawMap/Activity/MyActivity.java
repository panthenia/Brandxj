package com.p.DrawMap.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.lef.scanner.*;
import com.p.DrawMap.Utils.NetWorkService;
import com.p.DrawMap.DataType.PublicData;
import com.p.DrawMap.R;
import com.p.DrawMap.Utils.ScanView;

import java.util.Collection;
import java.util.Iterator;

public class MyActivity extends Activity implements
        com.lef.scanner.IBeaconConsumer{
    public static final int REDRAW_SCAN_VIEW = 1;
    public static final int FIND_NEW_BEACON = 2;
    public static final int REQUEST_FINISH_SUCCESS = 0;
    public static final int KEY_TIME_OUT = 4;
    public static final int REQUEST_FINISH_FAIL = 3;

    private IBeaconManager iBeaconManager;
    ScanView scanView = null;
    Button btSee,btReset,btUpload,btWhite;
    boolean scanStoped = false;
    ImageView stopImage = null;
    Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case REDRAW_SCAN_VIEW:
                    scanView.rePaint();
                    break;
                case FIND_NEW_BEACON:
                    scanView.setFindNum(PublicData.getInstance().beacons.size());
                    scanView.rePaint();
                    break;
                case KEY_TIME_OUT:
                    new AlertDialog.Builder(MyActivity.this)
                            .setTitle("巡检数据上报失败！")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setMessage("登录超时，请重新登录!")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(MyActivity.this,LoginActivity.class);
                                    startActivityForResult(intent, 5);
                                }
                            })
                            .create().show();
                    break;
                case REQUEST_FINISH_SUCCESS:
                    Toast.makeText(MyActivity.this,"巡检数据上报成功！",Toast.LENGTH_LONG).show();
                    break;
                case REQUEST_FINISH_FAIL:
                    Toast.makeText(MyActivity.this, "巡检数据上报失败！", Toast.LENGTH_LONG).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    public void startDrawSignal(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    Message ms = new Message();
                    ms.what = REDRAW_SCAN_VIEW;
                    mhandler.sendMessage(ms);
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("周围的Beacons");
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
        setContentView(R.layout.myactivity_activity);
        iBeaconManager = IBeaconManager.getInstanceForApplication(this);
        PublicData.getInstance().getHandlerHashMap().put(MyActivity.class.getName(),mhandler);
        btReset = (Button)findViewById(R.id.bt_reset);
        btSee = (Button)findViewById(R.id.bt_see);
        btUpload = (Button)findViewById(R.id.bt_upload);
        btWhite = (Button)findViewById(R.id.bt_white);
        btWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyActivity.this,BeaconFilterActivity.class);
                MyActivity.this.startActivity(intent);
            }
        });
        stopImage = (ImageView)findViewById(R.id.bt_stop_run);
        stopImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scanStoped){
                    scanStoped = false;
                    stopImage.setImageResource(R.drawable.f04c);
                    if (!iBeaconManager.isBound(MyActivity.this)){
                        iBeaconManager.bind(MyActivity.this);
                    }
                    scanView.setEnabled(true);
                }else{
                    scanStoped = true;
                    stopImage.setImageResource(R.drawable.f051);
                    if (iBeaconManager.isBound(MyActivity.this))
                        iBeaconManager.unBind(MyActivity.this);
                    scanView.setEnabled(false);
                }
            }
        });
        btReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MyActivity.this)
                        .setTitle("警告")
                        .setMessage("是否重置状态").setCancelable(false)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                PublicData.getInstance().checkBeaconSet.clear();
                                PublicData.getInstance().beacons.clear();
                                PublicData.getInstance().uploadBeaconSet.clear();
                                scanView.setFindNum(0);
                                PublicData.getInstance().removeCheckedBeaconInDb();
                                Toast.makeText(MyActivity.this,"状态重置成功！",Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();

            }
        });
        btSee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyActivity.this,ShowActivity.class);
                startActivity(intent);
            }
        });
        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PublicData.getInstance().isNetworkAvailable()) {
                    if(PublicData.getInstance().isLogin()){
                        Intent intent = new Intent(MyActivity.this, NetWorkService.class);
                        intent.putExtra("ActivityName", MyActivity.class.getName());
                        intent.putExtra("ReuqestType", "upload_checked");
                        startService(intent);
                        Toast.makeText(MyActivity.this, "开始上传...", Toast.LENGTH_SHORT).show();
                    }else {
                        Intent intent = new Intent(MyActivity.this,LoginActivity.class);
                        startActivityForResult(intent,5);
                    }

                }else{
                    Toast.makeText(MyActivity.this, "当前无网络连接！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        scanView = (ScanView) findViewById(R.id.scan_view);
        scanView.setFindNum(PublicData.getInstance().beacons.size());
        startDrawSignal();
    }
    private void initBluetooth() {
        // TODO Auto-generated method stub
        final BluetoothAdapter blueToothEable = BluetoothAdapter
                .getDefaultAdapter();
        if (!blueToothEable.isEnabled()) {
            new AlertDialog.Builder(MyActivity.this)
                    .setTitle("蓝牙开启")
                    .setMessage("配置需要开启蓝牙").setCancelable(false)
                    .setPositiveButton("开启", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            blueToothEable.enable();
                            iBeaconManager.bind(MyActivity.this);
                        }
                    }).setNegativeButton("退出", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    MyActivity.this.finish();
                }
            }).create().show();
        } else {
            iBeaconManager.setForegroundScanPeriod(800);
            iBeaconManager.bind(this);
        }
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        // if (iBeaconManager.isBound(this)) {
        // iBeaconManager.unBind(this);
        // }
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (iBeaconManager != null && !iBeaconManager.isBound(this)) {
//            if(PublicData.getInstance().beacons.size() > 0)
//                PublicData.getInstance().beacons.clear();
            // 蓝牙dialog
            initBluetooth();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("return","resultCode"+requestCode);
        if(requestCode == 5 && resultCode == 5) {
            Intent intent = new Intent(MyActivity.this, NetWorkService.class);
            intent.putExtra("ActivityName", MyActivity.class.getName());
            intent.putExtra("ReuqestType", "upload_checked");
            startService(intent);
            Toast.makeText(this, "开始上传...", Toast.LENGTH_SHORT).show();
        }

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.upload, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_upload:
//                if(PublicData.getInstance().isNetworkAvailable()) {
//                    if(PublicData.getInstance().isLogin()){
//                        Intent intent = new Intent(MyActivity.this, NetWorkService.class);
//                        intent.putExtra("ActivityName", MyActivity.class.getName());
//                        intent.putExtra("ReuqestType", "upload_checked");
//                        startService(intent);
//                        Toast.makeText(this, "开始上传...", Toast.LENGTH_SHORT).show();
//                    }else {
//                        Intent intent = new Intent(MyActivity.this,LoginActivity.class);
//                        startActivityForResult(intent,5);
//                    }
//
//                }else{
//                    Toast.makeText(this, "当前无网络连接！", Toast.LENGTH_SHORT).show();
//                }
//                return true;
//
//            case R.id.action_see:
//                Intent intent = new Intent(MyActivity.this,ShowActivity.class);
//                startActivity(intent);
//                return true;
//            case R.id.action_reset:
//                PublicData.getInstance().checkBeaconSet.clear();
//                PublicData.getInstance().beacons.clear();
//                PublicData.getInstance().uploadBeaconSet.clear();
//                scanView.setFindNum(0);
//                PublicData.getInstance().removeCheckedBeaconInDb();
//                Toast.makeText(this,"状态重置成功！",Toast.LENGTH_SHORT).show();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (iBeaconManager != null && iBeaconManager.isBound(this)) {
            iBeaconManager.unBind(this);
        }
    }
    @Override
    public void onIBeaconServiceConnect() {
        // TODO Auto-generated method stub
        // 启动Range服务
        iBeaconManager.setRangeNotifier(new RangeNotifier() {

            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons,
                                                Region region) {
                Log.d("filter","-------------------------------");

                for (IBeacon temp : iBeacons) {
                    Log.d("filter", temp.getBluetoothAddress() + "-" + temp.getProximityUuid() + "-" + temp.getMajor());
                    if (PublicData.getInstance().isUnderFilter(temp)) {
                        if (!PublicData.getInstance().checkBeaconSet.contains(temp.getBluetoothAddress())) {
                            PublicData.getInstance().beacons.add(temp);
                            PublicData.getInstance().checkBeaconSet.add(temp.getBluetoothAddress());
                            mhandler.sendEmptyMessage(FIND_NEW_BEACON);
                            PublicData.getInstance().saveCheckBeacon2Db(temp);
                        }
                    }
                    //
                }
                //if (ProgressBarVisibile) {
                //    handler.sendEmptyMessage(PROGRESSBARGONE);
                // }
                // java.util.Iterator<IBeacon> iterator = iBeacons.iterator();
                // while (iterator.hasNext()) {
                // IBeacon temp = iterator.next();
                // if (beaconDataListA.contains(temp)) {
                // beaconDataListA.set(beaconDataListA.indexOf(temp), temp);
                // handler.sendEmptyMessage(UPDATEUI);
                // } else {
                // beaconDataListA.add(temp);
                // handler.sendEmptyMessage(UPDATEUI);
                // }
                //
                // }

            }

            @Override
            public void onNewBeacons(Collection<IBeacon> iBeacons, Region region) {
                // TODO Auto-generated method stub
                // beaconDataListA.addAll(iBeacons);
                // handler.sendEmptyMessage(UPDATEUI);

            }

            @Override
            public void onGoneBeacons(Collection<IBeacon> iBeacons,
                                      Region region) {
                // TODO Auto-generated method stub
//                Iterator<IBeacon> iterator = iBeacons.iterator();
//                while (iterator.hasNext()) {
//                    IBeacon temp = iterator.next();
//                    if (aroundBeaconDataList.contains(temp)) {
//                        aroundBeaconDataList.remove(temp);
//                    }
//                    handler.sendEmptyMessage(UPDATEUI);
//                }
            }

            @Override
            public void onUpdateBeacon(Collection<IBeacon> iBeacons,
                                       Region region) {
                // TODO Auto-generated method stub
//                Iterator<IBeacon> iterator = iBeacons.iterator();
//                while (iterator.hasNext()) {
//                    IBeacon temp = iterator.next();
//                    if (!PublicData.getInstance().beacons.contains(temp)) {
//                        PublicData.getInstance().beacons.add(temp);
//                    }
//                    mhandler.sendEmptyMessage(FIND_NEW_BEACON);
//                }
            }

        });
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {

            @Override
            public void didExitRegion(Region region) {
                // TODO Auto-generated method stub
            }

            @Override
            public void didEnterRegion(Region region) {
                // TODO Auto-generated method stub

            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                // TODO Auto-generated method stub

            }
        });
        try {
            Region myRegion = new Region("myRangingUniqueId", null, null, null);
            iBeaconManager.startRangingBeaconsInRegion(myRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
