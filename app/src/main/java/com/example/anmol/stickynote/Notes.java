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
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import static android.graphics.Typeface.createFromAsset;

public class Notes extends Fragment {

    FloatingActionMenu menu;

    com.github.clans.fab.FloatingActionButton add;
    String notes;
    DatabaseReference reference;
    MyAdapter adapter;
    Intent intent;
    ViewAdapter dataadapter;
    Intent sharingIntent;
    String sharedata;
    DatabaseReference mReference,ArchiveReference,TrashReference;
    Boolean clicked=false;
    String key;
    ArrayList<data> viewData,setData;
    ActionMode actionMode;
    Boolean bool=false;
    ArrayList<String> myData;
    Long result;
    SharedPreferences preferences;
    String rdate,rtime;
    int flag=0;
    ArrayList<String> category;
    int settype=0;
    Boolean LongClick=false;
    RecyclerView list;
    ArrayList<Uri> ImageUri=new ArrayList<>();
    ArrayList<String> index=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        final View view=inflater.inflate(R.layout.notes_layout,container,false);
        myData=new ArrayList();
        mReference= FirebaseDatabase.getInstance().getReference();
        preferences=getActivity().getSharedPreferences("user",Context.MODE_PRIVATE);
        menu=(FloatingActionMenu)view.findViewById(R.id.menu);
        add=(com.github.clans.fab.FloatingActionButton)view.findViewById(R.id.add);
        list=(RecyclerView)view.findViewById(R.id.list);
        setData=new ArrayList();
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        reference=mReference.child(preferences.getString("user",null)).child("notes");

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
                Log.i("myDataa",myData.toString());

                adapter=new MyAdapter(getActivity(),myData);
                adapter.notifyDataSetChanged();
                list.setAdapter(adapter);
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
                menu.close(true);
            }
        });

        setHasOptionsMenu(true);

        return view;
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

                    final DatabaseReference permanentreference = mReference.child(preferences.getString("user", null)).child("Trash").push();

                  final String mnotes = index.get(i);

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                data d = snapshot.getValue(data.class);

                                if (d.getNotes().equals(mnotes)) {

                                    permanentreference.push();

                                    FirebaseDatabase.getInstance().getReference().child(preferences.getString("user",null)).child("starred").child(snapshot.getKey()).removeValue();
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
            else if(R.id.archive==menuItem.getItemId()){

                for(int i=0;i<index.size();++i){

                     final String mnotes=index.get(i);

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
                                    ArchiveReference.child("category").setValue(d.getCategory());
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

            holder.addcategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(getActivity(),ADD.class);
                    intent.putExtra("category",holder.category.getText().toString());
                    startActivity(intent);
                }
            });

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
            ImageView addcategory;

            public MyHolder(View itemView) {
                super(itemView);
                category=(TextView)itemView.findViewById(R.id.category);
                categorylist=(RecyclerView)itemView.findViewById(R.id.viewlist);
                addcategory=(ImageView)itemView.findViewById(R.id.addcategory);
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

                         reference.addListenerForSingleValueEvent(new ValueEventListener() {
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
            holder.starred.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot snapshot:dataSnapshot.getChildren())
                            {
                                final String vNotes = holder.viewtext.getText().toString();
                                data d=snapshot.getValue(data.class);
                                if(d.getNotes().equals(vNotes)){
                                    bool = snapshot.hasChild("starred");

                                    if (!bool) {
                                        holder.starred.setImageResource(R.drawable.starredpink);
                                        holder.starred.setTag(R.drawable.starredpink);

                                        DatabaseReference starredDatabase = mReference.child(preferences.getString("user", null)).child("starred").child(snapshot.getKey());
                                        starredDatabase.child("notes").setValue(d.getNotes());
                                        starredDatabase.child("category").setValue(d.getCategory());
                                        starredDatabase.child("color").setValue(d.getColor());
                                        starredDatabase.child("font").setValue(d.getFont());
                                        if (d.getImageurl() != null) {
                                            starredDatabase.child("imageurl").setValue(d.getImageurl());
                                            starredDatabase.child("imageuri").setValue(d.getImageuri());
                                        }

                                        reference.child(snapshot.getKey()).child("starred").setValue("true");
                                    }
                                    else{
                                        holder.starred.setImageResource(R.drawable.starred);
                                        holder.starred.setTag(R.drawable.starred);
                                        final DatabaseReference starredDatabase2 = mReference.child(preferences.getString("user", null)).child("starred");
                                        starredDatabase2.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                                    data d=snapshot.getValue(data.class);
                                                    if(d.getNotes().equals(vNotes)){
                                                        starredDatabase2.child(snapshot.getKey()).removeValue();
                                                        reference.child(snapshot.getKey()).child("starred").removeValue();
                                                        break;
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });


            if (viewData.get(position).getStarred()!=null){
                holder.starred.setImageResource(R.drawable.starredpink);
            }
            else{
                holder.starred.setImageResource(R.drawable.starred);
            }


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
            ImageView starred;
            View mView;

            public viewHolder(View itemView) {
                super(itemView);
                starred=(ImageView)itemView.findViewById(R.id.starred);
                viewImg=(ImageView)itemView.findViewById(R.id.viewimg);
                viewtext=(TextView)itemView.findViewById(R.id.viewtext);
                mView=itemView;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.notesmenu, menu);
        MenuItem mSearch = menu.findItem(R.id.search);
        android.support.v7.widget.SearchView mSearchView = (android.support.v7.widget.SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Search your notes");
        mSearchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                list.setVisibility(View.INVISIBLE);

                setData.clear();

                for(int i=0;i<viewData.size();++i){

                    if(viewData.get(i).getNotes().indexOf(query)!=-1){
                        setData.add(viewData.get(i));
                    }
                }


                return true;

            }

            @Override
            public boolean onQueryTextChange(String newText) {


                return true;
            }

        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.starred){

            Fragment f=new Starred();
            getFragmentManager().beginTransaction().replace(R.id.layout,f).commit();
        }
        else if(item.getItemId()==R.id.signout){

            FirebaseAuth.getInstance().signOut();
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

            mGoogleSignInClient.signOut();

            startActivity(new Intent(getActivity(),MainActivity.class));
        }

        return true;
    }
}
