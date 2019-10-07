package com.anirudh.falldetect;


import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;


import org.tensorflow.lite.Interpreter;


import java.io.FileInputStream;

import java.io.IOException;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import java.util.NoSuchElementException;


public class Detect extends Service implements SensorEventListener {

    final int MAX_SIZE = 90;
    final int timeStep = 70;
    float threshold = 0.5f;

    long time_passed = System.currentTimeMillis();

    Interpreter tf_interpreter ;

    private CircularFifo<float[]> acc_values = new CircularFifo<>(MAX_SIZE);
    private CircularFifo<float[]> gyro_values = new CircularFifo<>(MAX_SIZE);

    float acc_current = SensorManager.GRAVITY_EARTH;
    float acc_previous = SensorManager.GRAVITY_EARTH;

    SensorManager manager;
    Sensor acc_sensor;
    Sensor gyro_sensor;
    Sensor significant_motion;

    Runnable warn = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(getApplicationContext(), Raise_Alarm.class);
            intent.setAction(Raise_Alarm.RAISE_ALARM) ;
            startService(intent);
        }
    };
    Handler handler = new Handler();


    public boolean isTimepassed(long t) {
        return ((t - time_passed) > 4000);
    }

    public void setTime_passed(long time_passed) {
        this.time_passed = time_passed;
    }


    HandlerThread detecting = new HandlerThread("Service_Proccess", Process.THREAD_PRIORITY_FOREGROUND) {
        @Override
        public void run() {
            if (tf_interpreter == null) {
                setup() ;
            }
            try{
            setTime_passed(System.currentTimeMillis());

            float[][] outputval = new float[1][1];
            float[][][] test = new float[1][70][6];
            tf_interpreter.run(get_vals(),outputval);

            float outputvalue = outputval[0][0] ;
            if (outputvalue >= threshold) {
                System.out.println("Value is " + outputvalue);
                handler.post(warn);
                acc_values = new CircularFifo<>(MAX_SIZE);
                gyro_values = new CircularFifo<>(MAX_SIZE);
                onDestroy();
                stopSelf();
            }

            }catch (Exception e ){ e.printStackTrace();}
            /* else if(outputvalue>further ){
                handler.postDelayed(timesteps,1500) ;
            }*/
        }
    };

    public void reset() {
        acc_values = new CircularFifo<>(MAX_SIZE);
        gyro_values = new CircularFifo<>(MAX_SIZE);
        stopSelf();
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Pulls data for analysis
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        significant_motion = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        acc_sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro_sensor = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        manager.registerListener(Detect.this, acc_sensor, 50000);
        manager.registerListener(Detect.this, gyro_sensor, 50000);

        setup() ;

        return START_STICKY;

    }

    SensorEventListener sigmotion_detect = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            acc_current = acc_previous;
            acc_current = ((float) Math.sqrt((event.values[0] * event.values[0]) + (event.values[1] * event.values[1]) + (event.values[2] * event.values[2])));

            float delta = acc_current - acc_previous;

            if (delta > 2.00 && isTimepassed(System.currentTimeMillis())) {

               detecting.start();

            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensor_type = event.sensor.getType();
        switch (sensor_type) {
            //accelerometer
            case 1:
                acc_values.addElement(event.values);
                break;
            //gyrometer
            case 4:
                gyro_values.addElement(event.values);
                break;
            default:
                break;
        }

    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("tripura1.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public boolean setup()
    {
        try {

            tf_interpreter = new Interpreter(loadModelFile()) ;
            manager.registerListener(sigmotion_detect, significant_motion, 200000);

        } catch (Exception e) {
            e.printStackTrace();
            return false ;
        }
        return true ;
    }


    private float[][][] get_vals(){

        float[][][] result = new float[1][timeStep][6];

            for (int i = 0; i < timeStep; i++) {
                try {
                    float[] acc = acc_values.removeFirst();
                    float[] gyro = gyro_values.removeFirst();
                    result[0][i][0] = gyro[0];
                    result[0][i][1] = gyro[1];
                    result[0][i][2] = gyro[2];
                    result[0][i][3] = acc[0];
                    result[0][i][4] = acc[1];
                    result[0][i][5] = acc[2];

                } catch (NoSuchElementException no){
                    System.out.println("ERROR");
                    return  result;
                }

            }

        return result ;
    }

    private float[][][] getSingleVal(){

        float[][][] result = new float[1][1][6];

        try {
            float[] acc = acc_values.removeFirst();
            float[] gyro = gyro_values.removeFirst();
            result[0][0][0] = gyro[0];
            result[0][0][1] = gyro[1];
            result[0][0][2] = gyro[2];
            result[0][0][3] = acc[0];
            result[0][0][4] = acc[1];
            result[0][0][5] = acc[2];

        } catch (NoSuchElementException no){
            System.out.println("ERROR");
            return result ;
        }
        return  result ;

    }
    @Override
    public void onAccuracyChanged(Sensor s, int i) {}

}