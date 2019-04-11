package com.anirudh.falldetect;
import android.app.IntentService;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class Raise_Alarm extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
     static final String RAISE_ALARM = "com.anirudh.mercury.action.ALARM";
     static final String STOP = "com.anirudh.mercury.action.WARN";
    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    long timePassed ;
    private static boolean pressed = false;
    boolean called ;
    Handler handler = new Handler() ;
    public Raise_Alarm() {
        super("Raise_Alarm");
        pressed = false;
        called= false ;
    }

    public boolean isCalled() {
        return called;
    }

    public void setCalled(boolean called) {
        this.called = called;
    }

    public boolean isPressed() {
        return pressed;

    }

    public void setPressed(boolean press) {
         pressed = press;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (RAISE_ALARM.equals(action)) {
                setPressed(false);
                timePassed = System.currentTimeMillis();
                handler.post(alarm);
            } else if (STOP.equals(action)) {
                setPressed(true);
                stopSelf();
                handler.post(restart_detect);
            }
        }
    }
    Runnable restart_detect = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(getApplicationContext(),Detect.class) ;
            startService(intent) ;
        }
    } ;
    final Runnable alarm = new Runnable() {
        @Override
        public void run() {
            if (!ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
            if(!isCalled()) {
                hasTimePassed();
            }
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE,AudioManager.ADJUST_UNMUTE);
            toneG.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT) ;
            if(!isPressed()) {
                handler.post(this);
            }
        }
    } ;

public void hasTimePassed(){

    long passed = (System.currentTimeMillis()-timePassed)/1000 ;
    if(passed>4500) {
        startService(new Intent(getApplicationContext(),SendMessage.class)) ;
        setCalled(true);
    }

}


}
