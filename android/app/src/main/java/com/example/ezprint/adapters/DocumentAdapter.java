package com.example.ezprint.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezprint.R;
import com.example.ezprint.models.ApiErrorResponse;
import com.example.ezprint.models.Order;
import com.example.ezprint.network.ApiService;
import com.example.ezprint.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private final List<Order.Document> documentList;
    private final Context context;

    public DocumentAdapter(Context context, List<Order.Document> documentList) {
        this.context = context;
        this.documentList = documentList;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        holder.bind(documentList.get(position));
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView docName, docStatus, docCopies, docColor, docDuplex, docPages;
        LinearLayout detailsLayout;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            docName = itemView.findViewById(R.id.doc_name);
            docStatus = itemView.findViewById(R.id.doc_status);
            detailsLayout = itemView.findViewById(R.id.details_layout);
            docCopies = itemView.findViewById(R.id.doc_copies);
            docColor = itemView.findViewById(R.id.doc_color);
            docDuplex = itemView.findViewById(R.id.doc_duplex);
            docPages = itemView.findViewById(R.id.doc_pages);
        }

        void bind(Order.Document document) {
            // Set main details
            String[] urlParts = document.getFileUrl().split("/");
            docName.setText(urlParts[urlParts.length - 1]);
            docStatus.setText(document.isPrinted() ? "Printed" : "Pending");

            // Set hidden details
            docCopies.setText(document.getCopies() + " Copies");
            docColor.setText(document.isColor() ? "Color" : "B&W");
            docDuplex.setText(document.isDuplex() ? "Duplex" : "Simplex");
            docPages.setText("Pages: " + document.getPageRange());

            // Set dynamic styles for status
            setStatusStyle(document.isPrinted());

            // Set long press listener for marking as printed
            itemView.setOnLongClickListener(v -> {
                if (!document.isPrinted()) {
                    showMarkAsPrintedDialog(document, getAdapterPosition());
                } else {
                    Toast.makeText(context, "This document is already marked as printed.", Toast.LENGTH_SHORT).show();
                }
                return true;
            });

            // Set simple click listener to open the document URL
            itemView.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(RetrofitClient.BASE_URL + document.getFileUrl()));
                context.startActivity(browserIntent);
            });
        }

        private void setStatusStyle(boolean isPrinted) {
            int backgroundRes;
            int textColorRes;
            int statusBarRes;

            if (isPrinted) {
                backgroundRes = R.drawable.status_background_completed;
                textColorRes = R.color.white;
                statusBarRes = R.drawable.status_bar_completed;
            } else {
                // You can add more states here like 'printing' if needed
                backgroundRes = R.drawable.status_background_pending;
                textColorRes = R.color.white;
                statusBarRes = R.drawable.status_bar_pending;
            }

            docStatus.setBackgroundResource(backgroundRes);
            docStatus.setTextColor(ContextCompat.getColor(context, textColorRes));
        }
    }

    private void showMarkAsPrintedDialog(Order.Document document, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Mark as Printed?")
                .setMessage("Are you sure you want to mark this document as printed?")
                .setPositiveButton("Yes, Mark as Printed", (dialog, which) -> {
                    markDocumentAsPrintedApiCall(document, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markDocumentAsPrintedApiCall(Order.Document document, int position) {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.markDocumentAsPrinted(document.getDocumentId()).enqueue(new Callback<ApiErrorResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiErrorResponse> call, @NonNull Response<ApiErrorResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    document.setPrinted(true);
                    notifyItemChanged(position);
                } else {
                    Toast.makeText(context, "Failed to update status.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiErrorResponse> call, @NonNull Throwable t) {
                Log.e("DocumentAdapter", "API Failure: ", t);
                Toast.makeText(context, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

