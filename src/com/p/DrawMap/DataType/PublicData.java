package com.p.DrawMap.DataType;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.lef.scanner.IBeacon;
import com.p.DrawMap.R;
import com.p.DrawMap.Utils.DataUtil;
import com.p.DrawMap.Utils.DatabaseContext;

import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by p on 2015/3/3.
 *
 */
public class PublicData extends Application {
    public static final int MAJOR_FILTER = 0;
    public static final int UUID_FILTER = 1;
    public static final int TYPE_FILTER = 2;
    private static PublicData self;
    public ArrayList<IBeacon> beacons = new ArrayList<IBeacon>();
    public HashSet<String> checkBeaconSet = new HashSet<String>();
    public HashSet<String> uploadBeaconSet = new HashSet<String>();
    public ArrayList<BeaconFilter> beaconFilters = new ArrayList<BeaconFilter>();
    public ArrayList<String> majorFilters = new ArrayList<String>();
    public ArrayList<String> uuidFilters = new ArrayList<String>();
    public ArrayList<String> typeFilters = new ArrayList<String>();
    public DataUtil du;
    private String ip;
    public SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-hh-mm-ss");
    public MessageDigest md5_encriptor = null;
    public boolean isHas_save_ip() {
        return has_save_ip;
    }

    public void setHas_save_ip(boolean has_save_ip) {
        this.has_save_ip = has_save_ip;
    }

    private boolean has_save_ip;

    public boolean isHas_save_user() {
        return has_save_user;
    }

    public void setHas_save_user(boolean has_save_user) {
        this.has_save_user = has_save_user;
    }

    private boolean has_save_user;
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    private String user,psw;
    public String getPsw() {
        return psw;
    }
    public boolean isUnderFilter(IBeacon ibeancon){
        boolean mok = false,uok = false,tok = false;
        if (majorFilters.size() == 0 && uuidFilters.size() == 0 && typeFilters.size() == 0)
            return true;
        if (majorFilters.size() ==0)
            mok = true;
        if (uuidFilters.size() == 0)
            uok = true;
        if (typeFilters.size() ==0)
            tok = true;
        for (String x : majorFilters){
            if (x.equals(String.valueOf(ibeancon.getMajor()))){
                mok = true;
                break;
            }
        }
        for (String x : uuidFilters){
            if (x.equals(ibeancon.getProximityUuid())){
                uok = true;
                break;
            }
        }
        String name = ibeancon.getName()==null?"":ibeancon.getName();
        for (String x : typeFilters){
            if (name.contains(x)){
                tok = true;
                break;
            }
        }
        Log.d("filter","---------------------");
        Log.d("filter",String.format("m:%s,u:%s,t:%s",mok,uok,tok));
        return mok && uok && tok;
    }
    public void setPsw(String psw) {
        this.psw = psw;
    }

    public String getUser() {
        return user;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    boolean login = false;
    public void setUser(String user) {
        this.user = user;
    }
    private ConcurrentHashMap<String, Handler> handlerHashMap = new ConcurrentHashMap<String, Handler>();

    public ConcurrentHashMap<String, Handler> getHandlerHashMap() {
        return handlerHashMap;
    }
    private String port;
    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
        DatabaseContext dbContext = new DatabaseContext(this);
        du = new DataUtil(dbContext, this.getString(R.string.unupload_dbname), null, 1);
        try {
            md5_encriptor = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        getCheckedBeaconInDb();
        getFiltersInDb();
    }
    public static PublicData getInstance(){
        return self;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    String key;
    public String getMd5(String data){

        md5_encriptor.reset();
        byte[] data_byte;
        data_byte = data.getBytes();

        byte[] hash_data = md5_encriptor.digest(data_byte);
        StringBuilder md5StrBuff = new StringBuilder();

        for (byte aHash_data : hash_data) {
            if (Integer.toHexString(0xFF & aHash_data).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & aHash_data));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & aHash_data));
        }

