package com.anirudh.falldetect;
import android.app.IntentService;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class Raise_Alarm extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    final int timeToPass = 120 ;
     static final String RAISE_ALARM = "com.anirudh.mercury.action.ALARM";
     static final String STOP = "com.anirudh.mercury.action.WARN";
    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    long timePassed ;
    Handler handler = new Handler() ;
    Handler handler1 = new Handler() ;
    private static boolean pressed = false;
    static boolean called ;
    public Raise_Alarm() {
        super("Raise_Alarm");
        pressed = false;
        called= false ;
    }


    public boolean isCalled() {
        return called;
    }

    public void setCalled(boolean call) {
        called = call;
    }

    public boolean isPressed() {
        return pressed;

    }

    public void setPressed(boolean press) {
         pressed = press;
    }
     @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
         if (STOP.equals(intent.getAction())) {
             setPressed(true);
             restart_detect.start();
             onDestroy();
             stopSelf();
         }
         else {
             super.onStartCommand(intent, flags, startId);
         }
        return START_STICKY;
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (RAISE_ALARM.equals(action)) {
                setPressed(false);
                setCalled(false);
                timePassed = System.currentTimeMillis();
                run();
            }
        }
    }


    HandlerThread restart_detect = new HandlerThread("Restart_Detection", Process.THREAD_PRIORITY_FOREGROUND) {
        @Override
        public void run() {
            Intent intent = new Intent(getApplicationContext(), Detect.class) ;
            startService(intent) ;
        }
    } ;
        public void run() {
            while(!isPressed()) {
                if (!ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }

                boolean tr = !isCalled() && (((System.currentTimeMillis() - timePassed) / 1000) > timeToPass);


                if (tr) {
                    setCalled(true);
                    System.out.println("CALLING");
                    sendMessage.start();
                }
                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.ADJUST_UNMUTE);
                toneG.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT);

            }
        }


HandlerThread sendMessage = new HandlerThread("SENDING_ALERT_MESSAGE", Process.THREAD_PRIORITY_FOREGROUND) {
    @Override
    public void run() {
        startService(new Intent(getApplicationContext(),SendMessage.class)) ;
    }
} ;


}
