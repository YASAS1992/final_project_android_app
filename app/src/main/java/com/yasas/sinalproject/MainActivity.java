package com.yasas.sinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static String KEY_LAST_CHARGING_STATUS = "0";
    private static String KEY_CHARGING_CIRCLE_START_TIME = "1";
    private static String KEY_CHARGING_CIRCLE_START_LEVEL = "2";

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        editor = sharedPref.edit();
        startService(new Intent(MainActivity.this,BatteryStatusService.class));
        textView = findViewById(R.id.textView);
    }


    public void btnClick(View v){

        textView.setText(getValue(KEY_CHARGING_CIRCLE_START_TIME));

    }

    private void putValue(String key,String value){
        editor.putString(key, value);
        editor.apply();
    }

    private String getValue(String key){
        return sharedPref.getString(key, null);
    }
}