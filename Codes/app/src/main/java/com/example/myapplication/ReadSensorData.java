package com.example.myapplication;

import static java.lang.Math.abs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

public class ReadSensorData extends Activity implements SensorEventListener {
    public static final String X_ARRAY = "X_ARRAY";
    public static final String H_ARRAY = "H_ARRAY";

    GraphView graphView;

    private List<Float> xList,hList;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private float x, vx;
    private float lastValuex, lastValuez;
    private float accelerometerTimestamp, gyroscopeTimestamp;
    private float h, theta;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_sensor_data);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        setGraphAttr();
        accelerometerTimestamp = 0;
        gyroscopeTimestamp = 0;
        x = 0;
        lastValuex = 0;
        vx = 0;
        h = 0;
        theta = 0;
        hList = new ArrayList<>();
        xList = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, gyroscope, 3);
        accelerometerTimestamp = 0;
        gyroscopeTimestamp = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @Override
    public final void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            accelerationChange(event);
            sensorManager.registerListener(this, gyroscope, (int) (100 / Math.abs(vx)));
        }
        else
            gyroscopeChange(event);
        xList.add(x);
        hList.add(h);
        lineGraph(xList,hList);
    }

    private void gyroscopeChange(SensorEvent event) {
        Float y = event.values[1];
        Float alpha = 1.f;
        Float dT = (event.timestamp - gyroscopeTimestamp) / 1000000000.0f;
        if (lastValuez > 0.02 || lastValuez < -0.02) {
            theta += y * dT;
            if (Math.sin(theta) > 0)
                alpha = 2.5f;
            else
                alpha = 5f;
            h -= (float) Math.sin(theta) * alpha;
        }
        gyroscopeTimestamp = event.timestamp;
        TextView textView = findViewById(R.id.gyroscope);
        textView.setText(Float.toString(h));
    }

    private void accelerationChange(SensorEvent event) {
        xChange(event);
        zChange(event);
        accelerometerTimestamp = event.timestamp;

    }

    private void xChange(SensorEvent event) {
        Float ax = abs(event.values[0]);
        Float difference = ax - lastValuex;
        int alpha = 1;
        if(difference > 0.2 || difference < -0.2) {
            float dT = (event.timestamp - accelerometerTimestamp) / 1000000000.0f;
            vx += ax  * dT;
            if (x < 10)
                alpha = 50;
            else if (x < 50)
                alpha = 10;
            else if(x < 100)
                alpha = 7;
            else if (x < 500)
                alpha = 4;
            else
                alpha = 2;
            x += vx * dT * alpha;

        }
        lastValuex = ax;

        TextView textView = findViewById(R.id.StartButton);
        textView.setText(Float.toString(x));
    }

    private void zChange(SensorEvent event) {
        lastValuez = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    public void stop(View view){

        Intent intent = new Intent(this, LineGraph.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        float[] xArray = toFloatArray(xList);
        float[] hArray = toFloatArray(hList);

        intent.putExtra(X_ARRAY, xArray);
        intent.putExtra(H_ARRAY, hArray);

        startActivity(intent);
        finish();
    }

    private float[] toFloatArray(List<Float> list){
        float[] arr = new float[list.size()];
        int index = 0;
        for (final Float value: list)
            arr[index++] = value;

        return arr;
    }
    private void lineGraph(List<Float> xList, List<Float> hList){

        DataPoint[] dataPoints = new DataPoint[xList.size()];
        for(int i=0 ; i< xList.size() ; i++)
              dataPoints[i] = new DataPoint(xList.get(i),hList.get(i));

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        graphView.removeAllSeries();
        graphView.addSeries(series);

    }

    private void setGraphAttr(){
        graphView = (GraphView) findViewById(R.id.graphView);
        graphView.setTitle("Online Output");
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(-50);
        graphView.getViewport().setMaxY(50);
        graphView.getViewport().setMaxX(200);


    }
}



