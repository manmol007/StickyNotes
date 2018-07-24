package com.example.anmol.stickynote;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

import static android.graphics.Typeface.createFromAsset;

public class ADD extends AppCompatActivity {


    android.support.v7.widget.Toolbar toolbar;
    EditText text;
    DatabaseReference add;
    String str;
    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    SharedPreferences preferences;
    ImageView font,color;
    ImageView image;
    String colorget;
    String fontget;
    Uri link=null;
    ImageView preview;
    Uri img=null;
    Uri uri=null;
    Bitmap url=null;
    Intent intent;
    public static final int REQ=1;
    int flag=0;
    StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        text=(EditText)findViewById(R.id.text);
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        preferences=getSharedPreferences("user",MODE_PRIVATE);
        image=(ImageView) findViewById(R.id.image);
        font=(ImageView) findViewById(R.id.fontspin);
        color=(ImageView) findViewById(R.id.colorspin);
        mStorage= FirebaseStorage.getInstance().getReference();
        preview=(ImageView)findViewById(R.id.preview);
        Typeface type = Typeface.createFromAsset(getAssets(),"chop.TTF");
        text.setTypeface(type);
        fontget="chop";
        colorget="-1";

        intent=getIntent();

        if(intent!=null){

            text.setText(intent.getStringExtra("notes"));
            if(intent.getStringExtra("color")!=null) {
                text.setBackgroundColor(Integer.parseInt(intent.getStringExtra("color")));
                preview.setBackgroundColor(Integer.parseInt(intent.getStringExtra("color")));
            }
            colorget=intent.getStringExtra("color");
            if(intent.getStringExtra("color")!=null){
                flag = 1;
            }
            if(intent.getStringExtra("font")!=null) {
                if (intent.getStringExtra("font").equals("barbaric")) {
                    fontget="barbaric";
                    Typeface typeface = createFromAsset(getAssets(), "barbaric.ttf");
                    text.setTypeface(typeface);
                } else if (intent.getStringExtra("font").equals("bloody")) {
                    Typeface typeface = createFromAsset(getAssets(), "bloody.TTF");
                    text.setTypeface(typeface);
                    fontget="bloody";
                } else if (intent.getStringExtra("font").equals("blox")) {
                    Typeface typeface = createFromAsset(getAssets(), "blox.ttf");
                    text.setTypeface(typeface);
                    fontget="blox";
                } else if (intent.getStringExtra("font").equals("boston")) {
                    Typeface typeface = createFromAsset(getAssets(), "boston.ttf");
                    text.setTypeface(typeface);
                    fontget="boston";
                } else if (intent.getStringExtra("font").equals("charles_s")) {
                    Typeface typeface = createFromAsset(getAssets(), "charles_s.ttf");
                    text.setTypeface(typeface);
                    fontget="charles_s";
                } else if (intent.getStringExtra("font").equals("chop")) {
                    Typeface typeface = createFromAsset(getAssets(), "chop.TTF");
                    text.setTypeface(typeface);
                    fontget="chop";
                } else if (intent.getStringExtra("font").equals("degrassi")) {
                    Typeface typeface = createFromAsset(getAssets(), "degrassi.ttf");
                    text.setTypeface(typeface);
                    fontget="degrassi";
                } else if (intent.getStringExtra("font").equals("delicious")) {
                    Typeface typeface = createFromAsset(getAssets(), "delicious.ttf");
                    text.setTypeface(typeface);
                    fontget="delicious";
                } else if (intent.getStringExtra("font").equals("koshlang")) {
                    Typeface typeface = createFromAsset(getAssets(), "koshlang.ttf");
                    text.setTypeface(typeface);
                    fontget="koshlang";
                } else if (intent.getStringExtra("font").equals("lokicola")) {
                    Typeface typeface = createFromAsset(getAssets(), "lokicola.TTF");
                    text.setTypeface(typeface);
                } else if (intent.getStringExtra("font").equals("nofutur")) {
                    Typeface typeface = createFromAsset(getAssets(), "nofutur.ttf");
                    text.setTypeface(typeface);
                    fontget="nofutur";
                } else if (intent.getStringExtra("font").equals("romantic")) {
                    Typeface typeface = createFromAsset(getAssets(), "romantic.ttf");
                    text.setTypeface(typeface);
                    fontget="romantic";
                }
            }
            if(intent.getStringExtra("imageurl")!=null) {
                preview.getLayoutParams().height = 200;
                img=Uri.parse(intent.getStringExtra("imageuri"));
                Log.i("img",img.toString());
                preview.setImageURI(img);
            }
            }

        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final ColorPicker cp=new ColorPicker(ADD.this,255,255,255);

                cp.show();

