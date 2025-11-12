package com.example.ezprint.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezprint.OrderDetails;
import com.example.ezprint.R;
import com.example.ezprint.models.Order;

import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> {

    private final List<Order> orderList;
    private final Context context;

    public OrderHistoryAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new OrderHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHistoryViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderHistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView orderStatusIcon;
        TextView orderCodeText, orderDetailsText, orderAmountText;

        public OrderHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            orderStatusIcon = itemView.findViewById(R.id.order_status_icon);
            orderCodeText = itemView.findViewById(R.id.order_code_text);
            orderDetailsText = itemView.findViewById(R.id.order_details_text);
            orderAmountText = itemView.findViewById(R.id.order_amount_text);
        }

        void bind(Order order) {
            // Set the main text fields
            orderCodeText.setText(order.getOrderUid());

            String details = String.format(Locale.getDefault(), "%d Files • %s",
                    order.getFileCount(),
                    order.getCustomerName());
            orderDetailsText.setText(details);

            // Set the amount, formatted as currency
            if (order.getAmount() != null) {
                orderAmountText.setText(String.format(Locale.getDefault(), "₹%.2f", order.getAmount()));
            } else {
                orderAmountText.setText("₹--.--");
            }

            // Set the status icon based on the order status
            switch (order.getStatus().toLowerCase()) {
                case "completed":
                    orderStatusIcon.setImageResource(R.drawable.ic_status_completed);
                    break;
                case "cancelled":
                case "rejected":
                    orderStatusIcon.setImageResource(R.drawable.ic_status_cancelled);
                    break;
                case "pending":
                case "accepted":
                case "printing":
                default:
                    orderStatusIcon.setImageResource(R.drawable.ic_status_pending);
                    break;
            }

            // Set a click listener to open the OrderDetailsActivity
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderDetails.class);
                intent.putExtra("ORDER_ID", order.getOrderId());
                context.startActivity(intent);
            });
        }
    }
}
