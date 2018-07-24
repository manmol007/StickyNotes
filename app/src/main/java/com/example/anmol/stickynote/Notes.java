package com.example.anmol.stickynote;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import br.com.goncalves.pugnotification.notification.PugNotification;

import static android.graphics.Typeface.SANS_SERIF;
import static android.graphics.Typeface.SERIF;
import static android.graphics.Typeface.createFromAsset;

public class Notes extends Fragment {

    FloatingActionMenu menu;
    com.github.clans.fab.FloatingActionButton add;
    GridView grid;
    String notes;
    DatabaseReference reference;
    MyAdapter adapter;
    String sharedata;
    DatabaseReference mReference,ArchiveReference,TrashReference;
    Boolean clicked=false;
    FragmentTransaction transaction;
    ArrayList<data> myData,adapterData;
    Long result;
    SharedPreferences preferences;
    MaterialDialog.Builder builder;
    MaterialDialog dialog;
    String rdate,rtime;
    int flag=0;
    int settype=0;
    Boolean LongClick=false;
    ArrayList<Uri> ImageUri=new ArrayList<>();
    ActionMode actionMode;

    ArrayList<Integer> index=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        transaction=getFragmentManager().beginTransaction();
        final View view=inflater.inflate(R.layout.notes_layout,container,false);
        myData=new ArrayList();
        adapterData=new ArrayList<>();
        preferences=getActivity().getSharedPreferences("user",Context.MODE_PRIVATE);
        builder=new MaterialDialog.Builder(getActivity()).content("Loading").progress(true,0);
        dialog=builder.build();
        dialog.show();
        mReference= FirebaseDatabase.getInstance().getReference();
        menu=(FloatingActionMenu)view.findViewById(R.id.menu);
        add=(com.github.clans.fab.FloatingActionButton)view.findViewById(R.id.add);
        grid=(GridView)view.findViewById(R.id.notes_grid);
        setHasOptionsMenu(true);

        reference=mReference.child(preferences.getString("user",null)).child("notes");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    myData.clear();

                for(DataSnapshot snap:dataSnapshot.getChildren()){

                    data d=snap.getValue(data.class);
                   myData.add(d);
                }

