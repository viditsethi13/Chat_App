package com.example.finalexam;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


import com.example.finalexam.databinding.FragmentRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RegisterFragment extends Fragment {
    FragmentRegisterBinding binding;
    public static final String TAG = "vidit";
    private FirebaseAuth mAuth;
    FirebaseFirestore db;


    AlertDialog.Builder builder;
    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("Create New Account");

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Alert!!!!");
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName, userEmail, userPassword;
                userName = binding.editTextRegisterName.getText().toString();
                userEmail = binding.editTextRegisterEmail.getText().toString();
                userPassword = binding.editTextRegisterPassoword.getText().toString();

                if(!userName.isEmpty() && !userEmail.isEmpty() && !userPassword.isEmpty()){
                    mAuth = FirebaseAuth.getInstance();
                    mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(userName)
                                        .build();
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentReference dr = db.collection("chat").document("users").collection("users").document(mAuth.getCurrentUser().getUid());
                                                    HashMap<String,Object> map = new HashMap<>();
                                                    map.put("name",mAuth.getCurrentUser().getDisplayName());
                                                    map.put("userid",mAuth.getCurrentUser().getUid());
                                                    dr.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                HashMap<String,String> map = new HashMap<>();
                                                                map.put("Name","Name");
                                                                db.collection("chat").document(mAuth.getCurrentUser().getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
//                                                                            db.collection("chat").document(mAuth.getCurrentUser().getUid()).update("allchat", FieldValue.arrayUnion("add"));
                                                                        }
                                                                        else{
                                                                            //document error
                                                                        }
                                                                    }
                                                                });
                                                                Intent intent = new Intent(getActivity(),MainActivity.class);
                                                                startActivity(intent);
                                                                getActivity().finish();
                                                            }
                                                            else{
                                                                //Alert task.msg
                                                                Log.d(TAG, "onComplete: "+ task.getException().getMessage());
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                            }else{
                                //Alert UnSuccessful
                                builder.setMessage(task.getException().getMessage());
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        }
                    });
                }
                else{
                    //Alert Empty
                    builder.setMessage("Please enter Data...");
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });

        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

    }
}