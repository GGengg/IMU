package com.example.imu;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    Boolean Running = true;

    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    private final HashMap<String,Sensor> sensors = new HashMap<>();

    private TextView accxText, accyText, acczText, gyroxText, gyroyText, gyrozText,
            magxText, magyText, magzText, Timestamp;

    public float accx,accy,accz,gyrox,gyroy,gyroz,magx,magy,magz;
    public long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accxText = findViewById(R.id.accx);
        accyText = findViewById(R.id.accy);
        acczText = findViewById(R.id.accz);
        gyroxText = findViewById(R.id.gyrox);
        gyroyText = findViewById(R.id.gyroy);
        gyrozText = findViewById(R.id.gyroz);
        magxText = findViewById(R.id.magx);
        magyText = findViewById(R.id.magy);
        magzText = findViewById(R.id.magz);
        Timestamp = findViewById(R.id.time);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensors.put("Accelerometer",sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensors.put("Gyroscope",sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensors.put("Magnetic",sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        registerSensors();
    }

    public void registerSensors(){
        for (Sensor eachSensor:sensors.values()){
            sensorManager.registerListener(this,
                    eachSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    public void unregisterSensors(){
        for (Sensor eachSensor:sensors.values()){
            sensorManager.unregisterListener(this,eachSensor);
        }
    }

    public void onClickLogData(View view){
        Log.d(TAG,"onClickLogData");
        final OkHttpClient client = new OkHttpClient();

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (Running) {
                    handler.postDelayed(this, 20);
                    String url = "http://192.168.86.43:5000/server";
                    Log.d(TAG, String.valueOf(time));
                    RequestBody body = new FormBody.Builder()
                            .add("Timestamp", String.valueOf(time))
                            .add("accx", String.valueOf(accx))
                            .add("accy", String.valueOf(accy))
                            .add("accz", String.valueOf(accz))
                            .add("gyrox", String.valueOf(gyrox))
                            .add("gyroy", String.valueOf(gyroy))
                            .add("gyroz", String.valueOf(gyroz))
                            .add("magx", String.valueOf(magx))
                            .add("magy", String.valueOf(magy))
                            .add("magz", String.valueOf(magz))
                            .build();

                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();

                    final Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.i("onFailure", e.getMessage());
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response)
                                throws IOException {
                            assert response.body() != null;
                            String result = response.body().string();
                            Log.i("result", result);
                        }
                    });
                } else {
                    handler.removeCallbacks(this);
                }
                }
        };
        handler.postDelayed(runnable, 1000);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        time = SystemClock.elapsedRealtime();
        Timestamp.setText(String.valueOf(time));

        //in milliseconds since last boot
        //Log.d(TAG, String.valueOf(SystemClock.elapsedRealtime()));

        switch (sensorEvent.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                accx = sensorEvent.values[0];
                accy = sensorEvent.values[1];
                accz = sensorEvent.values[2];
                String AccX = this.getString(R.string.AccelerometerX,accx);
                String AccY = this.getString(R.string.AccelerometerY,accy);
                String AccZ = this.getString(R.string.AccelerometerZ,accz);
                accxText.setText(AccX);
                accyText.setText(AccY);
                acczText.setText(AccZ);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                magx = sensorEvent.values[0];
                magy = sensorEvent.values[1];
                magz = sensorEvent.values[2];
                String MagX = this.getString(R.string.Magnetic_FieldX,magx);
                String MagY = this.getString(R.string.Magnetic_FieldY,magy);
                String MagZ = this.getString(R.string.Magnetic_FieldZ,magz);
                magxText.setText(MagX);
                magyText.setText(MagY);
                magzText.setText(MagZ);
                break;

            case Sensor.TYPE_GYROSCOPE:
                gyrox = sensorEvent.values[0];
                gyroy = sensorEvent.values[1];
                gyroz = sensorEvent.values[2];
                String GyroX = this.getString(R.string.GyroscopeX,gyrox);
                String GyroY = this.getString(R.string.GyroscopeY,gyroy);
                String GyroZ = this.getString(R.string.GyroscopeZ,gyroz);
                gyroxText.setText(GyroX);
                gyroyText.setText(GyroY);
                gyrozText.setText(GyroZ);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume() MainActivity");
        super.onResume();
        registerSensors();
        Running = true;
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() MainActivity");
        super.onStop();
        unregisterSensors();
        Running = false;
    }
}