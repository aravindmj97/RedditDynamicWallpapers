package com.a.redditfetch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jreddit.entity.Submission;
import com.github.jreddit.entity.Subreddit;
import com.github.jreddit.entity.User;
import com.github.jreddit.retrieval.Submissions;
import com.github.jreddit.retrieval.params.SubmissionSort;
import com.github.jreddit.utils.restclient.PoliteHttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    RecyclerView photoRecycle;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<ImagePojo> photoArray = new ArrayList<>();
    DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("url");
    FloatingActionButton fab;
    Toolbar toolbar;
    Switch timerSwitch;
    ImageView deleteLOcal;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) MainActivity.this.findViewById(R.id.toolBarInclude);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        Intent alarmIntent = new Intent(MainActivity.this, TimeReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

        timerSwitch = (Switch) toolbar.findViewById(R.id.timer);
        deleteLOcal = (ImageView) toolbar.findViewById(R.id.deleteLocal);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.active_switches), Context.MODE_PRIVATE);

        int timerToogle = sharedPref.getInt(getString(R.string.timer_on), 0);
        if(timerToogle == 1){
            timerSwitch.setChecked(true);
        }

        timerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    start();
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.timer_on), 1);
                    editor.commit();
                } else {
                    cancel();
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.timer_on), 0);
                    editor.commit();
                }
            }
        });

        deleteLOcal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ImagePojo> imagePojos = ImagePojo.listAll(ImagePojo.class);
                ImagePojo.deleteAll(ImagePojo.class);
                Toast.makeText(MainActivity.this, "Local DB Cleared!", Toast.LENGTH_SHORT).show();
                fab.callOnClick();
            }
        });

        photoRecycle = (RecyclerView) findViewById(R.id.recycler);
        photoRecycle.setHasFixedSize(true);
       // layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        layoutManager = new GridLayoutManager(this, 2);
        photoRecycle.setLayoutManager(layoutManager);

        fab = (FloatingActionButton) findViewById(R.id.sync);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0;i<photoArray.size();i++){
                    ImagePojo imagePojo = new ImagePojo(photoArray.get(i).getUrl(), photoArray.get(i).getKey());
                    imagePojo.save();
                }
                Toast.makeText(MainActivity.this, "Synced", Toast.LENGTH_SHORT).show();
            }
        });

        dRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                for(int i=0;i<photoArray.size();i++){
                    if (photoArray.get(i).getKey().equals(dataSnapshot.getKey())){
                        photoArray.remove(i);
                        photoRecycle.getAdapter().notifyItemRemoved(i);
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void start() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 5000;

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()+10000,
                1000 * 60 * 5, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

    public void cancel() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getData();
    }

    public void getData(){
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    photoArray.clear();
                    for (DataSnapshot dsnap : dataSnapshot.getChildren()){
                        photoArray.add(dsnap.getValue(ImagePojo.class));
                    }
                    adapter = new ImageRecyclerAdapter(photoArray, getApplicationContext());
                    photoRecycle.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
