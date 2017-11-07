package com.joekelly.mapsandlocation;

import android.content.Intent;
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
        int todayssteps = myDb.todaysSteps();
        int week = myDb.weeksSteps();
        int overall = myDb.overallSteps();
        TextView current_steps;
        TextView today_steps;
        TextView week_steps;
        TextView overall_steps;

//
//
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
