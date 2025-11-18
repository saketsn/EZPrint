# EZPrint – Smart Digital Print Order Management System

EZPrint is a full-stack application that digitalizes the print-ordering process in college xerox shops.  
It includes:

- **Android Mobile App (Shop Owner)**  
- **Web Portal (Student Side)**  
- **Backend Server (Node.js + MySQL)**  

This system removes the need for physical queues, pen drives, and manual communication.

---

## 🚀 Project Overview

EZPrint allows students to upload their documents online and submit print requests remotely.  
The shop owner receives these requests through the mobile app and manages the entire workflow digitally.

---

## 📱 System Components

### 1. Shop Owner Mobile App (Android – Java)
- Login / Signup
- Auto-login (SharedPreferences)
- Dashboard with order statistics
- View order details
- View uploaded files & ID proof
- Update order status (Pending → Printing → Completed)
- Manage price details
- Generate & share QR code
- Profile & settings pages

### 2. Student Web Portal
- Access form via QR code
- Upload PDF files
- Upload ID Proof
- Enter personal details (Name, USN, Phone, Email)
- Select print settings
- View shop pricing
- Submit order online

### 3. Backend Server (Node.js + MySQL)
- API endpoints for mobile + web
- Order submission & storage
- File upload handling (multer)
- Shop authentication
- Price management
- QR Code generation
- MySQL database integration

---

## 🧱 Architecture

```

Student Web Form → Backend API → Shop Owner App
↑                              ↓
└────────── Shop QR Code ──────┘

```

---

## 🛠️ Tech Stack

### Mobile App
- Java (Android)
- Retrofit + Gson
- Glide
- Material Components
- Lottie Animations
- SharedPreferences
- ZXing / ML Kit (QR scanning)

### Student Web
- HTML5
- CSS3
- JavaScript
- Fetch API

### Backend
- Node.js (Express)
- MySQL
- Multer
- Dotenv
- Bcrypt.js
- CORS

---

## 📂 Project Structure

### Backend
```

server/
├── node_modules/
├── public/
├── routes/
│   ├── orders.js
│   └── shops.js
├── uploads/
│   ├── EZP-4-xxxx...... (auto-generated order folders)
│   ├── profile/
│   └── qr-codes/
├── utils/
│   ├── .env
│   ├── db.js
│   ├── fcm-helper.js
│   └── firebase-adminsdk.json
├── package.json
└── server.js

```

### Student Web Portal
```

student/
├── img/
├── index.html
├── script.js
└── styles.css

```

### Android Mobile App (Java)
```

app/src/main/java/com.example.ezprint/
├── adapters/
├── models/
├── network/
│   ├── ApiService.java
│   └── RetrofitClient.java
├── utils/
├── AllOrders.java
├── BaseActivity.java
├── DashboardFragment.java
├── Home.java
├── Login.java
├── MainActivity.java
├── MyApp.java
├── MyFirebaseMessagingService.java
├── NewOrderBottomSheet.java
├── OrderDetails.java
├── PendingOrders.java
├── PriceSettingsActivity.java
├── Profile.java
├── QrCode.java
├── SearchOrder.java
├── SettingsActivity.java
└── SignUp.java

```

---

## 🔗 API Endpoints

### Authentication
```

POST /api/shops/login
POST /api/shops/register

```

### Orders
```

POST /api/orders/submit
GET  /api/orders/shop/:shopId
GET  /api/orders/:orderId
PUT  /api/orders/status

```

### Shop Details & Pricing
```

GET  /api/shops/details/:id
PUT  /api/shops/prices

```

---

## 💾 Database Tables

- shops  
- students  
- orders  
- documents  
- price_settings  

Each order stores:
- Student details
- ID proof
- Multiple print files
- File-level print settings
- Order-level status

---

# 📸 EZPrint App — Screenshot Descriptions

## 1️⃣ Home Screen (Dashboard)
The Home Screen provides a quick overview of the shop’s daily activity.  
- Displays shop name and profile picture  
- Shows Today’s Orders: Pending, In Progress, Completed  
- Overview of Today’s Revenue, Monthly Orders, and New Customers  
- Recent Orders list with status and file count  
- Bottom navigation bar for Home, Search, Add Order, QR Code, and Price Settings  
<img width="1280" height="2856" alt="Home Page" src="https://github.com/user-attachments/assets/1d24cf0e-d6dc-4def-b169-befcb4f41e90" />

---

## 2️⃣ Splash / Loading Screen
A minimal and animated screen shown at app launch.  
- Checks login status via SharedPreferences  
- Redirects user to Login Page or Home Page  
- Visually smooth entry into the app  
<img width="1280" height="2856" alt="Loading Screen" src="https://github.com/user-attachments/assets/16458ad3-a1da-4db9-9fd5-9603620584dd" />

---

## 3️⃣ Login Screen
Authentication screen for shop owners.  
- Phone number (username) and password fields  
- Password visibility toggle  
- “Forget password” option  
- “Sign Up” link for new shop owners  
- Simple and secure UI  
<img width="1280" height="2856" alt="Login Page" src="https://github.com/user-attachments/assets/5c836387-f6ba-4a56-9edd-1683344e328f" />

---

