package com.carpoolingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.carpoolingapp.R;
import com.carpoolingapp.models.Message;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        // Check if this message was sent by current user
        boolean isSentByMe = message.getSenderId() != null &&
                message.getSenderId().equals(currentUserId);

        if (isSentByMe) {
            // Show sent message (right side)
            holder.sentMessageLayout.setVisibility(View.VISIBLE);
            holder.receivedMessageLayout.setVisibility(View.GONE);
            holder.sentMessageText.setText(message.getMessage());
        } else {
            // Show received message (left side)
            holder.sentMessageLayout.setVisibility(View.GONE);
            holder.receivedMessageLayout.setVisibility(View.VISIBLE);
            holder.receivedMessageText.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout sentMessageLayout, receivedMessageLayout;
        TextView sentMessageText, receivedMessageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sentMessageLayout = itemView.findViewById(R.id.sentMessageLayout);
            receivedMessageLayout = itemView.findViewById(R.id.receivedMessageLayout);
            sentMessageText = itemView.findViewById(R.id.sentMessageText);
            receivedMessageText = itemView.findViewById(R.id.receivedMessageText);
        }
    }
}