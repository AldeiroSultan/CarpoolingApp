package com.carpoolingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.carpoolingapp.R;
import com.carpoolingapp.adapters.MessageAdapter;
import com.carpoolingapp.models.Conversation;
import com.carpoolingapp.models.Message;
import com.carpoolingapp.utils.FirebaseHelper;
import com.carpoolingapp.utils.SharedPrefsHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private MaterialButton completeBookingButton;
    private MaterialCardView demoInfoCard;
    private TextView otherUserNameText;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private SharedPrefsHelper prefsHelper;
    private FirebaseHelper firebaseHelper;
    private Handler handler;

    private String otherUserId = "bot_id";
    private String otherUserName = "Driver";
    private String conversationId;
    private String rideId;
    private String fromLocation;
    private String toLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        prefsHelper = new SharedPrefsHelper(this);
        firebaseHelper = FirebaseHelper.getInstance();
        handler = new Handler();

        // Get data from intent
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");
        conversationId = getIntent().getStringExtra("conversationId");
        rideId = getIntent().getStringExtra("rideId");
        fromLocation = getIntent().getStringExtra("from");
        toLocation = getIntent().getStringExtra("to");

        if (otherUserId == null) otherUserId = "bot_id";
        if (otherUserName == null) otherUserName = "Driver";
        if (conversationId == null) {
            conversationId = generateConversationId(prefsHelper.getUserId(), otherUserId);
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        // Check if demo mode
        boolean isDemo = getIntent().getBooleanExtra("isDemo", false);
        if (isDemo) {
            showDemoMode();
        }

        // Add welcome message
        addSystemMessage("Chat started. Say hello!");
    }

    private void initViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        completeBookingButton = findViewById(R.id.completeBookingButton);
        demoInfoCard = findViewById(R.id.demoInfoCard);
        otherUserNameText = findViewById(R.id.otherUserName);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(otherUserName);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add home icon to toolbar
        menu.add(0, R.id.action_home, 0, "Home")
                .setIcon(android.R.drawable.ic_menu_view)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_home) {
            goToHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, prefsHelper.getUserId());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());

        if (completeBookingButton != null) {
            completeBookingButton.setOnClickListener(v -> completeBooking());
        }

        if (otherUserNameText != null) {
            otherUserNameText.setText(otherUserName);
        }
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Add user's message
        String currentUserId = prefsHelper.getUserId();
        String currentUserName = prefsHelper.getUserName();

        Message userMessage = new Message(
                conversationId,
                currentUserId,
                currentUserName,
                otherUserId,
                messageText
        );
        messageList.add(userMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        messagesRecyclerView.scrollToPosition(messageList.size() - 1);

        // Save conversation to Firebase
        saveConversation(messageText);

        // Clear input
        messageEditText.setText("");

        // Trigger auto-reply after delay
        handler.postDelayed(() -> sendAutoReply(messageText), 1000);
    }

    private void sendAutoReply(String userMessage) {
        String reply = generateAutoReply(userMessage);

        Message botMessage = new Message(
                conversationId,
                otherUserId,
                otherUserName,
                prefsHelper.getUserId(),
                reply
        );
        messageList.add(botMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        messagesRecyclerView.scrollToPosition(messageList.size() - 1);

        // Update conversation with bot's reply
        saveConversation(reply);
    }

    private void saveConversation(String lastMessage) {
        String currentUserId = prefsHelper.getUserId();
        if (currentUserId == null) return;

        Conversation conversation = new Conversation(
                conversationId,
                otherUserId,
                otherUserName,
                lastMessage,
                System.currentTimeMillis(),
                rideId,
                fromLocation != null ? fromLocation : "Unknown",
                toLocation != null ? toLocation : "Unknown"
        );

        // Save to current user's conversations
        firebaseHelper.getDatabase()
                .child("conversations")
                .child(currentUserId)
                .child(conversationId)
                .setValue(conversation);
    }

    private String generateConversationId(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) {
            return "conv_" + System.currentTimeMillis();
        }
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    private String generateAutoReply(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        if (lowerMessage.contains("hi") || lowerMessage.contains("hello") || lowerMessage.contains("hey")) {
            return "Hi! How can I help you with the ride?";
        } else if (lowerMessage.contains("thanks") || lowerMessage.contains("thank you")) {
            return "You're welcome! 😊";
        } else if (lowerMessage.contains("where")) {
            return "I'll pick you up at the agreed location.";
        } else if (lowerMessage.contains("when") || lowerMessage.contains("time")) {
            return "We'll meet at the scheduled time. I'll be there!";
        } else if (lowerMessage.contains("price") || lowerMessage.contains("cost")) {
            return "The price is as listed in the booking details.";
        } else if (lowerMessage.contains("cancel")) {
            return "If you need to cancel, please do so through the app.";
        } else if (lowerMessage.contains("yes")) {
            return "Great! See you then!";
        } else if (lowerMessage.contains("no")) {
            return "No problem. Let me know if you change your mind!";
        } else if (lowerMessage.contains("ok") || lowerMessage.contains("okay")) {
            return "Perfect! 👍";
        } else if (lowerMessage.contains("bye")) {
            return "Goodbye! Have a great day!";
        } else {
            return "Got it! If you have any questions, just ask.";
        }
    }

    private void addSystemMessage(String text) {
        Message systemMessage = new Message(
                conversationId,
                "system",
                "System",
                prefsHelper.getUserId(),
                text
        );
        messageList.add(systemMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
    }

    private void showDemoMode() {
        if (demoInfoCard != null) {
            demoInfoCard.setVisibility(View.VISIBLE);
        }

        if (completeBookingButton != null) {
            completeBookingButton.setVisibility(View.VISIBLE);
        }
    }

    private void completeBooking() {
        Intent intent = new Intent(ChatActivity.this, RideConfirmationActivity.class);
        intent.putExtra("confirmationType", "booking");
        intent.putExtra("isDemo", true);
        startActivity(intent);
        finish();
    }

    private void goToHome() {
        Intent intent = new Intent(ChatActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}