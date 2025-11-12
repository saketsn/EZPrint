package com.example.ezprint.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezprint.OrderDetails;
import com.example.ezprint.R;
import com.example.ezprint.models.ApiErrorResponse;
import com.example.ezprint.models.Order;
import com.example.ezprint.models.OrderStatusUpdateRequest;
import com.example.ezprint.network.ApiService;
import com.example.ezprint.network.RetrofitClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingOrderAdapter extends RecyclerView.Adapter<PendingOrderAdapter.ViewHolder> {

    // Interface to notify the activity when an order is removed
    public interface OnOrderActionListener {
        void onOrderAction();
    }

    private final List<Order> orderList;
    private final Context context;
    private final OnOrderActionListener listener;

    public PendingOrderAdapter(Context context, List<Order> orderList, OnOrderActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pending_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(orderList.get(position));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView orderStatusIcon;
        ImageButton menuButton;
        TextView orderCodeText, orderDateText, orderDetailsText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderStatusIcon = itemView.findViewById(R.id.order_status_icon);
            menuButton = itemView.findViewById(R.id.menu_button);
            orderCodeText = itemView.findViewById(R.id.order_code_text);
            orderDateText = itemView.findViewById(R.id.order_date_text);
            orderDetailsText = itemView.findViewById(R.id.order_details_text);
        }

        void bind(final Order order) {
            // Bind data to the views
            orderCodeText.setText(order.getOrderUid());
            String details = String.format(Locale.getDefault(), "%d Files • %s",
                    order.getFileCount(),
                    order.getCustomerName());
            orderDetailsText.setText(details);

            // Format and set date
            try {
                SimpleDateFormat incomingFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat outgoingFormat = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
                Date date = incomingFormat.parse(order.getCreatedAt());
                orderDateText.setText(outgoingFormat.format(date));
            } catch (ParseException e) {
                orderDateText.setText("N/A");
            }

            // Main click navigates to details activity
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderDetails.class);
                intent.putExtra("ORDER_ID", order.getOrderId());
                context.startActivity(intent);
            });

            // 3-dot menu click shows the popup menu
            menuButton.setOnClickListener(this::showPopupMenu);
        }

        private void showPopupMenu(View view) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.getMenuInflater().inflate(R.menu.pending_order_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return false;
                Order order = orderList.get(position);
                int itemId = item.getItemId();

                if (itemId == R.id.action_accept) {
                    updateOrderStatus(order, "accepted", position);
                    return true;
                } else if (itemId == R.id.action_reject) {
                    updateOrderStatus(order, "rejected", position);
                    return true;
                } else if (itemId == R.id.action_delete) {
                    showDeleteConfirmationDialog(order, position);
                    return true;
                }
                return false;
            });
            popup.show();
        }

        private void showDeleteConfirmationDialog(Order order, int position) {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Order?")
                    .setMessage("This action cannot be undone. Are you sure you want to permanently delete this order?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteOrder(order, position))
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void updateOrderStatus(Order order, String status, int position) {
            ApiService apiService = RetrofitClient.getApiService();
            OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(status, null);

            apiService.updateOrderStatus(order.getOrderId(), request).enqueue(new Callback<ApiErrorResponse>() {
                @Override
                public void onResponse(@NonNull Call<ApiErrorResponse> call, @NonNull Response<ApiErrorResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Order " + status + " successfully!", Toast.LENGTH_SHORT).show();
                        orderList.remove(position);
                        notifyItemRemoved(position);
                        listener.onOrderAction(); // Notify the activity
                    } else {
                        Toast.makeText(context, "Failed to update status.", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ApiErrorResponse> call, @NonNull Throwable t) {
                    Toast.makeText(context, "Network Error.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void deleteOrder(Order order, int position) {
            ApiService apiService = RetrofitClient.getApiService();
            apiService.deleteOrder(order.getOrderId()).enqueue(new Callback<ApiErrorResponse>() {
                @Override
                public void onResponse(@NonNull Call<ApiErrorResponse> call, @NonNull Response<ApiErrorResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Order deleted successfully.", Toast.LENGTH_SHORT).show();
                        orderList.remove(position);
                        notifyItemRemoved(position);
                        listener.onOrderAction(); // Notify the activity
                    } else {
                        Toast.makeText(context, "Failed to delete order.", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ApiErrorResponse> call, @NonNull Throwable t) {
                    Toast.makeText(context, "Network Error.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

