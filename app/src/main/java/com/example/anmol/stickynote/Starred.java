package com.example.anmol.stickynote;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.graphics.Typeface.createFromAsset;

public class Starred extends Fragment {

    GridView grid;
    DatabaseReference reference;
    SharedPreferences preferences;
    MyAdapter adapter;
    ArrayList<data> list;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.gridlayout,container,false);
        preferences=getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        grid=(GridView)view.findViewById(R.id.grid);
        list=new ArrayList();
        reference= FirebaseDatabase.getInstance().getReference().child(preferences.getString("user",null)).child("starred");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    data d=snapshot.getValue(data.class);
                    list.add(d);
                }
                adapter=new MyAdapter(getActivity(),list);
                adapter.notifyDataSetChanged();
                grid.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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


}
