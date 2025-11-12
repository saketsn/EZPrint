package com.example.ezprint.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents the comprehensive details of a single order, including the student
 * and all associated documents. This is used for the Order Details screen.
 */
public class Order {
    // --- Existing Fields ---
    @SerializedName("order_id")
    private int orderId;

    @SerializedName("order_code") // This is 'order_code' from your DB
    private String orderUid;

    @SerializedName("student_name")
    private String customerName;

    @SerializedName("file_count")
    private int fileCount;

    @SerializedName("status")
    private String status;

    // --- NEW Fields for Order Details ---
    @SerializedName("student_email")
    private String studentEmail;

    @SerializedName("student_phone")
    private String studentPhone;

    @SerializedName("notes")
    private String notes;

    @SerializedName("amount")
    private Double amount;

    @SerializedName("payment_status")
    private String paymentStatus;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("documents")
    private List<Document> documents;

    // --- Getters for all fields ---
    public int getOrderId() { return orderId; }
    public String getOrderUid() { return orderUid; }
    public String getCustomerName() { return customerName; }
    public int getFileCount() { return fileCount; }
    public String getStatus() { return status; }
    public String getStudentEmail() { return studentEmail; }
    public String getStudentPhone() { return studentPhone; }
    public String getNotes() { return notes; }
    public Double getAmount() { return amount; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getCreatedAt() { return createdAt; }
    public List<Document> getDocuments() { return documents; }

    /**
     * Nested class representing a single document within an order.
     */
    public static class Document {
        @SerializedName("document_id")
        private int documentId;
        @SerializedName("doc_type")
        private String docType;
        @SerializedName("file_url")
        private String fileUrl;
        @SerializedName("copies")
        private int copies;
        @SerializedName("is_color")
        private int isColor; // Using int for 0 or 1
        @SerializedName("is_duplex")
        private int isDuplex; // Using int for 0 or 1
        @SerializedName("orientation")
        private String orientation;
        @SerializedName("page_range")
        private String pageRange;

        // NEW: Field to track if the document has been marked as printed
        @SerializedName("is_printed")
        private int isPrinted; // 0 for false, 1 for true

        // Getters for document fields
        public int getDocumentId() { return documentId; }
        public String getDocType() { return docType; }
        public String getFileUrl() { return fileUrl; }
        public int getCopies() { return copies; }
        public boolean isColor() { return isColor == 1; }
        public boolean isDuplex() { return isDuplex == 1; }
        public String getOrientation() { return orientation; }
        public String getPageRange() { return pageRange; }
        public boolean isPrinted() { return isPrinted == 1; }

        // NEW: Setter to update the printed status in the app
        public void setPrinted(boolean printed) {
            this.isPrinted = printed ? 1 : 0;
        }


    }
}

