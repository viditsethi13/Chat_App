package com.example.finalexam;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.finalexam.databinding.FragmentNewChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class NewChatFragment extends Fragment {

    FragmentNewChatBinding binding;
    List<User> users = new ArrayList<>();
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    String TAG = "vidit";
    String userReceiverID;
    public NewChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        binding = FragmentNewChatBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("New Chat");
        users.clear();

        db.collection("chat").document("users").collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        String name = (String) document.get("name");
                        String userid = (String) document.get("userid");
                        String uri = (String) document.get("uri");
                        if(!mAuth.getCurrentUser().getUid().equals(userid))
                            users.add(new User(name,userid,uri));
                    }
                    binding.recyclerVIewNewChat.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.recyclerVIewNewChat.setAdapter(new NewChatAdapter());
                }
                else{
                    Log.d(TAG, "onComplete: "+ task.getException().getMessage());
                }
            }
        });

        binding.buttonNewChatCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.back();
            }
        });


        binding.buttonNewChatSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.textViewUserName.getText().toString();
                if(username.equals("No User Selected")){
                    //Alert to select user
                }
                else{
                    String msg = binding.editTextNewMsg.getText().toString();
                    if(msg == null || msg.length()==0){
                        //Alert to give msg
                    }
                    else{

                        DocumentReference senderMsg = db.collection("chat").document(mAuth.getCurrentUser().getUid()).collection(userReceiverID).document();
                        DocumentReference receiverMsg = db.collection("chat").document(userReceiverID).collection(mAuth.getCurrentUser().getUid()).document();

                        HashMap<String,Object> map = new HashMap<>();
                        map.put("user",mAuth.getCurrentUser().getUid());
                        map.put("msg",msg);
                        map.put("time",Timestamp.now());


                        senderMsg.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    db.collection("chat").document(mAuth.getCurrentUser().getUid()).update("allchats",FieldValue.arrayUnion(userReceiverID));
                                }
                                else{
                                    Log.d(TAG, "onComplete:  send msg failed");
                                    //send msg failed
                                }
                            }
                        });
                        receiverMsg.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    db.collection("chat").document(userReceiverID).update("allchats",FieldValue.arrayUnion(mAuth.getCurrentUser().getUid()));
                                }
                                else{
                                    Log.d(TAG, "onComplete:  receiver msg failed");
                                    // receiver msg failed
                                }
                            }
                        });

                        db.collection("chat").document(mAuth.getCurrentUser().getUid()).update("allchats",FieldValue.arrayUnion(userReceiverID));
                        db.collection("chat").document(userReceiverID).update("allchats",FieldValue.arrayUnion(mAuth.getCurrentUser().getUid()));

                        mListener.back();
                    }
                }
            }
        });
    }
    public class NewChatAdapter extends RecyclerView.Adapter<NewChatHolder>{

        @NonNull
        @Override
        public NewChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.users_layout,parent,false);
            NewChatHolder holder  = new NewChatHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull NewChatHolder holder, int position) {
            User user = users.get(position);
            holder.textViewUser.setText(user.getName());
            try{
                Uri uri = Uri.parse(user.getUri());
                Picasso.get().load(uri).into(holder.imageViewUserPhoto);
            }
            catch(Exception e){
                Log.d(TAG, "onComplete: No Image");
            }
            holder.textViewUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.textViewUserName.setText(user.getName());
                    userReceiverID = user.UserId;
                }
            });
            holder.imageViewUserPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView image = new ImageView(getContext());
                    Picasso.get().load(user.getUri()).into(image);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(user.getName())
                            .setView(image)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    builder.create().show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }
    }
    public class NewChatHolder extends RecyclerView.ViewHolder{

        TextView textViewUser;
        ImageView imageViewUserPhoto;
        View view;
        public NewChatHolder(@NonNull View itemView) {
            super(itemView);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            imageViewUserPhoto = itemView.findViewById(R.id.imageViewUserPhoto);
            view  = itemView;
        }
    }
    public interface NewChatListener{
        void back();
    }
    NewChatListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof NewChatListener){
            mListener = (NewChatListener) context;
        }
    }
}