package com.example.anmol.stickynote;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends Fragment {

    SignInButton google_btn;
    private static final int RC_SIGN_IN=1;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ProgressDialog mDialog;
    private RelativeLayout relativeLayout;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    Boolean flag=false;
    SharedPreferences preferences;
    public final String TAG="Error";
    SharedPreferences.Editor editor;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.login, container, false);
        mDialog = new ProgressDialog(getActivity());
        mAuth = FirebaseAuth.getInstance();
        relativeLayout=(RelativeLayout)view.findViewById(R.id.relativeLayout);
        google_btn = (SignInButton) view.findViewById(R.id.Google_sign_in);
        preferences=getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        editor=preferences.edit();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient=GoogleSignIn.getClient(getActivity(),gso);

       google_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {

                    Log.i("hii","hii");
                    reference=FirebaseDatabase.getInstance().getReference();
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                                Log.i("key",snapshot.getKey());

                                if(snapshot.getKey().equals(firebaseAuth.getUid())){

                                    mDialog.dismiss();
                                    Intent intent=new Intent(getActivity(),NavActivity.class);
                                    editor.putString("user",firebaseAuth.getUid()).apply();
                                    startActivity(intent);
                                }

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        };

        return view;
    }


        private void signIn() {
        mDialog.show();
        mDialog.setMessage("Signing with google");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                mDialog.dismiss();
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                           DatabaseReference mReference=FirebaseDatabase.getInstance().getReference();
                           mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(DataSnapshot dataSnapshot) {

                                   for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                       if(snapshot.getKey().equals(user.getUid())){
                                                flag=true;
                                       }
                                   }
                                   if(flag==true){
                                       Intent intent=new Intent(getActivity(),NavActivity.class);
                                       editor.putString("user",user.getUid()).apply();
                                       startActivity(intent);
                                   }
                                   else{

                                       AlertDialog.Builder builder=new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AppTheme));
                                       final View view=getActivity().getLayoutInflater().inflate(R.layout.password,null);
                                       builder.setView(view);
                                       builder.setTitle("Set your password");
                                       final EditText pass=(EditText)view.findViewById(R.id.Protected);
                                       builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                           @Override
                                           public void onClick(DialogInterface dialogInterface, int i) {

                                               String password=pass.getText().toString();
                                               if(TextUtils.isEmpty(password)){

                                                   mDialog.dismiss();
                                                   Snackbar.make(relativeLayout,"Enter the password to proceed",Snackbar.LENGTH_LONG).show();

                                               }
                                               else{
                                                   reference=FirebaseDatabase.getInstance().getReference().child(user.getUid());
                                                   reference.child("password").setValue(password);
                                                   mDialog.dismiss();
                                                   Intent intent=new Intent(getActivity(),NavActivity.class);
                                                   editor.putString("user",user.getUid()).apply();
                                                   startActivity(intent);
                                               }


                                           }
                                       });

                                       AlertDialog dialog=builder.create();
                                       dialog.show();

                                   }

                               }

                               @Override
                               public void onCancelled(DatabaseError databaseError) {

                               }
                           });

                        } else {
                            // If sign in fails, display a message to the user.
                            mDialog.dismiss();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(),"Authentication Failed",Toast.LENGTH_LONG).show();

                        }

                        // ...
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
      mAuth.addAuthStateListener(authStateListener);
    }

}
