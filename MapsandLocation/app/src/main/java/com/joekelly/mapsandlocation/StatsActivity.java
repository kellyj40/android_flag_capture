package com.joekelly.mapsandlocation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

// Activity for the stats screen
public class StatsActivity extends AppCompatActivity {
    //shared prefrences
    SharedPreferences sharedpreferences;

    private EditText height;
    private EditText weight;
    Button b1;
    ToggleButton sex;
    public EditText age;


    public boolean loadSex;
    public float loadAge;
    public float loadHeight;
    public float loadWeight;
    public double stepLength;
    public double BMI;
    public double caloriesBurnt;


    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Weight = "weightKey";
    public static final String Height = "heightKey";
    public static final String Age = "ageKey";
    public static final String Sex = "sexKey";


    Databasehelperclass myDb;

    //Variable with the current steps
    private int numSteps;
    //Object which contains the step listeners;
    private SensorObject stepObject;

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
        series.setValuesOnTopColor(Color.BLACK);

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


        //Step stuff

        stepObject = new SensorObject();
        numSteps= stepObject.numSteps;
        stepObject.initialiseStepSensor(this);

        //height weight and BMI:
        height = (EditText) findViewById(R.id.height);
        weight = (EditText) findViewById(R.id.weight);
        age = (EditText) findViewById(R.id.age);
        sex = (ToggleButton)findViewById(R.id.tgsex);
        b1 = (Button)findViewById(R.id.calc);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String heightStr = height.getText().toString();
                String weightStr = weight.getText().toString();
                String ageStr = age.getText().toString();
                boolean female = sex.isChecked();
//                if (sex.isChecked()){
//                boolean female = true;}
//                else {boolean female = false;}

                if (!"".equals(heightStr)
                        && !"".equals(weightStr) && !"".equals(ageStr)) {
                    float x = Float.parseFloat(heightStr) / 100; //needs to be metres
                    float y = Float.parseFloat(weightStr);
                    float z = Float.parseFloat(ageStr);



                    saveWeightHeight(x, y, z, female);
                }
            }




    });


    }

    private void loadSharedPreferences() {
        if (sharedpreferences != null) {
            loadHeight= sharedpreferences.getFloat(
                    Height , 0);
            loadWeight= sharedpreferences.getFloat(
                    Weight, 0);
            loadAge = sharedpreferences.getFloat(
                    Age, 0);
            loadSex =  sharedpreferences.getBoolean(
                    Sex, true);
            if (loadHeight != 0 && loadWeight!= 0 && loadAge!=0 ) {
                stepLength = (loadHeight * 0.413)/1000;
                BMI = loadWeight / loadHeight * loadHeight;
                //For males: BMR = (13.75 x WKG) + (5 x HC) - (6.76 x age) + 66
                //For females: BMR = (9.56 x WKG) + (1.85 x HC) - (4.68 x age) + 655
                if (loadSex == true) {
                    caloriesBurnt = (((9.56*loadWeight)+(1.85*loadHeight) - (4.68*loadAge)+ 655)/24)*2.9;
                }
                else {
                    caloriesBurnt = (((13.57*loadWeight)+(5*loadHeight) - (6.76*loadAge)+ 66)/24)*2.9;
                }

            }

        }
        else {
            stepLength = 74/1000;
        }

        Toast.makeText(StatsActivity.this,"Stride"+stepLength,Toast.LENGTH_SHORT).show();
        Toast.makeText(StatsActivity.this,"BMI"+BMI,Toast.LENGTH_SHORT).show();
        Toast.makeText(StatsActivity.this,"calories"+caloriesBurnt,Toast.LENGTH_SHORT).show();
        Toast.makeText(StatsActivity.this,"female"+loadSex, Toast.LENGTH_SHORT).show();
    }


    private void saveWeightHeight(float weightValue, float heightValue, float age, boolean female) {
        float bmi = weightValue / (heightValue * heightValue);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putFloat(Weight, weightValue);
        editor.putFloat(Height, heightValue);
        editor.putFloat(Age, age);
        editor.putBoolean(Sex, female); //true if female
        editor.commit();
        Toast.makeText(StatsActivity.this,"Saved",Toast.LENGTH_SHORT).show();
    }


    protected void onStart() {
        super.onStart();
        loadSharedPreferences();
    }

    protected void onStop() {
        super.onStop();
        myDb.addSteps(new Steps(stepObject.numSteps));
    }

}
