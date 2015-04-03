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
import com.lef.scanner.IBeacon;
import com.p.DrawMap.Utils.DataUtil;
import com.p.DrawMap.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Filter;

/**
 * Created by p on 2015/3/3.
 */
public class PublicData extends Application {
    private static PublicData self;
    public ArrayList<IBeacon> beacons = new ArrayList<IBeacon>();
    public HashSet<String> checkBeaconSet = new HashSet<String>();
    public HashSet<String> uploadBeaconSet = new HashSet<String>();
    public ArrayList<BeaconFilter> beaconFilters = new ArrayList<BeaconFilter>();
    public DataUtil du;
    private String ip;
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
        for(BeaconFilter filter:beaconFilters){
            if (filter.major.contains(String.valueOf(ibeancon.getMajor())) && filter.uuid.contains(ibeancon.getProximityUuid()))
                return true;
        }
        return false;
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
        du = new DataUtil(this, this.getString(R.string.unupload_dbname), null, 1);
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
        byte[] data_byte = null;
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
        } else {
            //如果仅仅是用来判断网络连接
            //则可以使用 cm.getActiveNetworkInfo().isAvailable();
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
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
    public void getFiltersInDb(){
        SQLiteDatabase db = du.getReadableDatabase();
        String sql;
        Cursor cursor = null;
        // area text,type text,time text,val text
        sql = "select * from filter";
        try {
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {//直到返回false说明表中到了数据末尾
                    BeaconFilter filter = new BeaconFilter();
                    //Log.d("savebeacon",cursor.getString(cursor.getColumnIndex("mac_id")));
                    filter.uuid = cursor.getString(cursor.getColumnIndex("uuid"));
                    filter.major = cursor.getString(cursor.getColumnIndex("major"));
                    beaconFilters.add(filter);
                }
            }
        } catch (SQLException e) {
        } finally {
            db.close();
        }
    }
    public boolean saveFilter2Db(BeaconFilter filter){
        boolean result = true;
        SQLiteDatabase db = du.getReadableDatabase();
        String sql;
        // area text,type text,time text,val text

        sql = "insert into filter(uuid,major) values('";
        sql += filter.uuid + "','" + filter.major +"')";
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        } finally {
            db.close();
        }
        Log.d("save beacon",String.valueOf(result));
        return result;
    }
    public void removeFilterFromDb(BeaconFilter filter){
        SQLiteDatabase db = du.getReadableDatabase();

        String sql = "delete from filter where  uuid = '"+filter.uuid+"' and major = '"+filter.major+"'";
        try {
            db.execSQL(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
    public boolean saveCheckBeacon2Db(IBeacon iBeacon) {
        boolean result = true;
        SQLiteDatabase db = du.getReadableDatabase();
        String sql;
        // area text,type text,time text,val text

        sql = "insert into unupbeacon(mac_id,uuid,rssi,major,minor) values('";
        sql += iBeacon.getBluetoothAddress() + "','" + iBeacon.getProximityUuid()
                + "','" + iBeacon.getRssi()
                + "','" + iBeacon.getMajor() + "','" + iBeacon.getMinor()
                +"')";
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        } finally {
            db.close();
        }
        Log.d("save beacon",String.valueOf(result));
        return result;

    }
    public void removeCheckedBeaconInDb(){
        SQLiteDatabase db = du.getReadableDatabase();
        String sql = "delete from unupbeacon";
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
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
            db.close();
        }
    }
    public void getCheckedBeaconInDb() {
        SQLiteDatabase db = du.getReadableDatabase();
        String sql;
        Cursor cursor = null;
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

                    beacons.add(ibeacon);
                    Log.d("savebeacon",String.valueOf(beacons.size()));
                    checkBeaconSet.add(ibeacon.getBluetoothAddress());
                }
            }
        } catch (SQLException e) {
        } finally {
            db.close();
        }
    }
}
