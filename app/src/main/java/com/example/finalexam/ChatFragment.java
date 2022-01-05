package com.example.finalexam;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.finalexam.databinding.FragmentChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    String TAG = "vidit";
    private String mUserID;
    FirebaseFirestore db;
    FragmentChatBinding binding;
    private FirebaseAuth mAuth;
    List<Chats> chats = new ArrayList<>();
    Uri uriMe;

    public ChatFragment() {
        // Required empty public constructor
    }


    public static ChatFragment newInstance(String param1) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserID = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        binding = FragmentChatBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    void setupList(){
        chats.clear();
        db.collection("chat").document(mAuth.getCurrentUser().getUid()).collection(mUserID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document: task.getResult()){
                        String msg = (String)document.get("msg");
                        String user = (String)document.get("user");
                        Timestamp ts = (Timestamp) document.get("time");
                        String id = document.getId();
                        chats.add(new Chats(user,msg,ts,id));
                    }
                    chats.sort(new Comparator<Chats>() {
                        @Override
                        public int compare(Chats o1, Chats o2) {
                            return o1.Time.compareTo(o2.Time);
                        }
                    });
                    binding.recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.recyclerViewChat.getLayoutManager().scrollToPosition(chats.size()-1);
                    binding.recyclerViewChat.setAdapter(new UserChatApdapter());
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.chat_menu_layout,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menuButtonBlock){

        }
        else if(item.getItemId() == R.id.menuButtonDelete){
            CollectionReference senderMsg = db.collection("chat").document(mAuth.getCurrentUser().getUid()).collection(mUserID);
            CollectionReference receiverMsg = db.collection("chat").document(mUserID).collection(mAuth.getCurrentUser().getUid());

            DocumentReference senderArray = db.collection("chat").document(mAuth.getCurrentUser().getUid());
            DocumentReference receiverArray = db.collection("chat").document(mUserID);

            senderMsg.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot document: task.getResult()) {
                            senderMsg.document(document.getId()).delete();
                        }
                    }
                    else{
                        Log.d(TAG, "onComplete: "+ task.getException().getMessage());
                    }
                }
            });

            receiverMsg.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot document: task.getResult()){
                            receiverMsg.document(document.getId()).delete();
                        }
                    }
                    else{
                        Log.d(TAG, "onComplete: "+ task.getException().getMessage());
                    }
                }
            });

            senderArray.update("allchats",FieldValue.arrayRemove(mUserID));
            receiverArray.update("allchats",FieldValue.arrayRemove(mAuth.getCurrentUser().getUid()));
            mListener.back();
        }
        return true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        db.collection("chat").document("users").collection("users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    uriMe = Uri.parse(task.getResult().get("uri").toString());
                }
                else{
                    uriMe = null;
                }
            }
        });
        setupList();

        binding.buttonChatClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.back();
            }
        });

        binding.buttonChatSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = binding.editTextMsg.getText().toString();
                if(msg == null && msg.length()==0){
                    //Alert for msg
                }
                else{

                    DocumentReference dr1 = db.collection("chat").document(mAuth.getCurrentUser().getUid()).collection(mUserID).document();
                    DocumentReference dr2 = db.collection("chat").document(mUserID).collection(mAuth.getCurrentUser().getUid()).document();

                    HashMap<String,Object> map = new HashMap<>();
                    map.put("user",mAuth.getCurrentUser().getUid());
                    map.put("msg",msg);
                    map.put("time",Timestamp.now());

                    dr1.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                dr2.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            binding.editTextMsg.setText("");
                                            mListener.refreshChat(mUserID);
                                        }
                                    }
                                });
                            }
                            else{
                                //Alert Receive error
                            }
                        }
                    });
                }
            }
        });

    }

    public class UserChatApdapter extends RecyclerView.Adapter<UserChatHolder>{

        @NonNull
        @Override
        public UserChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.chat_layout,parent,false);
            UserChatHolder holder = new UserChatHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull UserChatHolder holder, int position) {
            Chats chat = chats.get(position);
            if(mAuth.getCurrentUser().getUid().equals(chat.Name)){
                holder.textViewChatName.setText("Me");
                try{
                    Picasso.get().load(uriMe).into(holder.imageViewChatPhoto);
                }
                catch(Exception e){
                    Log.d(TAG, "onComplete: No Image");
                }
            }
            else{
                db.collection("chat").document("users").collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot documentSnapshots: task.getResult()){
                            if(documentSnapshots.get("userid").equals(chat.Name)){
                                try{
                                    Uri uri = Uri.parse(documentSnapshots.get("uri").toString());
                                    Picasso.get().load(uri).into(holder.imageViewChatPhoto);
                                }
                                catch(Exception e){
                                    Log.d(TAG, "onComplete: No Image");
                                }
                                holder.textViewChatName.setText(documentSnapshots.get("name").toString());
                                getActivity().setTitle("Chat - " + documentSnapshots.get("name").toString());
                                break;
                            }
                        }
                    }
                });

                holder.imageButtonChatDelete.setVisibility(View.INVISIBLE);
            }
            holder.textViewChatMsg.setText(chat.LastMsg);
            SimpleDateFormat sfd = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
            holder.textViewChatTime.setText(sfd.format(chat.Time.toDate()));

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("ALert!!!");
            builder.setMessage("Do you really want to delete Message")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            db.collection("chat").document(mAuth.getCurrentUser().getUid()).collection(mUserID).document(chat.id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mListener.refreshChat(mUserID);
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

            AlertDialog dialog = builder.create();

            holder.imageButtonChatDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Alert and Delete Msg
                    dialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return chats.size();
        }
    }
    public class UserChatHolder extends RecyclerView.ViewHolder{

        ImageButton imageButtonChatDelete;
        TextView textViewChatTime;
        TextView textViewChatName;
        TextView textViewChatMsg;
        ImageView imageViewChatPhoto;
        public UserChatHolder(@NonNull View itemView) {
            super(itemView);
            textViewChatMsg = itemView.findViewById(R.id.textViewChatMsg);
            textViewChatName = itemView.findViewById(R.id.textViewChatName);
            textViewChatTime = itemView.findViewById(R.id.textViewChatTime);
            imageButtonChatDelete = itemView.findViewById(R.id.imageButtonChatDelete);
            imageViewChatPhoto = itemView.findViewById(R.id.imageViewChatPhoto);
        }
    }

    public interface ChatListener{
        void back();
        void refreshChat(String id);
    }
    ChatListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof ChatListener){
            mListener = (ChatListener) context;
        }
    }
}