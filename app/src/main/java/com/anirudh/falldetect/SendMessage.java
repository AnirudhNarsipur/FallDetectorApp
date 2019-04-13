package com.anirudh.falldetect;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SendMessage extends IntentService {
    URL loc ;
    LocationManager lm ;
    public SendMessage() {
        super("SendMessage");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println("SENDING sms");
            sendSMS() ;
        }
    private void sendSMS() {

        try {
            File contact = new File(getFilesDir(), "contact.txt");
            if (contact.exists()) {
                BufferedReader getInfo = new BufferedReader(new FileReader(contact));
                String phone1 = getInfo.readLine();
                String phone2 = getInfo.readLine();
                String name = getInfo.readLine();
                name = name.substring(1).toUpperCase() + name.substring(1) ;
                getInfo.close();
                lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, LocListener);
                TimeUnit.SECONDS.sleep(3);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                final String msg = name + " might have suffered a serious fall.\n" +
                        "You are receiving this message because you are their designated contact. Please check in on " + name ;
                SmsManager smsManager = SmsManager.getDefault();
                if(location!=null) {
                    loc = new URL("http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude());
                    final String msg2 = name + " is at: " + loc ;
                    smsManager.sendTextMessage(phone1, null, msg, null, null);
                    smsManager.sendTextMessage(phone1, null, msg2, null, null);
                    smsManager.sendTextMessage(phone2, null, msg, null, null);
                    smsManager.sendTextMessage(phone2, null, msg2, null, null);
                } else {
                    smsManager.sendTextMessage(phone1, null, msg, null, null);
                    smsManager.sendTextMessage(phone2, null, msg, null, null);

                }

            }
        } catch (SecurityException s) {
            s.printStackTrace();
        } catch (InterruptedException i) {
            i.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        LocationListener LocListener = new LocationListener (){
            @Override
            public void onLocationChanged(Location location) {
                try {
                    loc = new URL("http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude());
                    lm.removeUpdates(LocListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        }
        ;
    }




