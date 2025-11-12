package com.example.ezprint.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezprint.OrderDetails;
import com.example.ezprint.R;
import com.example.ezprint.models.Order;
import java.util.List;
import java.util.Random;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orderList;
    private final Context context;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orderList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView orderIcon;
        TextView orderTitle, orderSubtitle, orderAmount, orderStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIcon = itemView.findViewById(R.id.order_icon);
            orderTitle = itemView.findViewById(R.id.order_title);
            orderSubtitle = itemView.findViewById(R.id.order_subtitle);
            orderAmount = itemView.findViewById(R.id.order_amount);
            orderStatus = itemView.findViewById(R.id.order_status);
        }

        void bind(Order order, int position) {
            // Set data
            orderTitle.setText(order.getOrderUid());
            orderSubtitle.setText("Order by " + order.getCustomerName());
            orderAmount.setText(order.getFileCount() + " File");
            String status = order.getStatus();
            if (status != null && !status.isEmpty()) {
                // Capitalize first letter
                status = status.substring(0, 1).toUpperCase() + status.substring(1);
            }
            orderStatus.setText(status);

            // 🔹 Sequential icon selection (1 → 5 → repeat)
            int iconNumber = (position % 5) + 4; // cycles through 1–5
            int iconId = context.getResources().getIdentifier(
                    "ic_order_" + iconNumber,
                    "drawable",
                    context.getPackageName()
            );
            orderIcon.setImageResource(iconId);

            // UPDATED: Set dynamic status style
            setStatusStyle(order.getStatus());

            // UPDATED: Set click listener to navigate to details
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderDetails.class);
                intent.putExtra("ORDER_ID", order.getOrderId());
                context.startActivity(intent);
            });
        }


        private void setStatusStyle(String status) {
            int backgroundRes;
            int textColorRes;
            String statusText = status.toLowerCase();

            switch (statusText) {
                case "pending":
                    backgroundRes = R.drawable.status_background_pending;
                    textColorRes = R.color.white;
                    break;
                case "printing":
                case "accepted":
                case "in progress": // Added for more flexibility
                    backgroundRes = R.drawable.status_background_progress;
                    textColorRes = R.color.white;
                    break;
                case "cancelled":
                case "rejected":
                    backgroundRes = R.drawable.status_background_cancelled;
                    textColorRes = R.color.white;
                    break;
                case "completed":
                default:
                    backgroundRes = R.drawable.status_background_completed;
                    textColorRes = R.color.white;
                    break;
            }
            orderStatus.setBackgroundResource(backgroundRes);
            orderStatus.setTextColor(ContextCompat.getColor(context, textColorRes));
        }
    }
}