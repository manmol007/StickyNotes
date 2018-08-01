package com.example.anmol.stickynote;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.multidots.fingerprintauth.FingerPrintAuthCallback;
import com.multidots.fingerprintauth.FingerPrintAuthHelper;

import java.util.ArrayList;
import java.util.Calendar;

import static android.graphics.Typeface.SANS_SERIF;
import static android.graphics.Typeface.SERIF;
import static android.graphics.Typeface.createFromAsset;

public class Archive extends Fragment implements FingerPrintAuthCallback {

    String notes;
    FingerPrintAuthHelper mFingerPrint;
    DatabaseReference reference;
    MyAdapter adapter;
    Intent intent;
    ViewAdapter dataadapter;
    Intent sharingIntent;
    String sharedata;
    DatabaseReference mReference,ArchiveReference,TrashReference;
    Boolean clicked=false;
    ArrayList<data> viewData;
    ActionMode actionMode;
    ArrayList<String> myData;
    SharedPreferences preferences;
    String rdate,rtime;
    int flag=0;
    ArrayList<String> category;
    AlertDialog dialog;
    Boolean LongClick=false;
    RelativeLayout relativeLayout;
    RecyclerView list;
    String password;
    ArrayList<Uri> ImageUri=new ArrayList<>();
    ArrayList<String> index=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        mFingerPrint=FingerPrintAuthHelper.getHelper(getActivity(),this);
        final View view=inflater.inflate(R.layout.trash_layout,container,false);
        myData=new ArrayList();
        relativeLayout=(RelativeLayout)view.findViewById(R.id.layout);
        mReference= FirebaseDatabase.getInstance().getReference();
        preferences=getActivity().getSharedPreferences("user",Context.MODE_PRIVATE);
        list=(RecyclerView)view.findViewById(R.id.list);
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        reference=mReference.child(preferences.getString("user",null)).child("Archive");

        mReference.child(preferences.getString("user",null)).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    password= dataSnapshot.getValue().toString();
                    Log.i("password",password);


