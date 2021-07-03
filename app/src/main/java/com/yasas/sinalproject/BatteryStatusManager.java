package com.yasas.sinalproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import androidx.annotation.NonNull;

public class BatteryStatusManager {
    private static String KEY_LAST_CHARGING_STATUS = "0";
    private static String KEY_CHARGING_CIRCLE_START_TIME = "1";
    private static String KEY_CHARGING_CIRCLE_START_LEVEL = "2";

    private Context context;
    private float currentBatteryLevel;
    private FirebaseDatabase database;
    private float predictedBatteryDropForHour;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private MainActivity activity;
    private HashMap<String, String> DROP_RATES_FOR_EACH_HOUR = new HashMap<String, String>();


    public BatteryStatusManager(Context context) {
        this.context = context;
        this.database = FirebaseDatabase.getInstance();
        activity = (MainActivity)context;
        this.sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        this.editor = sharedPref.edit();
        setCurrentBatteryLevel();
        getPredictedBatteryDropForHour();
    }

    private void setCurrentBatteryLevel(){
        BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                currentBatteryLevel = level * 100 / (float)scale;
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


        context.registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public float getCurrentBatteryLevel() {
        return currentBatteryLevel;
    }

    private void getPredictedBatteryDropForHour(){
        database.getReference("Drop_rate").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {

                    for (int i = 0; i < 24; i++) {
                        String hr = i<10? "0"+i:String.valueOf(i);
                        DROP_RATES_FOR_EACH_HOUR.put(hr,task.getResult().child(hr).getValue().toString());
                    }
                }
            }
        });
    }

    private float getDropRateByTime(long time){
        Date dt = new Date();
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat("kk");
        return Float.parseFloat(DROP_RATES_FOR_EACH_HOUR.get(dateFormat.format(time)));
    }

    private float getPredictedBatteryLevelForTime(long c_time,long l_time,float l_charge){
        if(c_time == l_time){
            return 0;
        }else{
            int c_hr = getHourFromTimeStamp(c_time);
            int l_hr = getHourFromTimeStamp(l_time);
            if(c_hr == l_hr){
                return (l_charge - (getDropRateByTime(c_time)*((float) (c_time-l_time)/60000)/60));
            }else{
                while (l_hr != c_hr){
                    l_charge = l_charge - (getDropRateByTime(l_time)*(60 - getMinsFromTimeStamp(l_time))/60);
                    l_hr++;
                    l_time += (60 - getMinsFromTimeStamp(l_time))*60000;
                }
                return (l_charge - (getDropRateByTime(c_time)*((float)(c_time-l_time)/60)/60));
            }
        }
    }

    private void putValue(String key,String value){
        editor.putString(key, value);
        editor.apply();
    }

    private String getValue(String key){
        return sharedPref.getString(key, null);
    }
    private int getHourFromTimeStamp(long timeStamp){
        Date date = new Date(timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat("kk");
        sdf.setTimeZone(TimeZone.getDefault());
        String formattedDate = sdf.format(date);
        return Integer.parseInt(formattedDate);
    }

    private int getMinsFromTimeStamp(long timeStamp){
        Date date = new Date(timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        sdf.setTimeZone(TimeZone.getDefault());
        String formattedDate = sdf.format(date);
        return Integer.parseInt(formattedDate);
    }
}
