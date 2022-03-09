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
            magxText, magyText, magzText, AzimuthText, PitchText, RollText;

    public long time;

    private final float[] LastAccReading = new float[3];
    private final float[] LastMagReading = new float[3];
    private final float[] LastGyroReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] inclinationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

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
        AzimuthText = findViewById(R.id.Azimuth);
        PitchText = findViewById(R.id.Pitch);
        RollText = findViewById(R.id.Roll);

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

                    String AccX = getString(R.string.AccelerometerX,LastAccReading[0]);
                    String AccY = getString(R.string.AccelerometerY,LastAccReading[1]);
                    String AccZ = getString(R.string.AccelerometerZ,LastAccReading[2]);
                    accxText.setText(AccX);
                    accyText.setText(AccY);
                    acczText.setText(AccZ);

                    String MagX = getString(R.string.Magnetic_FieldX,LastMagReading[0]);
                    String MagY = getString(R.string.Magnetic_FieldY,LastMagReading[1]);
                    String MagZ = getString(R.string.Magnetic_FieldZ,LastMagReading[2]);
                    magxText.setText(MagX);
                    magyText.setText(MagY);
                    magzText.setText(MagZ);

                    String GyroX = getString(R.string.GyroscopeX,LastGyroReading[0]);
                    String GyroY = getString(R.string.GyroscopeY,LastGyroReading[1]);
                    String GyroZ = getString(R.string.GyroscopeZ,LastGyroReading[2]);
                    gyroxText.setText(GyroX);
                    gyroyText.setText(GyroY);
                    gyrozText.setText(GyroZ);

                    String Azimuth = getString(R.string.Azimuth,orientationAngles[0]);
                    String Pitch = getString(R.string.Pitch,orientationAngles[1]);
                    String Roll = getString(R.string.Roll,orientationAngles[2]);
                    AzimuthText.setText(Azimuth);
                    PitchText.setText(Pitch);
                    RollText.setText(Roll);
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
                        .add("accx", String.valueOf(LastAccReading[0]))
                        .add("accy", String.valueOf(LastAccReading[1]))
                        .add("accz", String.valueOf(LastAccReading[2]))
                        .add("gyrox", String.valueOf(LastGyroReading[0]))
                        .add("gyroy", String.valueOf(LastGyroReading[1]))
                        .add("gyroz", String.valueOf(LastGyroReading[2]))
                        .add("magx", String.valueOf(LastMagReading[0]))
                        .add("magy", String.valueOf(LastMagReading[1]))
                        .add("magz", String.valueOf(LastMagReading[2]))
                        .add("orientation", orientationAngles[0] + " " +
                                orientationAngles[1] + " " + orientationAngles[2])
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
            }
        });
        thread.start();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        final float alpha = 0.97f;
        //in milliseconds since last boot
        time = SystemClock.elapsedRealtime();

        switch (sensorEvent.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                //System.arraycopy(sensorEvent.values,0,LastAccReading,0,sensorEvent.values.length);
                LastAccReading[0] = alpha * LastAccReading[0] + (1-alpha) * sensorEvent.values[0];
                LastAccReading[1] = alpha * LastAccReading[1] + (1-alpha) * sensorEvent.values[1];
                LastAccReading[2] = alpha * LastAccReading[2] + (1-alpha) * sensorEvent.values[2];
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                //System.arraycopy(sensorEvent.values,0,LastMagReading,0,sensorEvent.values.length);
                LastMagReading[0] = alpha * LastMagReading[0] + (1-alpha) * sensorEvent.values[0];
                LastMagReading[1] = alpha * LastMagReading[1] + (1-alpha) * sensorEvent.values[1];
                LastMagReading[2] = alpha * LastMagReading[2] + (1-alpha) * sensorEvent.values[2];
                break;

            case Sensor.TYPE_GYROSCOPE:
                LastGyroReading[0] = sensorEvent.values[0];
                LastGyroReading[1] = sensorEvent.values[1];
                LastGyroReading[2] = sensorEvent.values[2];
        }

        // Rotation matrix based on current readings from accelerometer and magnetometer.
        SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix,
                LastAccReading, LastMagReading);
        // Express the updated rotation matrix as three orientation angles.
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        //Log.i("OrientationTestActivity",String.format("Orientation: %f, %f, %f", orientationAngles[0], orientationAngles[1], orientationAngles[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        switch (i) {
            case -1:
                Log.d(TAG,"No Contact");
                break;
            case 0:
                Log.d(TAG,"Unreliable");
                break;
            case 1:
                Log.d(TAG,"Low Accuracy");
                break;
            case 2:
                Log.d(TAG,"Medium Accuracy");
                break;
            case 3:
                Log.d(TAG,"High Accuracy");
        }
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