                AlertDialog.Builder builder=new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AppTheme));
                final View mview=getActivity().getLayoutInflater().inflate(R.layout.password,null);
                builder.setView(mview);
                final EditText pass=(EditText)mview.findViewById(R.id.Protected);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        mFingerPrint.stopAuth();
                        startActivity(new Intent(getActivity(),NavActivity.class));
                        getActivity().overridePendingTransition(0,0);

                    }
                });
                builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String GetPassword = pass.getText().toString();
                        if (TextUtils.isEmpty(GetPassword)) {

                            Snackbar.make(relativeLayout, "Enter the password to proceed", Snackbar.LENGTH_LONG).show();

                        } else {
                            if(password.equals(GetPassword)){

                                reference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        myData.clear();

                                        for(DataSnapshot snap:dataSnapshot.getChildren()) {

                                            String d = snap.getValue(data.class).getCategory();

                                            if(!myData.contains(d)){
                                                myData.add(d);
                                            }
                                        }

                                        adapter=new MyAdapter(getActivity(),myData);
                                        adapter.notifyDataSetChanged();
                                        list.setAdapter(adapter);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else
                                {
                                    Intent intent = new Intent(getActivity(), NavActivity.class);
                                    startActivity(intent);
                            }
                        }
                    }
                });
                 dialog=builder.create();
                 dialog.setCanceledOnTouchOutside(false);
                 dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onNoFingerPrintHardwareFound() {

        Toast.makeText(getActivity(),"Your phone doesnt support fingerprint",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNoFingerPrintRegistered() {

        Toast.makeText(getActivity(),"You havnt registered any fingerprint",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onBelowMarshmallow() {

        Toast.makeText(getActivity(),"Android is less than marshmello",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {

         dialog.dismiss();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                myData.clear();

                for(DataSnapshot snap:dataSnapshot.getChildren()) {

                    String d = snap.getValue(data.class).getCategory();

                    if(!myData.contains(d)){
                        myData.add(d);
                    }
                }

                adapter=new MyAdapter(getActivity(),myData);
                adapter.notifyDataSetChanged();
                list.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onAuthFailed(int errorCode, String errorMessage) {
        Toast.makeText(getActivity(),"Try again unable to recognize",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFingerPrint.startAuth();
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerPrint.stopAuth();
    }

    @SuppressLint("ValidFragment")
    public class ActionBarCallBack implements ActionMode.Callback{

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

            actionMode.getMenuInflater().inflate(R.menu.archviemenu,menu);

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

                    final DatabaseReference permanentreference = mReference.child(preferences.getString("user", null)).child("Trash").push();

                    final String mnotes = index.get(i);

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
                                    permanentreference.child("category").setValue(d.getCategory());
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

                            notes=index.get(j);
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
            else if(R.id.unArchive==menuItem.getItemId()){


                for(int i=0;i<index.size();++i){
                    final DatabaseReference unArchivereference=mReference.child(preferences.getString("user",null)).child("notes").push();

                    final String notes=index.get(i);

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                                data d=snapshot.getValue(data.class);

                                if(d.getNotes().equals(notes)){
                                    reference.child(snapshot.getKey()).removeValue();
                                    unArchivereference.child("notes").setValue(d.getNotes());
                                    unArchivereference.child("font").setValue(d.getFont());
                                    unArchivereference.child("color").setValue(d.getColor());
                                    unArchivereference.child("category").setValue(d.getCategory());
                                    if(d.getImageurl()!=null){
                                        unArchivereference.child("imageurl").setValue(d.getImageurl());
                                        unArchivereference.child("imageuri").setValue(d.getImageuri());
                                    }
                                actionMode.finish();
                                }

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

                Toast.makeText(getActivity(),index.toString(),Toast.LENGTH_LONG).show();
            }
            else if(R.id.share==menuItem.getItemId()){


                sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

                sharedata=index.get(0);

                for(int i=1;i<index.size();++i){
                    sharedata+="\n";
                    sharedata+=index.get(i);
                }


                sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                sharingIntent.setType("text/*");

                sharingIntent.putExtra(Intent.EXTRA_TEXT,sharedata);
                for(int i=0;i<index.size();++i){
                    final int finalI = i;
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                                data d=snapshot.getValue(data.class);

                                if(d.getNotes().equals(index.get(finalI))){
                                    if(d.getImageurl()!=null){
                                        Log.i("true ","true");
                                        if(!ImageUri.contains(d.getImageuri()))
                                            ImageUri.add(Uri.parse(d.getImageuri()));
                                    }
                                }
                            }
                            if(!ImageUri.isEmpty()) {

                                Toast.makeText(getActivity(),ImageUri.toString(),Toast.LENGTH_LONG).show();

                                sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,ImageUri);
                                sharingIntent.setType("image/*");
                            }
                            startActivity(sharingIntent);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

            startActivity(new Intent(getActivity(),NavActivity.class));
            getActivity().overridePendingTransition(0,0);
        }
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder>{

        ArrayList<String> myData;
        Context ctx;

        public MyAdapter(Context ctx,ArrayList<String> myData){

            this.myData=myData;
            this.ctx=ctx;

        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.noteslistlayout,parent,false);

            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

            final int peak=position;
            viewData=new ArrayList();
            holder.category.setText(myData.get(position));
            holder.categorylist.setLayoutManager(new LinearLayoutManager(ctx,LinearLayoutManager.HORIZONTAL,false));
            holder.categorylist.setHasFixedSize(true);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    viewData.clear();
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        data d=snapshot.getValue(data.class);

                        viewData.add(d);
                    }
                    ArrayList<data> list=new ArrayList<>();
                    for(int i=0;i<viewData.size();++i){

                        if(holder.category.getText().equals(viewData.get(i).getCategory())){
                            list.add(viewData.get(i));
                        }

                    }
                    dataadapter=new ViewAdapter(list,ctx);
                    holder.categorylist.setAdapter(dataadapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        @Override
        public int getItemCount() {
            return myData.size();
        }

        public class MyHolder extends RecyclerView.ViewHolder{

            TextView category;
            RecyclerView categorylist;
            View gridview;

            public MyHolder(View itemView) {
                super(itemView);
                category=(TextView)itemView.findViewById(R.id.category);
                categorylist=(RecyclerView)itemView.findViewById(R.id.viewlist);
                gridview=itemView;
            }
        }
    }

    public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.viewHolder>{

        ArrayList<data> viewData;
        Context ctx;

        public ViewAdapter(ArrayList<data> viewData,Context ctx){

            this.viewData=viewData;
            this.ctx=ctx;

        }


        @NonNull
        @Override
        public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.listviewbind,parent,false);

            return new viewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final viewHolder holder, int position) {

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Boolean flag = false;

                    Log.i("index",index.toString());
                    if (LongClick) {
                        for (int j = 0; j < index.size(); ++j) {


                            if (index.get(j).equals( holder.viewtext.getText().toString())) {

                                flag = true;
                                index.remove(j);
                                view.setAlpha(1);
                                break;
                            }

                        }

                        if (!flag) {
                            if (clicked) {
                                view.setAlpha((float) 0.5);
                                index.add(holder.viewtext.getText().toString());
                            }
                        } else if (index.size() == 0) {

                            actionMode.finish();
                        }
                    } else {

                        intent = new Intent(getActivity(), ADD.class);

                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                    data d=snapshot.getValue(data.class);
                                    if(d.getNotes().equals(holder.viewtext.getText().toString())){
                                        intent.putExtra("notes",d.getNotes());
                                        intent.putExtra("category",d.getCategory());
                                        intent.putExtra("color",d.getColor());
                                        intent.putExtra("font",d.getFont());
                                        if(d.getImageurl()!=null){
                                            intent.putExtra("imageurl",d.getImageurl());
                                            intent.putExtra("imageuri",d.getImageuri());
                                        }
                                        reference.child(snapshot.getKey()).removeValue();
                                    }
                                }
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }
            });

            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(LongClick){


                    }
                    else {
                        actionMode= getActivity().startActionMode(new ActionBarCallBack());

                        view.setAlpha((float) 0.5);

                        index.add(holder.viewtext.getText().toString());

                        clicked = true;
                        LongClick=true;
                        Log.i("index",index.toString());
                    }
                    return true;
                }

            });

            holder.viewtext.setText(viewData.get(position).getNotes());
            String fontget = viewData.get(position).getFont();
            String colorget = viewData.get(position).getColor();
            holder.mView.setBackgroundColor(Integer.parseInt(colorget));
            if (fontget.equals("barbaric")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "barbaric.ttf");
                holder.viewtext.setTypeface(typeface);
            } else if (fontget.equals("bloody")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "bloody.TTF");
                holder.viewtext.setTypeface(typeface);
            } else if (fontget.equals("blox")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "blox.ttf");
                holder.viewtext.setTypeface(typeface);
            } else if (fontget.equals("boston")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "boston.ttf");
                holder.viewtext.setTypeface(typeface);
            } else if (fontget.equals("charles_s")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "charles_s.ttf");
                holder.viewtext.setTypeface(typeface);
            } else if (fontget.equals("chop")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "chop.TTF");
                holder.viewtext.setTypeface(typeface);
            } else if (fontget.equals("degrassi")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "degrassi.ttf");
                holder.viewtext.setTypeface(typeface);
            } else if (fontget.equals("delicious")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "delicious.ttf");
                holder.viewtext.setTypeface(typeface);
            } else if (fontget.equals("koshlang")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "koshlang.ttf");
                holder.viewtext.setTypeface(typeface);
            } else if (fontget.equals("lokicola")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "lokicola.TTF");
                holder.viewtext.setTypeface(typeface);
            } else if (fontget.equals("nofutur")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "nofutur.ttf");
                holder.viewtext.setTypeface(typeface);
            } else if (fontget.equals("romantic")) {
                Typeface typeface = createFromAsset(getActivity().getAssets(), "romantic.ttf");
                holder.viewtext.setTypeface(typeface);
            }
            if (viewData.get(position).getImageurl() != null) {
                holder.viewImg.getLayoutParams().height = 200;
                Glide.with(ctx).load(viewData.get(position).getImageurl()).into(holder.viewImg);
            }
        }

        @Override
        public int getItemCount() {
            return viewData.size();
        }

        public class viewHolder extends RecyclerView.ViewHolder{

            ImageView viewImg;
            TextView viewtext;
            View mView;

            public viewHolder(View itemView) {
                super(itemView);
                viewImg=(ImageView)itemView.findViewById(R.id.viewimg);
                viewtext=(TextView)itemView.findViewById(R.id.viewtext);
                mView=itemView;
            }
        }
    }



}
