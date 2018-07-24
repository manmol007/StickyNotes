package com.example.anmol.stickynote;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends Fragment {

    EditText remail,rpassword;
    Button rlogin;
    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.register,container,false);

        remail=(EditText)view.findViewById(R.id.remail);
        rpassword=(EditText)view.findViewById(R.id.rpassword);
        rlogin=(Button) view.findViewById(R.id.rlogin);

        mAuth=FirebaseAuth.getInstance();


        rlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email=remail.getText().toString();
                String pass=rpassword.getText().toString();

                if(TextUtils.isEmpty(email)||TextUtils.isEmpty(pass)){
                    Toast.makeText(getActivity(),"Empty credentials",Toast.LENGTH_LONG).show();
                }
                else {
                    mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                startActivity(new Intent(getActivity(),NavActivity.class));
                            }
                            else{
                                Toast.makeText(getActivity(),"Error",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        return view;
    }
}
