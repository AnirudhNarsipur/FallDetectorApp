package com.anirudh.falldetect;
import android.app.IntentService;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ProcessLifecycleOwner;
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
    private static boolean pressed = false;
    Handler handler = new Handler() ;
    public Raise_Alarm() {
        super("Raise_Alarm");
        pressed = false;
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
                handler.post(alarm);
            } else if (STOP.equals(action)) {
                System.out.println("STOPPING");
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
            toneG.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT) ;
            if(!isPressed()) {
                handler.post(this);
            }
        }
    } ;

    private void handleActionWarn() {

        while (!isPressed())
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);

    }


}
