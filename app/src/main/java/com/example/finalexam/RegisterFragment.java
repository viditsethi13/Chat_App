package com.example.finalexam;

import static android.app.Activity.RESULT_OK;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import com.example.finalexam.databinding.FragmentRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class RegisterFragment extends Fragment {
    FragmentRegisterBinding binding;
    public static final String TAG = "vidit";
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private StorageReference mStorageRef;
    private StorageTask mUploadTask;
    Bitmap bitmap = null;

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

    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK)
            {
                bitmap = (Bitmap) result.getData().getExtras().get("data");
                binding.imageViewPhoto.setImageBitmap(bitmap);
            }
        }
    });

    protected void upload(Bitmap bitmap){
        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()+".JPEG");
            mUploadTask = fileReference.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    db = FirebaseFirestore.getInstance();
                                    HashMap<String,Object> map = new HashMap<>();
                                    map.put("uri",uri.toString());
                                    db.collection("chat").document("users").collection("users").document(mAuth.getCurrentUser().getUid()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: "+ e.getMessage());
                                }
                            });
                            Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getActivity(),MainActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            Toast.makeText(getContext(), "Photo not Clicked", Toast.LENGTH_SHORT).show();
        }
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

        binding.buttonPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startForResult.launch(cameraIntent);
                }
            }
        });

        binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName, userEmail, userPassword;
                userName = binding.editTextRegisterName.getText().toString();
                userEmail = binding.editTextRegisterEmail.getText().toString();
                userPassword = binding.editTextRegisterPassoword.getText().toString();

                if(!userName.isEmpty() && !userEmail.isEmpty() && !userPassword.isEmpty() && bitmap!=null){
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
                                                                upload(bitmap);
                                                                HashMap<String,String> map = new HashMap<>();
                                                                map.put("Name","Name");
                                                                db.collection("chat").document(mAuth.getCurrentUser().getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){

                                                                        }
                                                                        else{
                                                                            //document error
                                                                        }
                                                                    }
                                                                });
                                                                if(mUploadTask !=null && mUploadTask.isInProgress()){

                                                                }
                                                                else{
                                                                    Intent intent = new Intent(getActivity(),MainActivity.class);
                                                                    startActivity(intent);
                                                                    getActivity().finish();
                                                                }
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startForResult.launch(cameraIntent);
            }
            else
            {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}