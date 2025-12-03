package com.carpoolingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.carpoolingapp.R;
import com.carpoolingapp.adapters.ConversationAdapter;
import com.carpoolingapp.models.Conversation;
import com.carpoolingapp.utils.FirebaseHelper;
import com.carpoolingapp.utils.SharedPrefsHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private BottomNavigationView bottomNav;
    private View emptyState;

    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversationList;
    private FirebaseHelper firebaseHelper;
    private SharedPrefsHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        firebaseHelper = FirebaseHelper.getInstance();
        prefsHelper = new SharedPrefsHelper(this);

        initViews();
        setupBottomNav();
        setupRecyclerView();
        loadConversations();
    }

    private void initViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        bottomNav = findViewById(R.id.bottomNav);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_messages);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_create) {
                startActivity(new Intent(this, CreateRideActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_messages) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        conversationList = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(this, conversationList,
                new ConversationAdapter.OnConversationClickListener() {
                    @Override
                    public void onConversationClick(Conversation conversation) {
                        // Open chat with this person
                        Intent intent = new Intent(MessagesActivity.this, ChatActivity.class);
                        intent.putExtra("otherUserId", conversation.getOtherUserId());
                        intent.putExtra("otherUserName", conversation.getOtherUserName());
                        intent.putExtra("conversationId", conversation.getConversationId());
                        intent.putExtra("rideId", conversation.getRideId());
                        intent.putExtra("from", conversation.getFromLocation());
                        intent.putExtra("to", conversation.getToLocation());
                        startActivity(intent);
                    }
                });

        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(conversationAdapter);
    }

    private void loadConversations() {
        String currentUserId = prefsHelper.getUserId();
        if (currentUserId == null) {
            showEmptyState();
            return;
        }

        // Load conversations from Firebase
        firebaseHelper.getDatabase().child("conversations").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        conversationList.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Conversation conversation = snapshot.getValue(Conversation.class);
                            if (conversation != null) {
                                conversation.setConversationId(snapshot.getKey());
                                conversationList.add(conversation);
                            }
                        }

                        // Sort by last message time (most recent first)
                        Collections.sort(conversationList, new Comparator<Conversation>() {
                            @Override
                            public int compare(Conversation c1, Conversation c2) {
                                return Long.compare(c2.getLastMessageTime(), c1.getLastMessageTime());
                            }
                        });

                        conversationAdapter.notifyDataSetChanged();

                        if (conversationList.isEmpty()) {
                            showEmptyState();
                        } else {
                            hideEmptyState();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        showEmptyState();
                    }
                });
    }

    private void showEmptyState() {
        if (emptyState != null && messagesRecyclerView != null) {
            emptyState.setVisibility(View.VISIBLE);
            messagesRecyclerView.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (emptyState != null && messagesRecyclerView != null) {
            emptyState.setVisibility(View.GONE);
            messagesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_messages);
        }
        // Reload conversations when returning to this screen
        loadConversations();
    }
}