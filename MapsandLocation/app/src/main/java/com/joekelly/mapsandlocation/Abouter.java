package com.joekelly.mapsandlocation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

public class Abouter extends AppCompatActivity {


    ListView listView;
    List list = new ArrayList<>();
    Adapter arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abouter);

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

                            for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                // do something with the individual "issues"

                                Map<String, Object> scoreMap = (Map<String, Object>) issue.getValue();
//                                Log.i("value", scoreMap.get("flags collect").toString());

                                String value2 = "Rank "+Integer.toString(size-counter)+": "+scoreMap.get("name").toString() +" \nflags captured: " +scoreMap.get("flags collected").toString()
                                        +" \nflags stolen: " +scoreMap.get("flags stolen").toString();
                                list.add(0, value2);
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
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter((ListAdapter) arrayAdapter);
    }

}