## 4️⃣ Order Details Screen
Shows full information for a selected order.  
- Order code, creation date, and status  
- Student name, phone, and email  
- ID proof file preview and open option  
- List of uploaded print files with print settings (copies, color, duplex, pages)  
- Finalize Order section with amount, Cancel, and Completed buttons  
<img width="1280" height="2856" alt="Order Details Page" src="https://github.com/user-attachments/assets/d308077e-ad00-47ac-a3ae-fc9c2067313a" />

---

## 5️⃣ Order History Screen
Used to track finished and ongoing orders.  
- Filters: Daily, Weekly, Monthly  
- Order cards showing order ID, file count, student name, and status  
- Helps shop owners analyze past orders  
<img width="1280" height="2856" alt="Order History " src="https://github.com/user-attachments/assets/0de96976-59a1-4d19-a2c5-2f3584cfaade" />

---

## 6️⃣ Price Settings Screen
Allows the shop owner to update printing prices.  
- B&W Page, Color Page, B&W Duplex, Color Duplex  
- Editable list items  
- Syncs with backend after update  
<img width="1280" height="2856" alt="Price Settings" src="https://github.com/user-attachments/assets/70ac1e9a-bbce-4594-9551-7e0ef21341b0" />

---

## 7️⃣ Profile Screen
Displays the shop owner’s personal and business information.  
- Editable profile picture  
- Public Information: Owner Name, Shop Name  
- Private Information: Email, Phone, Address  
- Clean and organized layout  
<img width="1280" height="2856" alt="Profile Page" src="https://github.com/user-attachments/assets/93f15679-b816-4dcf-934b-864e64e6b050" />

---

## 8️⃣ QR Code Screen
Shows the QR code students can scan to open the web form.  
- Shop name, owner name, and profile image  
- Large generated QR code  
- Used for sharing student portal link easily  
<img width="1280" height="2856" alt="QR Code Page" src="https://github.com/user-attachments/assets/03dc863b-115c-4563-9d77-18a9f69882cd" />

---

## 9️⃣ Search Orders Screen
Helps find orders quickly.  
- Search bar for Order ID or Student Name  
- Tabs: Completed, Pending, Rejected  
- Order cards displayed in grid format  
- Useful for shops with high order volume  
<img width="1280" height="2856" alt="Search Orders" src="https://github.com/user-attachments/assets/78527795-60b9-4214-8b30-1a802a958608" />

---

## 🔟 Settings Screen
Contains device-level app preferences.  
- Dark mode toggle for theme switching  
- Notification toggles (In-app, Push)  
- Preferences saved locally on device  
<img width="1280" height="2856" alt="Settings Page" src="https://github.com/user-attachments/assets/b1a4d555-2657-48d9-a5dc-759a39e133d5" />

---

# Backend Server Setup and IP Configuration

This section explains how to run the backend server and how to configure the correct IP address inside the Android app (RetrofitClient.java).

---

## 1. Running the Backend Server (Node.js)

### Step 1: Navigate to the backend folder
```bash
cd server
```

### Step 2: Install dependencies
```bash
npm install
```

### Step 3: Create a `.env` file inside the `server/utils/` folder
Example `.env` content:
```
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=yourpassword
DB_NAME=ezprint
PORT=3000
```

### Step 4: Start the server
Using Node:
```bash
node server.js
```

Using Nodemon (optional):
```bash
nodemon server.js
```

You should see:
```
Server is running on port 3000
Connected to MySQL database
```

---

## 2. Changing the IP Address in the Android App

The Android app communicates with the backend using Retrofit.  
To make this work, update the base URL with the correct IP address of the machine running the server.

Open the file:
```
app/src/main/java/com/example/ezprint/network/RetrofitClient.java
```

You will see something like:
```java
public static final String BASE_URL = "http://10.186.68.162:3000/";
```

Replace the IP with your own local network IP.  
Example using a dummy IP:
```java
public static final String BASE_URL = "http://192.168.1.50:3000/";
```

---

## 3. How to Find Your IP Address

### Windows
```bash
ipconfig
```
Look for:
```
IPv4 Address . . . . . . . . . . : 192.168.x.x
```

### Mac / Linux
```bash
ifconfig
```
Look for:
```
inet 192.168.x.x
```

Use this IP address in the RetrofitClient.

---

## 4. Important Notes About IP Usage

- Your Android device and your server machine must be on the same Wi-Fi network.
- Do not use `localhost` or `127.0.0.1` inside Android. They do not work.
- For Android Emulator only, you can use:
```
http://10.0.2.2:3000/
```
- For a real device, always use:
```
http://<YOUR_LOCAL_IP>:3000/
```

---

## 5. Final RetrofitClient Example

```java
package com.example.ezprint.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Use your computer's local IP address here
    public static final String BASE_URL = "http://192.168.1.50:3000/";

    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
```

---

## 6. Final Steps to Run the System

1. Start the backend server  
2. Update IP address in RetrofitClient.java  
3. Connect both laptop and phone to the same Wi-Fi  
4. Run the Android app  

The mobile app will now communicate with the backend successfully.


## 🧪 Testing

- Tested on multiple Android devices  
- API tested with Postman  
- Web tested on Chrome, Firefox, Edge  
- File upload testing with multiple PDF sizes  
- Database load tested with multiple submissions  

---

## 📈 Results

- Eliminated physical queues  
- Smooth digital workflow  
- Fast processing for shop owners  
- Convenient access for students  
- Reliable, stable, and responsive performance  

---


