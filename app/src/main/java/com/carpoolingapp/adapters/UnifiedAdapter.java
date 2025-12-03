package com.carpoolingapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.carpoolingapp.R;
import com.carpoolingapp.activities.RideDetailActivity;
import com.carpoolingapp.models.Booking;
import com.carpoolingapp.models.Ride;

import java.util.List;
import java.util.Locale;

public class UnifiedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Object> items;
    private final Context context;

    private static final int TYPE_RIDE = 1;
    private static final int TYPE_BOOKING = 2;

    public UnifiedAdapter(List<Object> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = items.get(position);
        if (obj instanceof Booking) return TYPE_BOOKING;
        return TYPE_RIDE; // default
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_BOOKING) {
            View view = inflater.inflate(R.layout.item_booking, parent, false);
            return new BookingViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_ride, parent, false);
            return new RideViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        Object obj = items.get(position);

        if (holder instanceof BookingViewHolder) {
            ((BookingViewHolder) holder).bind((Booking) obj, context);
        } else if (holder instanceof RideViewHolder) {
            ((RideViewHolder) holder).bind((Ride) obj, context);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RideViewHolder extends RecyclerView.ViewHolder {

        TextView priceText, seatsText, fromText, toText, dateText, timeText, driverNameText;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);

            priceText = itemView.findViewById(R.id.priceText);
            seatsText = itemView.findViewById(R.id.seatsText);
            fromText = itemView.findViewById(R.id.fromText);
            toText = itemView.findViewById(R.id.toText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            driverNameText = itemView.findViewById(R.id.driverNameText);
        }

        void bind(Ride ride, Context context) {
            priceText.setText(formatPrice(ride.getPricePerSeat()));

            int remaining = ride.getAvailableSeats();
            seatsText.setText(remaining + " seats left");

            fromText.setText(ride.getFromLocation());
            toText.setText(ride.getToLocation());
            dateText.setText(ride.getDate());
            timeText.setText(ride.getTime());
            driverNameText.setText(ride.getDriverName());

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, RideDetailActivity.class);
                intent.putExtra("rideId", ride.getRideId());
                intent.putExtra("mode", "driver_manage");
                intent.putExtra("isManageMode", true);
                context.startActivity(intent);
            });
        }
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {

        TextView userNameText, fromText, toText, dateText, timeText, priceText, seatsBookedText;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameText = itemView.findViewById(R.id.userNameText);
            fromText = itemView.findViewById(R.id.fromText);
            toText = itemView.findViewById(R.id.toText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            priceText = itemView.findViewById(R.id.priceText);


            seatsBookedText = itemView.findViewById(R.id.seatsBookedText);
        }

        void bind(Booking booking, Context context) {

            userNameText.setText(booking.getDriverName());
            fromText.setText(booking.getFromLocation());
            toText.setText(booking.getToLocation());
            dateText.setText(booking.getDate());
            timeText.setText(booking.getTime());
            priceText.setText(formatPrice(booking.getTotalPrice()));
            seatsBookedText.setText(booking.getSeatsBooked() + " seats booked");

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, RideDetailActivity.class);
                intent.putExtra("rideId", booking.getRideId());
                intent.putExtra("mode", "rider_manage");
                intent.putExtra("seatsBooked", booking.getSeatsBooked());
                intent.putExtra("isManageMode", true);
                context.startActivity(intent);
            });
        }
    }
    private static String formatPrice(double price) {
        if (price == (long) price)
            return String.format(Locale.getDefault(), "$%d", (long) price);
        else
            return String.format(Locale.getDefault(), "$%.2f", price);
    }


}
