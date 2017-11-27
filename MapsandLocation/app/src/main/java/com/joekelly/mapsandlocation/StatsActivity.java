package com.joekelly.mapsandlocation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
    public double BMR;


    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Weight = "weightKey";
    public static final String Height = "heightKey";
    public static final String Age = "ageKey";
    public static final String Sex = "sexKey";


    Databasehelperclass myDb;
    DataBaseManagement flagsDb;

    public int todayssteps;
    public int week;
    public int overall;

    //Variable with the current steps
    private int numSteps;
    //Object which contains the step listeners;
    private SensorObject stepObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        Intent intent = getIntent();
        int numsteps = intent.getIntExtra("numSteps", 0);
        flagsDb = new DataBaseManagement(this);
        myDb = new Databasehelperclass(this);
        todayssteps = myDb.todaysSteps() + numsteps;
        week = myDb.weeksSteps() + numsteps;
        overall = myDb.overallSteps() + numsteps;
        TextView today_steps;
        TextView week_steps;
        TextView overall_steps;
        TextView today_flags;
        TextView week_flags;
        TextView overall_flags;



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
                return Color.rgb((int) data.getX()*100/4, (int) Math.abs(data.getY()*255/6), 100);
            }
        });




        // draw values on top
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.BLACK);

        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"Today","   Week", "    Overall", ""});

        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);




        today_steps = (TextView) findViewById(R.id.tvTodaySteps);
        week_steps = (TextView) findViewById(R.id.tvWeeklySteps);
        overall_steps = (TextView) findViewById(R.id.tvOverallSteps);
        today_flags = (TextView) findViewById(R.id.tvTodaysFlags);
        week_flags = (TextView) findViewById(R.id.textView9);
        overall_flags = (TextView) findViewById(R.id.textView10);


        loadSharedPreferences();

        TextView distance_today = (TextView) findViewById(R.id.tvTodaysDistance);
        TextView distance_week = (TextView) findViewById(R.id.tvWeeklyDistance);
        TextView distance_overall = (TextView) findViewById(R.id.tvOverallDistance);

        //current_steps.append(""+numsteps);
        today_steps.append(""+todayssteps);
        week_steps.append(""+week);
        overall_steps.append(""+overall);
        today_flags.append(flagsDb.todaysFlags()+"");
        week_flags.append(flagsDb.weeksFlags()+"");
        overall_flags.append(flagsDb.overallFlags()+"");
        //float distanceday = (Math.round(stepLength*week*100))/100.0f;
        //float distanceweek = (Math.round(stepLength*todayssteps*100))/100.0f;
        //float distanceoverall = (Math.round(stepLength*overall*100))/100.0f;

        distance_today.append(""+ (Math.round(stepLength*todayssteps*100))/100.0f + " km");
        distance_week.append(""+ (Math.round(stepLength*week*100))/100.0f + " km");
        distance_overall.append(""+ (Math.round(stepLength*overall*100))/100.0f + " km") ;

        if (BMI != 0.0 ){
        TextView calories_today = (TextView) findViewById(R.id.tvCaloriesToday);
        TextView calories_week = (TextView) findViewById(R.id.textView5);
        TextView calories_overall = (TextView) findViewById(R.id.tvOverallCalories);

            calories_today.append("Calories Burnt: "+ Math.round((BMR/24)*2.9*(((Math.round(stepLength*todayssteps*100))/100.0f)/5)));
            calories_week.append("Calories Burnt: "+ Math.round((BMR/24)*2.9*(((Math.round(stepLength*week*100))/100.0f)/5)));
            calories_overall.append("Calories Burnt: "+ Math.round((BMR/24)*2.9*((Math.round(stepLength*overall*100))/100.0f)/5));


        }



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
                    float x = Float.parseFloat(heightStr) / 100.0f; //needs to be metres
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
                stepLength = (loadHeight * 1.05)/1000;
                BMI = loadWeight / loadHeight * loadHeight;

//                Calculator Formulas
//                Calorie Burn = (BMR / 24) x MET x T
//                where
//
//                For males: BMR = (13.75 x WKG) + (5 x HC) - (6.76 x age) + 66
//                For females: BMR = (9.56 x WKG) + (1.85 x HC) - (4.68 x age) + 655
//
//                and
//
//                        BMR = Basal Metabolic Rate (over 24 hours)
//                MET = Metabolic Equivalent (for selected activity)
//                T = Activity duration time (in hours)
//                HC = Height (in centimetres)
//                WKG = Weight (in kilograms)
//                Time = (distance /average walking speed 5km/h)

                if (loadSex == true) {
                    BMR = ((9.56*loadWeight)+(1.85*loadHeight) - (4.68*loadAge)+ 655);
                }
                else {
                    BMR = ((13.57*loadWeight)+(5*loadHeight) - (6.76*loadAge)+ 66);
                }

            }

        }
        else {
            stepLength = 0.74/1000; //average step length
        }


    }


    private void saveWeightHeight(float heightValue, float weightValue,  float age, boolean female) {
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
//        myDb.addSteps(new Steps(stepObject.numSteps));
    }
    protected void onPause(){
        super.onPause();
        myDb.addSteps(new Steps(stepObject.numSteps));
    }

}
