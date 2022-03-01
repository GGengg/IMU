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
import android.widget.EditText;
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

    private final float[] LastAccReading = new float[3];
    private final float[] LastMagReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

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
        Update_UI();
    }

    public void Update_UI(){
        Handler Update_UI_Handler = new Handler();
        Runnable Update_UI_Runnable = new Runnable() {
            @Override
            public void run() {
                if (!Running) {
                    Update_UI_Handler.removeCallbacks(this);
                } else {
                    Update_UI_Handler.postDelayed(this,200);

                    String AccX = getString(R.string.AccelerometerX,accx);
                    String AccY = getString(R.string.AccelerometerY,accy);
                    String AccZ = getString(R.string.AccelerometerZ,accz);
                    accxText.setText(AccX);
                    accyText.setText(AccY);
                    acczText.setText(AccZ);

                    String MagX = getString(R.string.Magnetic_FieldX,magx);
                    String MagY = getString(R.string.Magnetic_FieldY,magy);
                    String MagZ = getString(R.string.Magnetic_FieldZ,magz);
                    magxText.setText(MagX);
                    magyText.setText(MagY);
                    magzText.setText(MagZ);

                    String GyroX = getString(R.string.GyroscopeX,gyrox);
                    String GyroY = getString(R.string.GyroscopeY,gyroy);
                    String GyroZ = getString(R.string.GyroscopeZ,gyroz);
                    gyroxText.setText(GyroX);
                    gyroyText.setText(GyroY);
                    gyrozText.setText(GyroZ);
                }
            }
        };
        Update_UI_Handler.postDelayed(Update_UI_Runnable,100);
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

        EditText url_text = findViewById(R.id.editTextNumber);
        String url_bit = url_text.getText().toString();

        String url = "http://192.168.86." + url_bit + ":5000/server";

        //String url = "http://192.168.86.48:5000/server";

        Thread thread = new Thread(() -> {
            while (Running) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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
                        .add("orientation", orientationAngles[0] + " " +
                                orientationAngles[1] + " " + orientationAngles[2])
                        .build();

                Log.d(TAG,orientationAngles[0] +
                        String.valueOf(orientationAngles[1]) +
                        orientationAngles[2]);

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
            }
        });
        thread.start();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //in milliseconds since last boot
        time = SystemClock.elapsedRealtime();
        //Timestamp.setText(String.valueOf(time));

        switch (sensorEvent.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(sensorEvent.values,0,LastAccReading,0,sensorEvent.values.length);
                accx = sensorEvent.values[0];
                accy = sensorEvent.values[1];
                accz = sensorEvent.values[2];
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(sensorEvent.values,0,LastMagReading,0,sensorEvent.values.length);
                magx = sensorEvent.values[0];
                magy = sensorEvent.values[1];
                magz = sensorEvent.values[2];
                break;

            case Sensor.TYPE_GYROSCOPE:
                gyrox = sensorEvent.values[0];
                gyroy = sensorEvent.values[1];
                gyroz = sensorEvent.values[2];
        }

        // Rotation matrix based on current readings from accelerometer and magnetometer.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                LastAccReading, LastMagReading);
        // Express the updated rotation matrix as three orientation angles.
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        //Log.i("OrientationTestActivity",String.format("Orientation: %f, %f, %f", orientationAngles[0],orientationAngles[1],orientationAngles[2]));
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