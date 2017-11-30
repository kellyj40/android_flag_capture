package com.joekelly.mapsandlocation;
/*
    This class will query th global database, sort all results in order of collected flags
    and display in a list view using an adapter
 */
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Leaderboard extends AppCompatActivity {

    // Variablees
    ListView listView;
    List listOfScores = new ArrayList<>();
    Adapter arrayAdapter;
    /*
    On create will set up listener to query the database
    and then set the listview to the values
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abouter);
        setContentView(R.layout.activity_abouter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Connect to database
        // Get reference to database
        DatabaseReference allUserRef = FirebaseDatabase.getInstance().getReference("users");
        Query query = allUserRef.orderByChild("flags collected");

        query.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            int counter = 0;
                            // dataSnapshot is the "issue" node with all children with id 0
                            Map<String, Object> fullMap = (Map<String, Object>) dataSnapshot.getValue();
                            int size = fullMap.size();
                            //Loop through all the results
                            for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                // do something with the individual "issues"

                                Map<String, Object> scoreMap = (Map<String, Object>) issue.getValue();

                                String value2 = "Rank "+Integer.toString(size-counter)+": "+scoreMap.get("name").toString() +" \nFlags captured: " +scoreMap.get("flags collected").toString()
                                        +" \nFlags stolen: " +scoreMap.get("flags stolen").toString();
                                listOfScores.add(0, value2);
                                counter++;
                            }
                            listAdapterSetup();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });



    }
    public void listAdapterSetup(){
        //Initialise values
        listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfScores){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the current item from ListView
                View view = super.getView(position,convertView,parent);
                if(position%2 == 1) {
                    // Set a background color for ListView regular row/item
                    view.setBackgroundColor(Color.parseColor("#f4f9f9"));
                }
                else {
                    // Set the background color for alternate row/item
                    view.setBackgroundColor(Color.parseColor("#7fbfbf"));
                }
                return view;
            }
        };
        listView.setAdapter((ListAdapter) arrayAdapter);
    }

}
