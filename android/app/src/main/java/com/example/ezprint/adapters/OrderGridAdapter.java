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

public class OrderGridAdapter extends RecyclerView.Adapter<OrderGridAdapter.OrderGridViewHolder> {

    private final List<Order> orderList;
    private final Context context;
    private final Random random = new Random();

    public OrderGridAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_grid, parent, false);
        return new OrderGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderGridViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderGridViewHolder extends RecyclerView.ViewHolder {
        ImageView orderIcon;
        TextView orderTitle, orderSubtitle, orderStatus;

        public OrderGridViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIcon = itemView.findViewById(R.id.order_icon);
            orderTitle = itemView.findViewById(R.id.order_title);
            orderSubtitle = itemView.findViewById(R.id.order_subtitle);
            orderStatus = itemView.findViewById(R.id.order_status);
        }

        void bind(Order order) {
            orderTitle.setText(order.getOrderUid());
            orderSubtitle.setText("by " + order.getCustomerName());
            String status = order.getStatus();
            if (status != null && !status.isEmpty()) {
                status = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
            }
            orderStatus.setText(status);

            int randomIconNum = random.nextInt(5) + 1;
            int iconId = context.getResources().getIdentifier("ic_order_" + randomIconNum, "drawable", context.getPackageName());
            orderIcon.setImageResource(iconId);

            setStatusColor(order.getStatus());

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderDetails.class);
                intent.putExtra("ORDER_ID", order.getOrderId());
                context.startActivity(intent);
            });
        }

        private void setStatusColor(String status) {
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
