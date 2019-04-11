package com.anirudh.falldetect;
import android.Manifest;
import android.content.Intent ;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

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
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.SEND_SMS}, 1);


        final ConstraintLayout contactpage = findViewById(R.id.contactPage) ;
        final Button stop = findViewById(R.id.alarm_button) ;
        final ImageView settings_button  = findViewById(R.id.settings_button);
        final ConstraintLayout settings_page = findViewById(R.id.settings_page) ;
        final FrameLayout about_description = findViewById(R.id.aboutPage) ;
        final Button setContact = findViewById(R.id.setContact);
        final ImageButton closeContact = findViewById(R.id.close_contact) ;
        final Button submit = findViewById(R.id.submitContact) ;
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stop = new Intent(getApplicationContext(),Raise_Alarm.class) ;
                stop.setAction(Raise_Alarm.STOP) ;
                startService(stop) ;
            }
        });

        settings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about_description.setVisibility(View.GONE);
                settings_page.setVisibility(View.VISIBLE);
                contactpage.setVisibility(View.GONE);
                settings_button.setVisibility(View.GONE);
                stop.setVisibility(View.VISIBLE);

            }
        });
        ImageButton close = findViewById(R.id.Close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about_description.setVisibility(View.GONE);
                settings_page.setVisibility(View.GONE);
                contactpage.setVisibility(View.GONE);
                settings_button.setVisibility(View.VISIBLE);
                stop.setVisibility(View.VISIBLE);
            }
        });

        Button about = findViewById(R.id.about_button) ;
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about_description.setVisibility(View.VISIBLE);
                settings_page.setVisibility(View.GONE);
                contactpage.setVisibility(View.GONE);
                settings_button.setVisibility(View.GONE);
                stop.setVisibility(View.GONE);

            }
        });
        ImageButton close_about = findViewById(R.id.close_about) ;
        close_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about_description.setVisibility(View.GONE);
                settings_page.setVisibility(View.GONE);
                contactpage.setVisibility(View.GONE);
                settings_button.setVisibility(View.VISIBLE);
                stop.setVisibility(View.VISIBLE);
            }
        });

        setContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about_description.setVisibility(View.GONE);
                settings_page.setVisibility(View.GONE);
                contactpage.setVisibility(View.VISIBLE);
                settings_button.setVisibility(View.GONE);
                stop.setVisibility(View.GONE);
            }
        });

        closeContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about_description.setVisibility(View.GONE);
                settings_page.setVisibility(View.GONE);
                contactpage.setVisibility(View.GONE);
                settings_button.setVisibility(View.VISIBLE);
                stop.setVisibility(View.VISIBLE);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    findViewById(R.id.Invalid).setVisibility(View.GONE);
                    File contact = new File(getFilesDir(), "contact.txt");
                    contact.createNewFile() ;
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(contact));
                    String phone1 = ((EditText) findViewById(R.id.phone1)).getText().toString();
                    String phone2 = ((EditText) findViewById(R.id.phone2)).getText().toString();
                    String name = ((EditText) findViewById(R.id.Name)).getText().toString();
                    if(!Patterns.PHONE.matcher(phone1).matches() || !Patterns.PHONE.matcher(phone2).matches()) {
                        final TextView invalid = findViewById(R.id.Invalid) ;
                        invalid.setVisibility(View.VISIBLE);
                        invalid.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                invalid.setVisibility(View.GONE) ;
                            }
                        },3000) ;
                    } else{
                        final TextView success = findViewById(R.id.Success) ;
                        success.setVisibility(View.VISIBLE);
                        success.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                success.setVisibility(View.GONE) ;
                            }
                        },3000) ;
                        bufferedWriter.write(phone1 + "\n");
                        bufferedWriter.write(phone2 + "\n");
                        bufferedWriter.write(name + "\n");
                        bufferedWriter.flush();

                    }
                    bufferedWriter.close();
                } catch (Exception e) { }

            }
        });


    }

}
