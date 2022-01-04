package com.example.finalexam;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.finalexam.databinding.FragmentMyChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class MyChatFragment extends Fragment {

    FragmentMyChatBinding binding;
    List<Chats> chats= new ArrayList<>();
    List<Chats> chatsInitial= new ArrayList<>();
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    String TAG = "vidit";
    public MyChatFragment() {
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
        binding = FragmentMyChatBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("My Chats");

        chats.clear();
        db.collection("chat").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    List<String> allChats = (List<String>)task.getResult().get("allchats");
                    if(allChats!=null) {
                        for (String s : allChats) {
                            db.collection("chat").document(mAuth.getCurrentUser().getUid()).collection(s).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        chatsInitial.clear();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String msg = (String) document.get("msg");
                                            Timestamp time = (Timestamp) document.get("time");
                                            String id = document.getId();
                                            chatsInitial.add(new Chats(s, msg, time, id));
                                        }
                                        chatsInitial.sort(new Comparator<Chats>() {
                                            @Override
                                            public int compare(Chats o1, Chats o2) {
                                                return o2.Time.compareTo(o1.Time);
                                            }
                                        });
                                        chats.add(chatsInitial.get(0));
                                    } else {
                                        //alert error
                                    }
                                    binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                    binding.recyclerView.setAdapter(new MyChatAdapter());
                                }
                            });
                        }

                    }
                }
            }
        });


        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                mListener.logout();
            }
        });
        binding.buttonNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Goto New Chat Fragment
                mListener.gotoNewChat();
            }
        });
    }
    public class MyChatAdapter extends RecyclerView.Adapter<MyChatHolder>{

        @NonNull
        @Override
        public MyChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view  = LayoutInflater.from(getContext()).inflate(R.layout.my_chat_layout,parent,false);
            MyChatHolder holder = new MyChatHolder(view);

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyChatHolder holder, int position) {
            final Uri[] uri = new Uri[1];
            final String[] name = new String[1];
            Chats chat = chats.get(position);
            db.collection("chat").document("users").collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for(QueryDocumentSnapshot documentSnapshots: task.getResult()){
                        if(documentSnapshots.get("userid").equals(chat.Name)){
                            name[0] = documentSnapshots.get("name").toString();
                            holder.textViewName.setText(name[0]);
                            try{
                                uri[0] = Uri.parse(documentSnapshots.get("uri").toString());
                                Picasso.get().load(uri[0]).into(holder.imageViewChatPhoto);
                            }
                            catch(Exception e){
                                Log.d(TAG, "onComplete: No Image");
                            }
                            break;
                        }
                    }
                }
            });
            holder.textViewLastMsg.setText(chat.LastMsg);
            SimpleDateFormat sfd = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
            holder.textViewTime.setText(sfd.format(chat.Time.toDate()));
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Goto the particular chat page
                    mListener.gotoChatFragment(chat.Name);
                }
            });

            holder.imageViewChatPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView image = new ImageView(getContext());
                    Picasso.get().load(uri[0]).into(image);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(name[0])
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
            return chats.size();
        }
    }
    public class MyChatHolder extends RecyclerView.ViewHolder{

        TextView textViewName;
        TextView textViewLastMsg;
        TextView textViewTime;
        ImageView imageViewChatPhoto;
        View view;
        public MyChatHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewLastMsg = itemView.findViewById(R.id.textViewLastMsg);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            imageViewChatPhoto = itemView.findViewById(R.id.imageViewMyChatPhoto);
            view = itemView;
        }
    }
    MyChatListener mListener;
    public interface MyChatListener{
        void logout();
        void gotoNewChat();
        void gotoChatFragment(String id);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MyChatListener){
            mListener = (MyChatListener) context;
        }
    }
}