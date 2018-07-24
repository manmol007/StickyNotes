package com.example.anmol.stickynote;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v7.view.ContextThemeWrapper;
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

    GridView grid;
    DatabaseReference reference;
    MyAdapter adapter;
    String sharedata;
    DatabaseReference mReference;
    Boolean clicked=false;
    ArrayList<data> myData;
    SharedPreferences preferences;
    Long rdate,rtime;
    int flag=0;
    Boolean LongClick=false;
    ActionMode actionMode;
    ArrayList<Integer> index=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        final View view=inflater.inflate(R.layout.trash_layout,container,false);
        preferences=getActivity().getSharedPreferences("user",Context.MODE_PRIVATE);
        mReference= FirebaseDatabase.getInstance().getReference();
        grid=(GridView)view.findViewById(R.id.trash_grid);
        setHasOptionsMenu(true);

        myData=new ArrayList<>();

        reference=mReference.child(preferences.getString("user",null)).child("Trash");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                    data d=snapshot.getValue(data.class);
                    myData.add(d);


                    adapter=new MyAdapter(getActivity(),myData);
                    adapter.notifyDataSetChanged();
                    grid.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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


                final int k=i;

                Boolean flag=false;

                if(LongClick) {
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
                    }
                    else if(index.size()==0){


                        startActivity(new Intent(getActivity(),NavActivity.class));
                        getActivity().overridePendingTransition(0,0);
                    }
                }
                else{

                    Intent intent=new Intent(getActivity(),ADD.class);
                    intent.putExtra("notes",myData.get(i).getNotes());
                    intent.putExtra("font",myData.get(i).getFont());
                    intent.putExtra("color",myData.get(i).getColor());

                    if(myData.get(i).getImageurl()!=null) {
                        intent.putExtra("imageurl",myData.get(i).getImageurl());
                    }
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                                data d=snapshot.getValue(data.class);

                                if(d.getNotes()==myData.get(k).getNotes()){
                                    reference.child(snapshot.getKey()).removeValue();
                                }

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    startActivity(intent);

                }
            }
        });

        return view;
    }


    public class MyAdapter extends BaseAdapter {

        Context ctx;
        ArrayList<data> myData=new ArrayList();


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



            final data d=(data)this.getItem(i);
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
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if(R.id.delete==menuItem.getItemId()) {

                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity())
                        .setTitle("Do you want to save it for further use")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int j) {


                                for (int i = 0; i < index.size(); ++i) {

                                    final String notes = myData.get(index.get(i)).getNotes();

                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                                final DatabaseReference permanentreference = mReference.child(preferences.getString("user", null)).child("permanent").child(snapshot.getKey());

                                                data d = snapshot.getValue(data.class);

                                                if (d.getNotes() == notes) {
                                                    reference.child(snapshot.getKey()).removeValue();
                                                    permanentreference.child("notes").setValue(d.getNotes());
                                                    permanentreference.child("font").setValue(d.getFont());
                                                    permanentreference.child("color").setValue(d.getColor());
                                                    if (d.getImageurl() != null) {
                                                        permanentreference.child("imageurl").setValue(d.getImageurl());
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
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int j) {

                                for (int i = 0; i < index.size(); ++i) {

                                    final String notes = myData.get(index.get(i)).getNotes();

                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                                data d = snapshot.getValue(data.class);

                                                if (d.getNotes() == notes) {
                                                    reference.child(snapshot.getKey()).removeValue();
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
                        });
                AlertDialog dialog=builder.create();
                dialog.show();

            }
            else if(menuItem.getItemId()==R.id.restore){

                final DatabaseReference restore = mReference.child(preferences.getString("user", null)).child("notes").push();

                for (int i = 0; i < index.size(); ++i) {

                    final String notes = myData.get(index.get(i)).getNotes();

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                data d = snapshot.getValue(data.class);

                                if (d.getNotes() == notes) {
                                    reference.child(snapshot.getKey()).removeValue();
                                    restore.child("notes").setValue(d.getNotes());
                                    restore.child("font").setValue(d.getFont());
                                    restore.child("color").setValue(d.getColor());
                                    if (d.getImageurl() != null) {
                                        restore.child("imageurl").setValue(d.getImageurl());
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
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

            actionMode.finish();
            startActivity(new Intent(getActivity(),NavActivity.class));
            getActivity().overridePendingTransition(0,0);

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.deletemainmenu,menu);
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
        else if(R.id.permanemtrestore==item.getItemId()){
            final DatabaseReference restore = mReference.child(preferences.getString("user", null)).child("permanent");

                restore.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            data d = snapshot.getValue(data.class);

                            DatabaseReference ref=mReference.child(preferences.getString("user",null)).child("notes").child(snapshot.getKey());
                            restore.child(snapshot.getKey()).removeValue();
                            ref.child("notes").setValue(d.getNotes());
                                ref.child("font").setValue(d.getFont());
                                ref.child("color").setValue(d.getColor());
                                if (d.getImageurl() != null) {
                                    ref.child("imageurl").setValue(d.getImageurl());
                                }
                                startActivity(new Intent(getActivity(), NavActivity.class));
                                getActivity().overridePendingTransition(0, 0);
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
