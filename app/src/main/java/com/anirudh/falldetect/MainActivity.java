package com.anirudh.falldetect;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler handler  = new Handler() ;
        Runnable s = new Runnable(){
            @Override
            public void run() {

                Intent intent = new Intent(getApplicationContext(),Detect.class) ;
                startService(intent) ;
            }
        } ;
        handler.post(s) ;
        Button stop = findViewById(R.id.alarm_button) ;
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stop = new Intent(getApplicationContext(),Raise_Alarm.class) ;
                stop.setAction(Raise_Alarm.STOP) ;
                startService(stop) ;
            }
        });

    }
}
