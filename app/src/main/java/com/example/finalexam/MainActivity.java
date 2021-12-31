package com.example.finalexam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements MyChatFragment.MyChatListener, NewChatFragment.NewChatListener, ChatFragment.ChatListener {

    String TAG = "vidit";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        //goto my chat fragment
        getSupportFragmentManager().beginTransaction().add(R.id.container,new MyChatFragment()).commit();
    }

    @Override
    public void logout() {
        Intent intent  = new Intent(this,AuthActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    public void gotoNewChat() {
        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container,new NewChatFragment()).commit();
    }

    @Override
    public void gotoChatFragment(String id) {
        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container,ChatFragment.newInstance(id),"Chat-Fragment").commit();
    }

    @Override
    public void back() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void refreshChat(String id) {
        ChatFragment fragment = (ChatFragment)getSupportFragmentManager().findFragmentByTag("Chat-Fragment");
        if(fragment != null){
            fragment.setupList();
        }
//        getSupportFragmentManager().popBackStack();
    }
}