                cp.setCallback(new ColorPickerCallback() {
                    @Override
                    public void onColorChosen(int color) {
                        colorget=Integer.toString(color);
                        text.setBackgroundColor(color);
                        preview.setBackgroundColor(color);
                        flag=1;
                        cp.dismiss();
                    }
                });
            }
        });


        font.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder=new AlertDialog.Builder(ADD.this);
                View Dialogview=getLayoutInflater().inflate(R.layout.fontinflate,null);
                builder.setView(Dialogview);

                TextView  barbaric=(TextView)Dialogview.findViewById(R.id.barbaric);
                TextView  bloody=(TextView)Dialogview.findViewById(R.id.bloody);
                TextView  blox=(TextView)Dialogview.findViewById(R.id.blox);
                TextView  boston=(TextView)Dialogview.findViewById(R.id.boston);
                TextView  charles=(TextView)Dialogview.findViewById(R.id.charles);
                TextView  chop=(TextView)Dialogview.findViewById(R.id.chop);
                TextView  degrassi=(TextView)Dialogview.findViewById(R.id.degrassi);
                TextView  delicious=(TextView)Dialogview.findViewById(R.id.delicious);
                TextView  koshlang=(TextView)Dialogview.findViewById(R.id.koshlang);
                TextView  lokicola=(TextView)Dialogview.findViewById(R.id.lokicola);
                TextView  nofutur=(TextView)Dialogview.findViewById(R.id.nofutur);
                TextView  romantic=(TextView)Dialogview.findViewById(R.id.romantic);

                final AlertDialog dialog=builder.create();
                Window window=dialog.getWindow();
                WindowManager.LayoutParams win=window.getAttributes();
                win.gravity=Gravity.BOTTOM;

                barbaric.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fontget="barbaric";
                        Typeface typeface=createFromAsset(getAssets(),"barbaric.ttf");
                        text.setTypeface(typeface);
                        dialog.dismiss();

                    }
                });

                bloody.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fontget="bloody";
                        Typeface typeface=createFromAsset(getAssets(),"bloody.TTF");
                        text.setTypeface(typeface);
                        dialog.dismiss();

                    }
                });
                blox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fontget="blox";
                        Typeface typeface=createFromAsset(getAssets(),"blox.ttf");
                        text.setTypeface(typeface);
                        dialog.dismiss();

                    }
                });
                boston.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fontget="boston";
                        Typeface typeface=createFromAsset(getAssets(),"boston.ttf");
                        text.setTypeface(typeface);
                        dialog.dismiss();

                    }
                });
                charles.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fontget="charles";
                        Typeface typeface=createFromAsset(getAssets(),"charles_s.ttf");
                        text.setTypeface(typeface);
                        dialog.dismiss();

                    }
                });
                chop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fontget="chop";
                        Typeface typeface=createFromAsset(getAssets(),"chop.TTF");
                        text.setTypeface(typeface);
                        dialog.dismiss();

                    }
                });
                degrassi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fontget="degrassi";
                        Typeface typeface=createFromAsset(getAssets(),"degrassi.ttf");
                        text.setTypeface(typeface);
                        dialog.dismiss();

                    }
                });
                delicious.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fontget="delicious";
                        Typeface typeface=createFromAsset(getAssets(),"delicious.ttf");
                        text.setTypeface(typeface);
                        dialog.dismiss();

                    }
                });
                koshlang.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fontget="koshlang";
                        Typeface typeface=createFromAsset(getAssets(),"koshlang.ttf");
                        text.setTypeface(typeface);
                        dialog.dismiss();

                    }
                });
                lokicola.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fontget="lokicola";
                        Typeface typeface=createFromAsset(getAssets(),"lokicola.TTF");
                        text.setTypeface(typeface);
                        dialog.dismiss();

                    }
                });
                nofutur.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fontget="nofutur";
                        Typeface typeface=createFromAsset(getAssets(),"nofutur.ttf");
                        text.setTypeface(typeface);
                        dialog.dismiss();

                    }
                });
                romantic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fontget="romantic";
                        Typeface typeface=createFromAsset(getAssets(),"romantic.ttf");
                        text.setTypeface(typeface);
                        dialog.dismiss();

                    }
                });
                dialog.show();

            }
        });



        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,REQ);

            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            str=text.getText().toString();

            text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    str=text.getText().toString();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null) {
            if (requestCode == REQ && resultCode == RESULT_OK) {
                img = data.getData();
                preview.setImageURI(img);
                BitmapDrawable bd=(BitmapDrawable) preview.getDrawable();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Bitmap bitmap = bd.getBitmap();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
                img= Uri.parse(path);
                preview.setImageURI(img);
                preview.getLayoutParams().height=300;
            }
        }
    }

    @Override
    public void onBackPressed() {

        if(flag==0){

            colorget="-1";
        }

        add = mDatabase.child(preferences.getString("user", null)).child("notes").push();

        if (TextUtils.isEmpty(str)) {
            Toast.makeText(getApplicationContext(), "Fields are empty", Toast.LENGTH_LONG).show();
        } else {

            add.child("notes").setValue(str);
            add.child("color").setValue(colorget);
            add.child("font").setValue(fontget);

        try
        {
                mStorage.child("photos").child(img.getLastPathSegment()).putFile(img).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        link = taskSnapshot.getDownloadUrl();

                        add.child("notes").setValue(str);
                        add.child("color").setValue(colorget);
                        add.child("font").setValue(fontget);
                        add.child("imageurl").setValue(link.toString());
                       Uri uri=null;
                        Bitmap url= null;
                        try {
                            url = new DownloadImagesTask().execute(link.toString()).get();
                            String path = MediaStore.Images.Media.insertImage(getContentResolver(),url ,"Title", null);
                            uri= Uri.parse(path);
                            Log.i("uri",uri.toString());
                            add.child("imageuri").setValue(uri.toString());
                            return;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        startActivity(new Intent(getApplicationContext(),NavActivity.class));
        overridePendingTransition(0,0);
    }
}
