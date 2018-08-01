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
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import static android.graphics.Typeface.SANS_SERIF;
import static android.graphics.Typeface.SERIF;
import static android.graphics.Typeface.createFromAsset;

public class Trash extends Fragment {

    DatabaseReference reference;
    MyAdapter adapter;
    Intent intent;
    ViewAdapter dataadapter;
    DatabaseReference mReference;
    Boolean clicked=false;
    ArrayList<String> index=new ArrayList<>();
    ArrayList<data> viewData;
    ActionMode actionMode;
    ArrayList<String> myData;
    SharedPreferences preferences;
    ArrayList<String> category;
    Boolean LongClick=false;
    RecyclerView list;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        final View view=inflater.inflate(R.layout.trash_layout,container,false);
        myData=new ArrayList();
        mReference= FirebaseDatabase.getInstance().getReference();
        preferences=getActivity().getSharedPreferences("user",Context.MODE_PRIVATE);
        list=(RecyclerView)view.findViewById(R.id.list);
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        reference=mReference.child(preferences.getString("user",null)).child("Trash");

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

        setHasOptionsMenu(true);

        return view;
    }

    @SuppressLint("ValidFragment")
    public class ActionBarCallBack implements ActionMode.Callback{

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

            actionMode.getMenuInflater().inflate(R.menu.deletemenu,menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {

            if(R.id.delete==menuItem.getItemId()) {

                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity())
                        .setTitle("Do you want to save it for further use")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int j) {


                                for (int i = 0; i < index.size(); ++i) {

                                     final String mnotes = index.get(i);

                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                                final DatabaseReference permanentreference = mReference.child(preferences.getString("user", null)).child("permanent").child(snapshot.getKey());

                                                data d = snapshot.getValue(data.class);

                                                if (d.getNotes().equals(mnotes)) {
                                                    reference.child(snapshot.getKey()).removeValue();
                                                    permanentreference.child("notes").setValue(d.getNotes());
                                                    permanentreference.child("font").setValue(d.getFont());
                                                    permanentreference.child("category").setValue(d.getCategory());
                                                    permanentreference.child("color").setValue(d.getColor());
                                                    if (d.getImageurl() != null) {
                                                        permanentreference.child("imageuri").setValue(d.getImageuri());
                                                        permanentreference.child("imageurl").setValue(d.getImageurl());
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
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int j) {

                                for (int i = 0; i < index.size(); ++i) {

                                    final String notes = index.get(i);

                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                                data d = snapshot.getValue(data.class);

                                                if (d.getNotes().equals( notes)) {
                                                    reference.child(snapshot.getKey()).removeValue();
                                                    actionMode.finish();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });


                                }
                            }

                        });
                AlertDialog dialog=builder.create();
                dialog.show();

            }
            else if(menuItem.getItemId()==R.id.restore){


                for (int i = 0; i < index.size(); ++i) {

                    final DatabaseReference restore = mReference.child(preferences.getString("user", null)).child("notes").push();

                    final String notes = index.get(i);

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                data d = snapshot.getValue(data.class);

                                if (d.getNotes().equals(notes)) {
                                    reference.child(snapshot.getKey()).removeValue();
                                    restore.child("notes").setValue(d.getNotes());
                                    restore.child("font").setValue(d.getFont());
                                    restore.child("category").setValue(d.getCategory());
                                    restore.child("color").setValue(d.getColor());
                                    if (d.getImageurl() != null) {
                                        restore.child("imageurl").setValue(d.getImageurl());
                                        restore.child("imageuri").setValue(d.getCategory());
                                    }
                                    startActivity(new Intent(getActivity(), NavActivity.class));
                                    getActivity().overridePendingTransition(0, 0);
                                }
                            }
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.deletemainmenu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(R.id.permanemtrestore==item.getItemId()){
            final DatabaseReference restore = mReference.child(preferences.getString("user", null)).child("permanent");

            restore.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        data d = snapshot.getValue(data.class);

                        DatabaseReference ref=mReference.child(preferences.getString("user",null)).child("notes").child(snapshot.getKey());
                        restore.child(snapshot.getKey()).removeValue();
                        ref.child("notes").setValue(d.getNotes());
                        ref.child("font").setValue(d.getFont());
                        ref.child("color").setValue(d.getColor());
                        ref.child("category").setValue(d.getCategory());
                        if (d.getImageurl() != null) {
                            ref.child("imageurl").setValue(d.getImageurl());
                            ref.child("imageuri").setValue(d.getImageuri());
                        }
                        startActivity(new Intent(getActivity(),NavActivity.class));
                        getActivity().overridePendingTransition(0,0);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        return true;
    }
}

