package com.carpoolingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.carpoolingapp.R;
import com.carpoolingapp.models.Conversation;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private Context context;
    private List<Conversation> conversationList;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationAdapter(Context context, List<Conversation> conversationList, OnConversationClickListener listener) {
        this.context = context;
        this.conversationList = conversationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);

        holder.nameText.setText(conversation.getOtherUserName());
        holder.lastMessageText.setText(conversation.getLastMessage());
        holder.routeText.setText(conversation.getFromLocation() + " → " + conversation.getToLocation());

        // Format time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
        String timeStr = sdf.format(new Date(conversation.getLastMessageTime()));
        holder.timeText.setText(timeStr);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, lastMessageText, routeText, timeText;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            lastMessageText = itemView.findViewById(R.id.lastMessageText);
            routeText = itemView.findViewById(R.id.routeText);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
}