        return md5StrBuff.toString();
    }
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        } else {
            //如果仅仅是用来判断网络连接
            //则可以使用 cm.getActiveNetworkInfo().isAvailable();
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public String getImei() {
        return ((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
                .getDeviceId();
    }
    public void setListViewHeightBasedOnChildren(ListView listView) {

        // 获取ListView对应的Adapter

        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {

            return;

        }

        int totalHeight = 0;

        for (int i = 0; i < listAdapter.getCount(); i++) { // listAdapter.getCount()返回数据项的数目

            View listItem = listAdapter.getView(i, null, listView);

            listItem.measure(0, 0); // 计算子项View 的宽高

            totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度

        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

        // listView.getDividerHeight()获取子项间分隔符占用的高度

        // params.height最后得到整个ListView完整显示需要的高度

        listView.setLayoutParams(params);

    }
    public void getFiltersInDb(){
        SQLiteDatabase db = du.getReadableDatabase();
        String sql,sql1,sql2;
        Cursor cursor;
        // area text,type text,time text,val text
        sql = "select * from major_filter";
        sql1 = "select * from uuid_filter";
        sql2 = "select * from type_filter";
        try {
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {//直到返回false说明表中到了数据末尾
                    String major = cursor.getString(cursor.getColumnIndex("major"));
                    majorFilters.add(major);
                }
                cursor.close();
            }
            cursor = db.rawQuery(sql1, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {//直到返回false说明表中到了数据末尾
                    String uuid = cursor.getString(cursor.getColumnIndex("uuid"));
                    uuidFilters.add(uuid);
                }
                cursor.close();
            }
            cursor = db.rawQuery(sql2, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {//直到返回false说明表中到了数据末尾
                    String type = cursor.getString(cursor.getColumnIndex("type"));
                    typeFilters.add(type);
                }
                cursor.close();
            }
        } catch (SQLException e) {

        } finally {
            //db.close();
        }
    }
    public boolean saveFilter2Db(String filter, int ftype){
        boolean result = true;
        SQLiteDatabase db = du.getReadableDatabase();
        String sql = null;
        // area text,type text,time text,val text

        switch (ftype){
            case MAJOR_FILTER:
                sql = "insert into major_filter(major) values('%s')";
                break;
            case UUID_FILTER:
                sql = "insert into uuid_filter(uuid) values('%s')";
                break;
            case TYPE_FILTER:
                sql = "insert into type_filter(type) values('%s')";
                break;
        }
        sql = String.format(sql,filter);
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        } finally {
            //db.close();
        }
        Log.d("save beacon",String.valueOf(result));
        return result;
    }
    public void removeFilterFromDb(String filter, int ftype){
        SQLiteDatabase db = du.getReadableDatabase();

        String sql = null;
        switch (ftype){
            case MAJOR_FILTER:
                sql = "delete from major_filter where  major = '%s'";
                break;
            case UUID_FILTER:
                sql = "delete from uuid_filter where  uuid = '%s'";
                break;
            case TYPE_FILTER:
                sql = "delete from type_filter where  type = '%s'";
                break;
        }
        sql = String.format(sql,filter);
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //db.close();
        }
    }
    public boolean updateCheckBeaconInDb(IBeacon iBeacon){
        boolean result = true;
        SQLiteDatabase db = du.getReadableDatabase();
        String sql;
        // area text,type text,time text,val text

        sql = "update unupbeacon set major='"+iBeacon.getMajor()+"',minor='"+iBeacon.getMinor()+"' where mac_id = '"+iBeacon.getBluetoothAddress()+"'";

        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        } finally {
            //db.close();
        }
        Log.d("update beacon",String.valueOf(result));
        return result;
    }
    public boolean saveCheckBeacon2Db(IBeacon iBeacon) {
        boolean result = true;
        SQLiteDatabase db = du.getReadableDatabase();
        String sql;
        // area text,type text,time text,val text

        sql = "insert into unupbeacon(mac_id,uuid,rssi,major,minor,type,timestamp) values('";
        sql += iBeacon.getBluetoothAddress() + "','" + iBeacon.getProximityUuid()
                + "','" + iBeacon.getRssi()
                + "','" + iBeacon.getMajor() + "','" + iBeacon.getMinor()
                + "','" + iBeacon.getName()
                + "','" + System.currentTimeMillis()
                +"')";
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        } finally {
            //db.close();
        }
        Log.d("save beacon",String.valueOf(result));
        return result;

    }
    public void saveBeaconLocation2Db(IBeacon iBeacon){
        SQLiteDatabase db = du.getReadableDatabase();
        String sql = "insert into beacon_location(mac_id,rssi,major,date) values('%s','%s','%s','%s')";
        try {
            db.execSQL(String.format(sql,iBeacon.getBluetoothAddress(),iBeacon.getRssi(),iBeacon.getMajor(),dateFormat.format(new   java.util.Date())));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //db.close();
        }
    }
    public void saveBeaconStop2Db(){
        SQLiteDatabase db = du.getReadableDatabase();
        String sql = "insert into beacon_location(mac_id,rssi,major,date) values('%s','%s','%s','%s')";
        try {
            if(db.isOpen())
                db.execSQL(String.format(sql,-1,-1,-1,dateFormat.format(new   java.util.Date())));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //db.close();
        }
    }
    public void removeCheckedBeaconInDb(){
        SQLiteDatabase db = du.getReadableDatabase();
        String sql = "delete from unupbeacon";
        String sql1 = "delete from beacon_location";
        try {
            if(db.isOpen()){
                db.execSQL(sql);
                db.execSQL(sql1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //db.close();
        }
    }
    public void removeUploadCheckedBeaconInDb(){
        SQLiteDatabase db = du.getReadableDatabase();

        String sql = "delete from unupbeacon where mac_id = '";
        try {
            for(String mac:uploadBeaconSet){
                sql += mac+"';";
                db.execSQL(sql);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //db.close();
        }
    }
    public void getCheckedBeaconInDb() {
        SQLiteDatabase db = du.getReadableDatabase();
        String sql;
        Cursor cursor;
        // area text,type text,time text,val text
        sql = "select * from unupbeacon";
        try {
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {//直到返回false说明表中到了数据末尾
                    DBIbeancon ibeacon = new DBIbeancon();
                    //Log.d("savebeacon",cursor.getString(cursor.getColumnIndex("mac_id")));
                    ibeacon.setMac(cursor.getString(cursor.getColumnIndex("mac_id")));
                    ibeacon.setMajor(cursor.getString(cursor.getColumnIndex("major")));
                    ibeacon.setMinor(cursor.getString(cursor.getColumnIndex("minor")));

                    ibeacon.setRssi(cursor.getString(cursor.getColumnIndex("rssi")));
                    ibeacon.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
                    ibeacon.setType(cursor.getString(cursor.getColumnIndex("type")));
                    beacons.add(ibeacon);
                    Log.d("savebeacon",String.valueOf(beacons.size()));
                    checkBeaconSet.add(ibeacon.getBluetoothAddress());
                }
            }
        } catch (SQLException e) {
        } finally {
            //db.close();
        }
    }
}
