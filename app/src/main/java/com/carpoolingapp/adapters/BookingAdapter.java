// File: CarpoolingApp/app/src/main/java/com/carpooling/app/adapters/BookingAdapter.java

package com.carpoolingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.carpoolingapp.R;
import com.carpoolingapp.models.Booking;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookingList;
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public BookingAdapter(Context context, List<Booking> bookingList, OnBookingClickListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.fromText.setText(booking.getFromLocation());
        holder.toText.setText(booking.getToLocation());
        holder.dateText.setText(booking.getDate());
        holder.timeText.setText(booking.getTime());
        holder.priceText.setText("$" + String.format("%.2f", booking.getTotalPrice()));
        holder.userNameText.setText(booking.getDriverName());

        // Set status
        String status = booking.getStatus();
        holder.statusBadge.setText(status);

        if ("confirmed".equals(status)) {
            holder.statusBadge.setBackgroundResource(R.drawable.bg_status_badge);
        } else if ("completed".equals(status)) {
            holder.statusBadge.setBackgroundColor(context.getColor(R.color.status_inactive));
        } else if ("cancelled".equals(status)) {
            holder.statusBadge.setBackgroundColor(context.getColor(R.color.status_error));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onBookingClick(booking);
                }
            }
        });

        holder.chatIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open chat
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView statusBadge, priceText, fromText, toText, dateText, timeText, userNameText;
        ImageView chatIcon;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            priceText = itemView.findViewById(R.id.priceText);
            fromText = itemView.findViewById(R.id.fromText);
            toText = itemView.findViewById(R.id.toText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            userNameText = itemView.findViewById(R.id.userNameText);
            chatIcon = itemView.findViewById(R.id.chatIcon);
        }
    }
}