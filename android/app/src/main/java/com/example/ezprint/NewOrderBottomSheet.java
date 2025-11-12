package com.example.ezprint;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ezprint.models.ApiErrorResponse;
import com.example.ezprint.models.OrderStatusUpdateRequest;
import com.example.ezprint.network.ApiService;
import com.example.ezprint.network.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewOrderBottomSheet extends BottomSheetDialogFragment {

    public interface OnDismissListener {
        void onBottomSheetDismissed();
    }

    private OnDismissListener dismissListener;
    private String orderId, orderCode, customerName, fileCount, totalCopies, printType;

    public static NewOrderBottomSheet newInstance(String orderId, String customerName, String fileCount, String totalCopies, String printType) {
        NewOrderBottomSheet fragment = new NewOrderBottomSheet();
        Bundle args = new Bundle();
        args.putString("orderId", orderId);
        args.putString("customerName", customerName);
        args.putString("fileCount", fileCount);
        args.putString("totalCopies", totalCopies);
        args.putString("printType", printType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDismissListener) {
            dismissListener = (OnDismissListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnDismissListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
            customerName = getArguments().getString("customerName");
            fileCount = getArguments().getString("fileCount");
            totalCopies = getArguments().getString("totalCopies");
            printType = getArguments().getString("printType");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_new_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView customerNameText = view.findViewById(R.id.customer_name_text);
        TextView fileCountText = view.findViewById(R.id.file_count_text);
        TextView totalCopiesText = view.findViewById(R.id.total_copies_text);
        TextView printTypeText = view.findViewById(R.id.print_type_text);
        Button rejectButton = view.findViewById(R.id.reject_button);
        Button acceptButton = view.findViewById(R.id.accept_button);

        customerNameText.setText("from " + customerName);
        fileCountText.setText(fileCount + " Files");
        totalCopiesText.setText(totalCopies + " Copies");
        printTypeText.setText(printType);

        rejectButton.setOnClickListener(v -> updateOrderStatus("rejected"));
        acceptButton.setOnClickListener(v -> updateOrderStatus("accepted"));
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissListener != null) {
            dismissListener.onBottomSheetDismissed();
        }
    }

    private void updateOrderStatus(String status) {
        ApiService apiService = RetrofitClient.getApiService();
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(status, null);

        apiService.updateOrderStatus(Integer.parseInt(orderId), request).enqueue(new Callback<ApiErrorResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiErrorResponse> call, @NonNull Response<ApiErrorResponse> response) {
                // CORRECTED: Add a safety check before using the context
                if (!isAdded()) {
                    return; // The fragment has been detached, so do nothing.
                }

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Order " + status + " successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to update order status.", Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<ApiErrorResponse> call, @NonNull Throwable t) {
                // CORRECTED: Add a safety check here as well
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }
}