                Log.i("mydata",myData.toString());
                adapter=new MyAdapter(getActivity(),myData);
                grid.setAdapter(adapter);
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(getActivity(),ADD.class);
                startActivity(intent);
            }
        });


        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(LongClick){


                }
                else {
                    actionMode= getActivity().startActionMode(new ActionBarCallBack());

                    view.setAlpha((float) 0.5);

                    index.add(i);

                    clicked = true;
                    LongClick=true;
                }
                return true;
            }
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                Boolean flag = false;

                if (LongClick) {
                    for (int j = 0; j < index.size(); ++j) {


                        if (index.get(j) == i) {

                            flag = true;
                            index.remove(j);
                            view.setAlpha(1);
                            break;


                        }

                    }

                    if (!flag) {
                        if (clicked) {
                            view.setAlpha((float) 0.5);
                            index.add(i);
                        }
                    } else if (index.size() == 0) {

                        actionMode.finish();
                    }
                } else {

                    Log.i("ith value",Integer.toString(i));
                    Intent intent = new Intent(getActivity(), ADD.class);
                    intent.putExtra("notes", myData.get(i).getNotes());
                    intent.putExtra("font", myData.get(i).getFont());
                    intent.putExtra("color", myData.get(i).getColor());

                    if (myData.get(i).getImageurl() != null) {
                        intent.putExtra("imageurl", myData.get(i).getImageurl());
                        intent.putExtra("imageuri", myData.get(i).getImageuri());
                    }
                    startActivity(intent);
                }
            }
        });
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.finalmenu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(R.id.grid==item.getItemId()){
            if(flag==0) {
                grid.setNumColumns(2);
                item.setIcon(R.mipmap.mysinglegrid);
                flag=1;
            }else
            {
                grid.setNumColumns(1);
                item.setIcon(R.mipmap.mygrid);
                flag=0;
            }
        }

        return true;
    }

    @SuppressLint("ValidFragment")
    public class ActionBarCallBack implements ActionMode.Callback{

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

            actionMode.getMenuInflater().inflate(R.menu.mainmenu,menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {


            if(R.id.delete==menuItem.getItemId()){

                for (int i = 0; i < index.size(); ++i) {

                    final DatabaseReference permanentreference = mReference.child(preferences.getString("user", null)).child("Trash");

                   final String mnotes = myData.get(index.get(i)).getNotes();

                     Log.i("notes",mnotes);

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                data d = snapshot.getValue(data.class);

                                if (d.getNotes().equals(mnotes)) {

                                    permanentreference.push();

                                    reference.child(snapshot.getKey()).removeValue();
                                    permanentreference.child("notes").setValue(d.getNotes());
                                    permanentreference.child("font").setValue(d.getFont());
                                    permanentreference.child("color").setValue(d.getColor());
                                    if (d.getImageurl() != null) {
                                        permanentreference.child("imageurl").setValue(d.getImageurl());
                                        permanentreference.child("imageuri").setValue(d.getImageuri());
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                        actionMode.finish();
            }
            else if(R.id.reminder==menuItem.getItemId()){

                AlertDialog.Builder builder=new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AppTheme));
                builder.setTitle("Add a Reminder");
                final LayoutInflater inflater=getActivity().getLayoutInflater();
                final View dialogview=inflater.inflate(R.layout.reminder,null);
                builder.setView(dialogview);

                final TextView date=(TextView)dialogview.findViewById(R.id.selectDate);
                final TextView time=(TextView)dialogview.findViewById(R.id.selectTime);

                date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final Calendar c=Calendar.getInstance();
                        int year=c.get(Calendar.YEAR);
                        int month=c.get(Calendar.MONTH);
                        int day=c.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dialog=new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

                                String low=null;
                                if(i1<9){
                                    low="0"+(i1+1);
                                }else{
                                    low= String.valueOf(i1);
                                }

                                rdate =i2+"-"+low+"-"+i;
                                date.setText(rdate);
                            }
                        },year,month,day);

                        dialog.getDatePicker().setMinDate(c.getTimeInMillis());
                        dialog.show();
                    }
                });

                time.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final Calendar c=Calendar.getInstance();
                        int hour=c.get(Calendar.HOUR);
                        int min=c.get(Calendar.MINUTE);

                        TimePickerDialog dialog=new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {

                                String low=null;
                                if(i1<9){
                                    low="0"+(i1);
                                }
                                else{
                                    low= String.valueOf(i1);
                                }

                                rtime=i+":"+low;
                                time.setText(rtime);
                            }
                        },hour,min,false);

                        dialog.show();
                    }
                });

                builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        for(int j = 0; j<index.size(); ++j){

                            notes=myData.get(index.get(j)).getNotes();
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                                        data d=snapshot.getValue(data.class);

                                        if(d.getNotes().equals(notes)){

                                            reference.child(snapshot.getKey()).child("rdate").setValue(rdate);
                                            reference.child(snapshot.getKey()).child("rtime").setValue(rtime);

                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        Intent intent1=new Intent(getActivity(),NotificationBroadcast.class);
                        getActivity().startService(intent1);
                        actionMode.finish();
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
            }
            else if(R.id.archive==menuItem.getItemId()){

                for(int i=0;i<index.size();++i){

                     final String mnotes=myData.get(index.get(i)).getNotes();

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                                ArchiveReference=mReference.child(preferences.getString("user",null)).child("Archive").child(snapshot.getKey());

                                data d=snapshot.getValue(data.class);

                                if(d.getNotes().equals(mnotes)){
                                    reference.child(snapshot.getKey()).removeValue();
                                    ArchiveReference.child("notes").setValue(d.getNotes());
                                    ArchiveReference.child("font").setValue(d.getFont());
                                    ArchiveReference.child("color").setValue(d.getColor());
                                    if(d.getImageurl()!=null){
                                        ArchiveReference.child("imageurl").setValue(d.getImageurl());
                                        ArchiveReference.child("imageuri").setValue(d.getImageuri());
                                    }
                                }

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    actionMode.finish();
                }
                }
            else if(R.id.share==menuItem.getItemId()){

                sharedata=myData.get(index.get(0)).getNotes();

                for(int i=1;i<index.size();++i){
                    sharedata+="\n";
                    sharedata+=myData.get(index.get(i)).getNotes();
                }

                Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                sharingIntent.setType("text/*");
                for(int i=0;i<index.size();++i){

                    if(myData.get(index.get(i)).getImageuri()!=null) {
                        sharingIntent.setType("image/*");
                        settype=1;
                        ImageUri.add(Uri.parse(myData.get(index.get(i)).getImageuri()));
                    }
                }
                if(settype==1){
                    sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,ImageUri);
                }
                sharingIntent.putExtra(Intent.EXTRA_TEXT,sharedata);
                startActivity(sharingIntent);

            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

            startActivity(new Intent(getActivity(),NavActivity.class));
            getActivity().overridePendingTransition(0,0);
        }
    }

    public class MyAdapter extends BaseAdapter {

        Context ctx;
        ArrayList<data> myData;


        public MyAdapter(Context ctx,ArrayList<data> myData) {
            this.myData = myData;
            this.ctx=ctx;
        }

        @Override
        public int getCount() {
            return myData.size();
        }

        @Override
        public Object getItem(int i) {
            return myData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

                    view= LayoutInflater.from(ctx).inflate(R.layout.card,viewGroup,false);

                    data d=(data)this.getItem(i);
                    TextView text=(TextView)view.findViewById(R.id.datatext);
                    ImageView img=(ImageView)view.findViewById(R.id.img);
                    String fontget = d.getFont();
                    String colorget = d.getColor();

                    view.setBackgroundColor(Integer.parseInt(colorget));

            if (fontget.equals("barbaric")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "barbaric.ttf");
                text.setTypeface(typeface);
            } else if (fontget.equals("bloody")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "bloody.TTF");
                text.setTypeface(typeface);
            } else if (fontget.equals("blox")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "blox.ttf");
                text.setTypeface(typeface);
            } else if (fontget.equals("boston")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "boston.ttf");
                text.setTypeface(typeface);
            } else if (fontget.equals("charles_s")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "charles_s.ttf");
                text.setTypeface(typeface);
            } else if (fontget.equals("chop")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "chop.TTF");
                text.setTypeface(typeface);
            } else if (fontget.equals("degrassi")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "degrassi.ttf");
                text.setTypeface(typeface);
            } else if (fontget.equals("delicious")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "delicious.ttf");
                text.setTypeface(typeface);
            } else if (fontget.equals("koshlang")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "koshlang.ttf");
                text.setTypeface(typeface);
            } else if (fontget.equals("lokicola")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "lokicola.TTF");
                text.setTypeface(typeface);
            } else if (fontget.equals("nofutur")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "nofutur.ttf");
                text.setTypeface(typeface);
            } else if (fontget.equals("romantic")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "romantic.ttf");
                text.setTypeface(typeface);
            }

            text.setText(d.getNotes());
            if(d.getImageurl()!=null) {
                img.getLayoutParams().height = 200;
                Glide.with(ctx).load(d.getImageurl()).into(img);
            }
            return view;
        }
    }

}
