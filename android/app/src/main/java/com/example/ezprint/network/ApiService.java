package com.example.ezprint.network;

import com.example.ezprint.PriceSettingsActivity;
import com.example.ezprint.models.ApiErrorResponse;
import com.example.ezprint.models.DashboardStatsResponse;
import com.example.ezprint.models.ImageUploadResponse;
import com.example.ezprint.models.LoginRequest;
import com.example.ezprint.models.LoginResponse;
import com.example.ezprint.models.OrderDetailsResponse;
import com.example.ezprint.models.OrderResponse;
import com.example.ezprint.models.OrderStatusUpdateRequest;
import com.example.ezprint.models.PriceSettings;
import com.example.ezprint.models.PriceSettingsResponse;
import com.example.ezprint.models.PriceUpdateResponse;
import com.example.ezprint.models.ProfileUpdateRequest;
import com.example.ezprint.models.ProfileUpdateResponse; // Import the correct response model
import com.example.ezprint.models.QrCodeResponse;
import com.example.ezprint.models.RegisterDeviceRequest;
import com.example.ezprint.models.RegisterRequest;
import com.example.ezprint.models.RegisterResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Login API
    @POST("api/shops/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    // Register API
    @POST("api/shops/register")
    Call<RegisterResponse> registerShop(@Body RegisterRequest registerRequest);

    // API call for uploading a profile picture
    @Multipart
    @POST("api/shops/profile-picture/{shopId}")
    Call<ImageUploadResponse> uploadProfilePicture(
            @Path("shopId") int shopId,
            @Part MultipartBody.Part profile_pic
    );

    // CORRECTED: update profile details method
    @PUT("api/shops/profile/{shopId}")
    Call<ProfileUpdateResponse> updateProfile(
           @Path("shopId") int shopId,
           @Body ProfileUpdateRequest request
    );

    // NEW: API call to get recent orders
    @GET("api/orders/recent/{shopId}")
    Call<OrderResponse> getRecentOrders(@Path("shopId") int shopId);

    @POST("api/shops/generate-qr/{shopId}")
    Call<QrCodeResponse> generateQrCode(@Path("shopId") int shopId);

    // UPDATED: API call to search for orders with status filter
    @GET("api/shops/search/{shopId}")
    Call<OrderResponse> searchOrders(
            @Path("shopId") int shopId,
            @Query("q") String searchQuery,
            @Query("status") String status
    );

    // NEW: API calls for price settings
    @GET("api/shops/prices/{shopId}")
    Call<PriceSettingsResponse> getPriceSettings(@Path("shopId") int shopId);

    // CORRECTED: The return type must match the callback in your activity
    @PUT("api/shops/prices/{shopId}")
    Call<PriceUpdateResponse> updatePriceSettings(@Path("shopId") int shopId, @Body PriceSettings prices);

    // NEW: API call to get all dashboard statistics
    @GET("api/shops/dashboard-stats/{shopId}")
    Call<DashboardStatsResponse> getDashboardStats(@Path("shopId") int shopId);

    @GET("api/orders/details/{orderId}")
    Call<OrderDetailsResponse> getOrderDetails(@Path("orderId") int orderId);

    // NEW: API call to mark a document as printed
    @PUT("api/orders/documents/{documentId}/mark-printed")
    Call<ApiErrorResponse> markDocumentAsPrinted(@Path("documentId") int documentId);

    // NEW: API call to update an order's status
    @PUT("api/orders/update-status/{orderId}")
    Call<ApiErrorResponse> updateOrderStatus(
            @Path("orderId") int orderId,
            @Body OrderStatusUpdateRequest request
    );

    @GET("api/orders/all/{shopId}")
    Call<OrderResponse> getAllOrders(
            @Path("shopId") int shopId,
            @Query("filter") String filter
    );

    /**
     * API call to register a shopkeeper's device token for push notifications.
     * @param request The request body containing the shop ID and FCM token.
     * @return A generic response indicating success or failure.
     */
    @POST("api/shops/register-device")
    Call<ApiErrorResponse> registerDevice(@Body RegisterDeviceRequest request);

    // NEW: API call to unregister a device token on logout
    @HTTP(method = "DELETE", path = "api/shops/unregister-device", hasBody = true)
    Call<ApiErrorResponse> unregisterDevice(@Body RegisterDeviceRequest request);

    @GET("api/orders/pending/{shopId}")
    Call<OrderResponse> getPendingOrders(@Path("shopId") int shopId);

    @DELETE("api/orders/{orderId}")
    Call<ApiErrorResponse> deleteOrder(@Path("orderId") int orderId);
}
