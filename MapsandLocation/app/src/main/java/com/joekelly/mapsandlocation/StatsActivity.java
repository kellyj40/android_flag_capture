package com.joekelly.mapsandlocation;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

public class StatsActivity extends AppCompatActivity {


    Databasehelperclass myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Intent intent = getIntent();
        int numsteps = intent.getIntExtra("numSteps", 0);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Maybe email stats to yourself?", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        myDb = new Databasehelperclass(this);
        int todayssteps = myDb.todaysSteps() + numsteps;
        int week = myDb.weeksSteps() + numsteps;
        int overall = myDb.overallSteps() + numsteps;
        TextView current_steps;
        TextView today_steps;
        TextView week_steps;
        TextView overall_steps;
        //taken from graphView examples
        GraphView graph = (GraphView) findViewById(R.id.gGraph);
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[] {

                new DataPoint (0, todayssteps),
                new DataPoint(2, week),
                new DataPoint(4, overall)



        });
        graph.addSeries(series);
        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);
            }
        });




        // draw values on top
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.RED);

        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"Today","   Week", "    Overall", ""});

        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);



        current_steps = (TextView) findViewById(R.id.tvCurrentSession);
        today_steps = (TextView) findViewById(R.id.tvTodaySteps);
        week_steps = (TextView) findViewById(R.id.tvWeeklySteps);
        overall_steps = (TextView) findViewById(R.id.tvOverallSteps);

        current_steps.append(""+numsteps);
        today_steps.append(""+todayssteps);
        week_steps.append(""+week);
        overall_steps.append(""+overall);


    }



}
