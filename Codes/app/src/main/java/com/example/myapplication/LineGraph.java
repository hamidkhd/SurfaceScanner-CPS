package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class LineGraph extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        Intent intent = getIntent();
        float[] xArray =  intent.getFloatArrayExtra(ReadSensorData.X_ARRAY);
        float[] hArray =  intent.getFloatArrayExtra(ReadSensorData.H_ARRAY);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();

        for(int i=0 ; i< xArray.length ; i++){
            series.appendData(new DataPoint(xArray[i],hArray[i]),true,xArray.length);
        }

        GraphView graphView = (GraphView) findViewById(R.id.graph);
        graphView.setTitle("Sample Output");
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMaxX(200);
        graphView.getViewport().setMaxY(50);
        graphView.getViewport().setMinY(-50);
        graphView.addSeries(series);

    }

}