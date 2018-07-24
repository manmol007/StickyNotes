package com.example.anmol.stickynote;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import br.com.goncalves.pugnotification.notification.PugNotification;

public class NotificationBroadcast extends Service {

    String date;
    String time;
    String rdate,rtime,msg;
    ArrayList<data> list=new ArrayList<>();
    DatabaseReference reference;
    Notification notification;
    SharedPreferences preferences;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final PendingIntent intent1=PendingIntent.getActivity(this,0,new Intent(this,NavActivity.class),0);

        preferences=this.getSharedPreferences("user",MODE_PRIVATE);

        reference= FirebaseDatabase.getInstance().getReference().child(preferences.getString("user",null)).child("notes");


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                list.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    data d=snapshot.getValue(data.class);
                    list.add(d);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final Handler  handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                    date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                    time = new SimpleDateFormat("HH:mm").format(new Date());
                    Log.i("date", date);
                    Log.i("time", time);
                    for(int i=0;i<list.size();++i){

                        rdate=list.get(i).getRdate();
                        rtime=list.get(i).getRtime();
                        msg=list.get(i).getNotes();
                        Log.i("rdate",rdate);
                        if(rdate.equals(date)) {
                            Log.i("status","date matched");
                            if (rtime.equals(time)) {
                                Log.i("status", "success");
       PugNotification.with(getApplicationContext())
                                        .load()
                                        .title("Notification")
                                        .message(msg).click(intent1).autoCancel(true)
                                        .flags(Notification.DEFAULT_ALL).smallIcon(R.drawable.file)
                                        .simple()
                                        .build();

                                stopService(new Intent(getApplicationContext(),NotificationBroadcast.class));

                                return;
                            } else {
                                handler.postDelayed(this, 1000);
                            }
                        }
                        else{
                            handler.postDelayed(this, 1000);
                        }
                    }
                }
        },1000);

        stopService(new Intent(getApplicationContext(),NotificationBroadcast.class));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
