const admin = require('firebase-admin');
const pool = require('./db'); // Ensure this path correctly points to your database connection file

// IMPORTANT: This line requires the 'service-account-key.json' file you downloaded from Firebase.
// Make sure that file is in the same root directory as this one.
const serviceAccount = require('./ezprint-cb431-firebase-adminsdk-fbsvc-a40318ed0c.json');

// Initialize the Firebase Admin SDK. This only needs to happen once when your server starts.
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

/**
 * Finds a shopkeeper's device token from the 'devices' table and sends them a push notification.
 * @param {number} shopId The ID of the shop to notify.
 * @param {string} title The title of the notification (e.g., "New Order Received!").
 * @param {string} body The main text of the notification (e.g., "You have a new order from...").
 * @param {object} orderData Extra data to send silently to the app (e.g., { orderId: '123', ... }).
 */
const sendPushNotification = async (shopId, title, body, orderData) => {
    try {
        // 1. Find the FCM token for the given shop owner in your database.
        const [devices] = await pool.execute(
            'SELECT fcm_token FROM devices WHERE user_id = ? AND user_type = "shopkeeper"',
            [shopId]
        );

        // If no token is found for this user, we can't send a notification.
        if (devices.length === 0) {
            console.log(`Notification Error: No device token found for shop ID: ${shopId}`);
            return;
        }

        const token = devices[0].fcm_token;

        // 2. Construct the message payload for Firebase.
        const message = {
            notification: {
                title: title,
                body: body
            },
            token: token,
            data: orderData // This is the crucial data payload for your in-app notification.
        };

        // 3. Send the message using the Firebase Admin SDK.
        const response = await admin.messaging().send(message);
        console.log('Successfully sent push notification:', response);
    } catch (error) {
        // This will catch errors like an invalid or expired token.
        console.error('Error sending push notification:', error);
    }
};

// Export the function so it can be used in your other route files (like orders.js).
module.exports = { sendPushNotification };
