package com.stdio.gyroscope;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import static android.util.Half.EPSILON;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class MainActivity extends AppCompatActivity {

    // Создаем константу для перевода наносекунд в секунды
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    private SensorManager sensorManager;
    private Sensor sensor;
    TextView textView;
    float[] gyroscope = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(listenerLight, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }


    SensorEventListener listenerLight = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
// Дельта-вращение этого временного шага, умноженное на текущее вращение
// после вычисления его из данных экземпляра гироскопа.
            if (timestamp != 0) {
                final float dT = (event.timestamp - timestamp) * NS2S;
// Ось вращения экземпляра еще не нормирована.
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];
// Рассчитываем угловую скорость экземпляра.
                float omegaMagnitude = (float) sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
// Нормализовать вектор вращения, если он достаточно большой, чтобы получить ось
// (то есть EPSILON должен представлять ваш максимально допустимый предел погрешности)
                if (omegaMagnitude > EPSILON) {
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                }
                float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                float sinThetaOverTwo = (float) sin(thetaOverTwo);
                float cosThetaOverTwo = (float) cos(thetaOverTwo);
                deltaRotationVector[0] = sinThetaOverTwo * axisX;
                deltaRotationVector[1] = sinThetaOverTwo * axisY;
                deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                deltaRotationVector[3] = cosThetaOverTwo;
            }
            timestamp = event.timestamp;
            float[] deltaRotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(deltaRotationMatrix,
                    deltaRotationVector);
            gyroscope[0] = (float) (Math.round((event.values[0] + deltaRotationVector[0] * 1000)/10)) / 10;
            gyroscope[1] = (float) (Math.round((event.values[1] + deltaRotationVector[1] * 1000)/10)) / 10;
            gyroscope[2] = (float) (Math.round((event.values[2] + deltaRotationVector[2] * 1000)/10)) / 10;
            textView.setText("x:" + gyroscope[0] + " / y:" + gyroscope[1] + " / z:" + gyroscope[2] + " рад/с");
            // Пользовательский код должен объединять дельта-вращение, которое мы вычислили с текущим вращением
        // чтобы получить обновленное вращение
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;
        }
    };
}