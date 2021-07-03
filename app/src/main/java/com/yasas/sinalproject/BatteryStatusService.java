package com.yasas.sinalproject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;

public class BatteryStatusService extends Service {

    private static String KEY_LAST_CHARGING_STATUS = "0";
    private static String KEY_CHARGING_CIRCLE_START_TIME = "1";
    private static String KEY_CHARGING_CIRCLE_START_LEVEL = "2";

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    public BatteryStatusService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(BatteryStatusService.this);
        editor = sharedPref.edit();
        setCurrentBatteryLevel();
        return START_STICKY;
    }


    private void setCurrentBatteryLevel(){
        BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float currentBatteryLevel = level * 100 / (float)scale;
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;
                if((getValue(KEY_LAST_CHARGING_STATUS)==null || "true".equalsIgnoreCase(getValue(KEY_LAST_CHARGING_STATUS))) && "false".equalsIgnoreCase(String.valueOf(isCharging))){
                    Date date = new Date();
                    putValue(KEY_CHARGING_CIRCLE_START_TIME, String.valueOf(date.getTime()));
                    putValue(KEY_CHARGING_CIRCLE_START_LEVEL, String.valueOf(currentBatteryLevel));

                }

                putValue(KEY_LAST_CHARGING_STATUS, String.valueOf(isCharging));
            }

        };


        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void putValue(String key,String value){
        editor.putString(key, value);
        editor.apply();
    }

    private String getValue(String key){
        return sharedPref.getString(key, null);
    }
}