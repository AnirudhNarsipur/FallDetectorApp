package com.anirudh.falldetect;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.InputStream;

public class Detect extends Service implements SensorEventListener {
    final int MAX_SIZE = 90 ;
    final int timeStep = 70 ;
    float threshold  = 0.5f ;
    final int forward_look = 25;
    final float further = 0.4f ;
    long time_passed = System.currentTimeMillis();

    private CircularFifo<float[]> acc_values = new CircularFifo<>(MAX_SIZE) ;
    private CircularFifo<float[]>gyro_values = new CircularFifo<>(MAX_SIZE);

    float acc_current  = SensorManager.GRAVITY_EARTH;
    float acc_previous  = SensorManager.GRAVITY_EARTH;

    SensorManager manager ;
    Sensor acc_sensor ;
    Sensor gyro_sensor ;
    Sensor significant_motion ;
    Intent stop  ;

    Handler handler = new Handler();
    ComputationGraph model ;
    public boolean isTimepassed(long t){
        return ((t- time_passed)>4000) ;
    }

    public void setTime_passed(long time_passed) {
        this.time_passed = time_passed;
    }


    Runnable detecting = new Runnable() {
        @Override
        public void run() {
            if(model==null) {
                handler.post(setup);
            }
           //  acc_values.setLocked(true);
            //gyro_values.setLocked(true);
            setTime_passed(System.currentTimeMillis());
            model.rnnClearPreviousState();
            float outputvalue = (model.output(get_vals())[0]).getFloat(0) ;
            if(outputvalue>=threshold) {
                startService(stop);

            } else if(outputvalue>further){
                handler.postDelayed(timesteps,1500) ;
            }
            model.rnnClearPreviousState();
            // acc_values.setLocked(false);
             //gyro_values.setLocked(false);


        }
    };
    public void reset(){
       acc_values = new CircularFifo<>(MAX_SIZE) ;
       gyro_values = new CircularFifo<>(MAX_SIZE);
       onDestroy();
    }

    Runnable timesteps = new Runnable() {
        @Override
        public void run() {
            for(int i = 0 ; i <forward_look; i++) {
                float outputvalue = (model.rnnTimeStep(getSingleVal()))[0].getFloat(0);
                if (outputvalue > threshold) {
                    reset();
                    startService(stop);
                    break;
                }
            }
        }
    } ;

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
    @Override
    public void onCreate(){
        onStartCommand(stop,START_FLAG_REDELIVERY,1);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Pulls data for analysis
        manager = (SensorManager) getSystemService(SENSOR_SERVICE) ;
        significant_motion = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        acc_sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro_sensor = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) ;

        manager.registerListener(Detect.this,acc_sensor,50000);
        manager.registerListener(Detect.this,gyro_sensor,50000) ;

        handler.postDelayed(setup,2000);
        stop = new Intent(this,Raise_Alarm.class) ;
        stop.setAction(Raise_Alarm.RAISE_ALARM) ;
        return START_STICKY;

    }

    SensorEventListener sigmotion_detect = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
                acc_current = acc_previous ;
                acc_current = ((float) Math.sqrt( (event.values[0] * event.values[0]) + (event.values[1] * event.values[1]) + (event.values[2] * event.values[2]) ));

                float delta  =acc_current-acc_previous ;

            if(delta > 2.00 &&isTimepassed(System.currentTimeMillis())){

                handler.post(detecting) ;

            }



        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {


        }
    } ;




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
    Runnable setup= new Runnable() {
        @Override
        public void run() {
            try{


                InputStream is = getResources().openRawResource(R.raw.mostly);
                model = ModelSerializer.restoreComputationGraph(is,false) ;
                model.init();
                manager.registerListener(sigmotion_detect,significant_motion,200000);

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    } ;
    private INDArray get_vals(){
        INDArray ind  ;
        float[][][] result = new float[1][6][timeStep];

            for (int i = 0; i < timeStep; i++) {
                float[] acc = acc_values.removeFirst();
                float[] gyro = gyro_values.removeFirst();
                result[0][0][i] = gyro[0];
                result[0][1][i] = gyro[1];
                result[0][2][i] = gyro[2];
                result[0][3][i] = acc[0];
                result[0][4][i] = acc[1];
                result[0][5][i] = acc[2];

            }

        ind = Nd4j.create(result);
        return ind ;
    }

    private INDArray getSingleVal(){
        INDArray ind  ;
        float[][][] result = new float[1][6][1];
        int i = 0;
        float[] acc = acc_values.removeFirst();
        float[] gyro = gyro_values.removeFirst();
        result[0][0][i] = gyro[0];
        result[0][1][i] = gyro[1];
        result[0][2][i] = gyro[2];
        result[0][3][i] = acc[0];
        result[0][4][i] = acc[1];
        result[0][5][i] = acc[2];
        ind = Nd4j.create(result);
        return ind ;
    }
    @Override
    public void onAccuracyChanged(Sensor s, int i) {

    }

}