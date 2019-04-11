package com.anirudh.falldetect;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SendMessage extends IntentService {
    URL loc ;
    public SendMessage() {
        super("SendMessage");
    }




    @Override
    protected void onHandleIntent(Intent intent) {
            sendSMS() ;
        }
    private void sendSMS(){

        try {
           File contact  = new File(getFilesDir(),"contact.txt" ) ;
           if(contact.exists()) {
                BufferedReader getInfo = new BufferedReader(new FileReader(contact)) ;
                String phone1 = getInfo.readLine() ;
                String phone2 = getInfo.readLine() ;
                String name = getInfo.readLine() ;
                getInfo.close();
               LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

               LocationListener locationListener = new LocationListener() {
                   @Override
                   public void onLocationChanged(Location location) {
                        try{
                             loc = new URL("http://maps.google.com/?q="+location.getLatitude()+","+location.getLongitude()) ;

                        }
                        catch (Exception e) {

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
               } ;

               try {
                   lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 10, locationListener);
               } catch (SecurityException e) {}
               final String msg= name + " might have suffered a serious fall.\n" +
                       "You are recieving this message because you are their designated contact. Please check in on "+ name +
                       "\n who is currently at- \n"+ loc.toString();
               Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phone1));
               intent.putExtra("sms_body", msg);
               startActivity(intent);
               Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phone2));
               intent2.putExtra("sms_body", msg);
               startActivity(intent2);
           }
        }
        catch (Exception e) {

        }
    }


    }



