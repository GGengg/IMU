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
import android.view.View;
import android.widget.TextView;
import android.util.Log;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    private TextView accxText;
    private TextView accyText;
    private TextView acczText;
    private TextView gyroxText;
    private TextView gyroyText;
    private TextView gyrozText;
    private TextView magxText;
    private TextView magyText;
    private TextView magzText;
    private TextView Timestamp;

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
        Sensor sensora = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensorg = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor sensorm = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, sensora, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorg, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorm, SensorManager.SENSOR_DELAY_GAME);
    }

    public void onClickLogData(View view){
        Log.d(TAG,"onClickLogData");
        final OkHttpClient client = new OkHttpClient();
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 20);
                //接口参数 String username,String password
                String url = "http://192.168.86.34:5000/server";
                //第一步创建OKHttpClient

                //第二步创建RequestBody（Form表达）
                RequestBody body = new FormBody.Builder()
                        .add("accx", (String) ((TextView) findViewById(R.id.time)).getText())
                        .add("accx", (String) ((TextView) findViewById(R.id.accx)).getText())
                        .add("accy", (String) ((TextView) findViewById(R.id.accy)).getText())
                        .add("accz", (String) ((TextView) findViewById(R.id.accz)).getText())
                        .add("gyrox", (String) ((TextView) findViewById(R.id.gyrox)).getText())
                        .add("gyroy", (String) ((TextView) findViewById(R.id.gyroy)).getText())
                        .add("gyroz", (String) ((TextView) findViewById(R.id.gyroz)).getText())
                        .add("magx", (String) ((TextView) findViewById(R.id.magx)).getText())
                        .add("magy", (String) ((TextView) findViewById(R.id.magy)).getText())
                        .add("magz", (String) ((TextView) findViewById(R.id.magz)).getText())
                        .build();
                //第三步创建Rquest
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                
                //第四步创建call回调对象
                final Call call = client.newCall(request);
                //第五步发起请求
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.i("onFailure", e.getMessage());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        assert response.body() != null;
                        String result = response.body().string();
                        Log.i("result", result);
                        //Log.d(TAG, "Status code: "+ response.code());

                    }
                });
            }
        };
        handler.postDelayed(runnable, 1000);
       // handler.postDelayed(runnable, 100);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long time = sensorEvent.timestamp;
        Timestamp.setText(String.valueOf(time));
        switch (sensorEvent.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                String AccX = this.getString(R.string.AccelerometerX,sensorEvent.values[0]);
                String AccY = this.getString(R.string.AccelerometerY,sensorEvent.values[1]);
                String AccZ = this.getString(R.string.AccelerometerZ,sensorEvent.values[2]);
                accxText.setText(AccX);
                accyText.setText(AccY);
                acczText.setText(AccZ);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                String MagX = this.getString(R.string.Magnetic_FieldX,sensorEvent.values[0]);
                String MagY = this.getString(R.string.Magnetic_FieldY,sensorEvent.values[1]);
                String MagZ = this.getString(R.string.Magnetic_FieldZ,sensorEvent.values[2]);
                magxText.setText(MagX);
                magyText.setText(MagY);
                magzText.setText(MagZ);
                break;
            case Sensor.TYPE_GYROSCOPE:
                String GyroX = this.getString(R.string.GyroscopeX,sensorEvent.values[0]);
                String GyroY = this.getString(R.string.GyroscopeY,sensorEvent.values[1]);
                String GyroZ = this.getString(R.string.GyroscopeZ,sensorEvent.values[2]);
                gyroxText.setText(GyroX);
                gyroyText.setText(GyroY);
                gyrozText.setText(GyroZ);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}