// shops.js
const express = require('express');
const router = express.Router();
const pool = require('../db');
const bcrypt = require('bcryptjs');
const multer = require('multer');
const path = require('path');
const qrcode = require('qrcode');
const fs = require('fs');

// ================================
// POST /shops/register
router.post('/register', async (req, res) => {
    try {
        const { shop_name, owner_name, email, phone, password, address } = req.body;

        if (!shop_name || !owner_name || !phone || !password) {
            return res.status(400).json({ error: 'shop_name, owner_name, phone and password are required' });
        }

        // check if phone or email already exists
        // 1. We now select the phone and email of the conflicting row
        const [existing] = await pool.execute(
            'SELECT phone, email FROM shops WHERE phone = ? OR email = ? LIMIT 1',
            [phone, email || '']
        );

        if (existing.length > 0) {
            // 2. Now we check WHICH field caused the conflict
            const conflict = existing[0];
            if (conflict.phone === phone) {
                return res.status(400).json({ error: 'A shop with this phone number already exists' });
            }
            if (email && conflict.email === email) {
                return res.status(400).json({ error: 'A shop with this email address already exists' });
            }
        }

        // hash password
        const salt = await bcrypt.genSalt(10);
        const password_hash = await bcrypt.hash(password, salt);

        // insert shop
        const [result] = await pool.execute(
            'INSERT INTO shops (shop_name, owner_name, email, phone, password_hash, address) VALUES (?, ?, ?, ?, ?, ?)',
            [shop_name, owner_name, email || null, phone, password_hash, address || null]
        );

        res.json({ ok: true, shop_id: result.insertId, message: 'Shop registered successfully' });
    } catch (err) {
        console.error('Error /shops/register:', err);
        res.status(500).json({ error: 'Server error' });
    }
});


// ====================================
// POST /shops/login
router.post('/login', async (req, res) => {
    try {
        const { phone, password } = req.body;

        if (!phone || !password) {
            return res.status(400).json({ error: 'Phone and password are required' });
        }

        // find shop by phone
        const [rows] = await pool.execute(
            'SELECT shop_id, shop_name, owner_name, phone, email, password_hash, address, qr_code_url, profile_img FROM shops WHERE phone = ? LIMIT 1',
            [phone]
        );

        if (rows.length === 0) {
            return res.status(400).json({ error: 'Shop not found' });
        }

        const shop = rows[0];

        // compare password
        const isMatch = await bcrypt.compare(password, shop.password_hash);
        if (!isMatch) {
            return res.status(400).json({ error: 'Invalid password' });
        }

        // return shop info
        res.json({
            ok: true,
            shop_id: shop.shop_id,
            shop_name: shop.shop_name,
            owner_name: shop.owner_name,
            email: shop.email,
            phone: shop.phone,
            address: shop.address,
            profile_img: shop.profile_img,
            qr_code_url: shop.qr_code_url,
            message: 'Login successful'
        });

    } catch (err) {
        console.error('Error /shops/login:', err);
        res.status(500).json({ error: 'Server error' });
    }
});


// =========================================
// --- GET /api/shops/details/:id ---
// Fetches details for a single shop, now including all price settings
router.get('/details/:id', async (req, res) => {
    try {
        const { id } = req.params;

        if (isNaN(id)) {
            return res.status(400).json({ success: false, message: 'Invalid shop ID format.' });
        }

        // UPDATED QUERY: Join shops with price_settings to get all four price points
        const [rows] = await pool.execute(
            `SELECT 
                s.shop_id, s.shop_name, s.owner_name, s.address,
                ps.price_per_bw_page, ps.price_per_color_page,
                ps.price_per_bw_duplex, ps.price_per_color_duplex
             FROM shops s
             LEFT JOIN price_settings ps ON s.shop_id = ps.shop_id
             WHERE s.shop_id = ?`,
            [id]
        );

        if (rows.length === 0) {
            return res.status(404).json({ success: false, message: 'Shop not found' });
        }

        res.json({ success: true, shop: rows[0] });

    } catch (error) {
        console.error('Error fetching shop details:', error);
        res.status(500).json({ success: false, message: 'Database error while fetching shop details.' });
    }
});



// ================ Images =======================
// --- Multer Configuration for Image Uploads ---
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, 'uploads/profile/');
    },
    filename: function (req, file, cb) {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
    }
});

const upload = multer({ storage: storage });

// ===============================================
// POST /shops/profile-picture/:shopId
// API for uploading a profile picture
// ===============================================
router.post('/profile-picture/:shopId', upload.single('profile_pic'), async (req, res) => {
    try {
        const { shopId } = req.params;

        if (!req.file) {
            return res.status(400).json({ error: 'No file uploaded.' });
        }

        // The path to the file as it will be accessed from the server URL
        const imageUrl = `uploads/profile/${req.file.filename}`;

        // Update the database with the new image URL
        const [result] = await pool.execute(
            'UPDATE shops SET profile_img = ? WHERE shop_id = ?',
            [imageUrl, shopId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: 'Shop not found.' });
        }

        res.json({
            ok: true,
            message: 'Profile picture updated successfully.',
            imageUrl: imageUrl
        });

    } catch (err) {
        console.error('Error /shops/profile-picture:', err);
        res.status(500).json({ error: 'Server error during file upload.' });
    }
});


// ===============================================
// PUT /shops/profile/:shopId (UPDATED)
// API for updating a shop's profile details
// ===============================================
router.put('/profile/:shopId', async (req, res) => {
    try {
        const { shopId } = req.params;
        const { shop_name, owner_name, email, phone, address } = req.body;

        // Basic validation
        if (!shop_name || !owner_name || !phone) {
            return res.status(400).json({ error: 'Shop name, owner name, and phone are required' });
        }

        // --- MODIFICATION START ---
        // Step 1: Check if the new phone number conflicts with ANOTHER user
        const [existingPhone] = await pool.execute(
            'SELECT shop_id FROM shops WHERE phone = ? AND shop_id != ? LIMIT 1',
            [phone, shopId]
        );

        if (existingPhone.length > 0) {
            return res.status(409).json({ error: 'This phone number is already in use by another shop.' });
        }

        // Step 2: Check if the new email (if provided) conflicts with ANOTHER user
        if (email) {
            const [existingEmail] = await pool.execute(
                'SELECT shop_id FROM shops WHERE email = ? AND shop_id != ? LIMIT 1',
                [email, shopId]
            );
            if (existingEmail.length > 0) {
                return res.status(409).json({ error: 'This email is already in use by another shop.' });
            }
        }
        // --- MODIFICATION END ---


        // Update the database
        const [result] = await pool.execute(
            'UPDATE shops SET shop_name = ?, owner_name = ?, email = ?, phone = ?, address = ? WHERE shop_id = ?',
            [shop_name, owner_name, email || null, phone, address || null, shopId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: 'Shop not found.' });
        }

        res.json({
            ok: true,
            message: 'Profile updated successfully.'
        });

    } catch (err) {
        console.error('Error /shops/profile/:shopId:', err);
        res.status(500).json({ error: 'Server error during profile update.' });
    }
});


// ===============================================
// POST /shops/generate-qr/:shopId
// API for generating and saving a QR code for a shop
// ===============================================
router.post('/generate-qr/:shopId', async (req, res) => {
    try {
        const { shopId } = req.params;

        // The URL that the QR code will point to.
        const orderUrl = `${process.env.CLIENT_BASE_URL}/order/${shopId}`;

        const qrCodeFileName = `shop-qr-${shopId}-${Date.now()}.png`;
        const qrCodePath = `uploads/qr-codes/${qrCodeFileName}`;
        const qrCodeUrl = `uploads/qr-codes/${qrCodeFileName}`;

        // Generate and save the QR code image file
        await qrcode.toFile(qrCodePath, orderUrl, {
            color: {
                dark: '#000000',  // Black dots
                light: '#FFFFFFFF' // White background
            }
        });

        // Update the database with the new QR code URL
        const [result] = await pool.execute(
            'UPDATE shops SET qr_code_url = ? WHERE shop_id = ?',
            [qrCodeUrl, shopId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: 'Shop not found.' });
        }

        res.json({
            ok: true,
            message: 'QR Code generated successfully.',
            qrCodeUrl: qrCodeUrl
        });

    } catch (err) {
        console.error('Error /shops/generate-qr:', err);
        res.status(500).json({ error: 'Server error during QR code generation.' });
    }
});

// ===============================================
// GET /orders/search/:shopId?q=...&status=... (UPDATED)
// API for searching orders with text and status filters
// ===============================================
router.get('/search/:shopId', async (req, res) => {
    try {
        const { shopId } = req.params;
        const { q, status } = req.query; // Get search query and status filter

        let query = `
            SELECT 
                o.order_id, 
                o.order_code AS order_code, 
                s.student_name AS student_name, 
                o.status,
                o.created_at,
                o.amount,
                (SELECT COUNT(*) FROM documents d WHERE d.order_id = o.order_id AND d.doc_type = 'print_file') AS file_count
            FROM orders o
            JOIN students s ON o.student_id = s.student_id
            WHERE o.shop_id = ?`;
        
        const queryParams = [shopId];

        // Add text search condition if query 'q' exists
        if (q && q.trim() !== '') {
            query += ' AND (o.order_code LIKE ? OR s.student_name LIKE ?)';
            const searchQuery = `%${q}%`;
            queryParams.push(searchQuery, searchQuery);
        }

        // Add status filter condition if 'status' exists
        if (status && status.trim() !== '') {
            query += ' AND o.status = ?';
            queryParams.push(status);
        }

        query += ' ORDER BY o.created_at DESC';

        const [orders] = await pool.execute(query, queryParams);

        res.json({ ok: true, orders: orders });

    } catch (err) {
        console.error('Error /orders/search/:shopId:', err);
        res.status(500).json({ error: 'Server error during order search.' });
    }
});


// ===============================================
// GET /shops/prices/:shopId
// API for fetching the current price settings for a shop
// ===============================================
router.get('/prices/:shopId', async (req, res) => {
    try {
        const { shopId } = req.params;
        const [rows] = await pool.execute(
            'SELECT * FROM price_settings WHERE shop_id = ? LIMIT 1',
            [shopId]
        );

        if (rows.length === 0) {
            // If no settings exist, you might return default values or a specific status
            return res.status(404).json({ error: 'Price settings not found for this shop.' });
        }
        res.json({ ok: true, prices: rows[0] });
    } catch (err) {
        console.error('Error /shops/prices/:shopId:', err);
        res.status(500).json({ error: 'Server error fetching prices.' });
    }
});

// ===============================================
// PUT /shops/prices/:shopId
// API for updating price settings for a shop
// ===============================================
router.put('/prices/:shopId', async (req, res) => {
    try {
        const { shopId } = req.params;
        const {
            price_per_bw_page,
            price_per_color_page,
            price_per_bw_duplex,
            price_per_color_duplex
        } = req.body;

        // Basic validation
        if (price_per_bw_page == null || price_per_color_page == null || price_per_bw_duplex == null || price_per_color_duplex == null) {
            return res.status(400).json({ error: 'All price fields are required.' });
        }

        const [result] = await pool.execute(
            `UPDATE price_settings SET 
                price_per_bw_page = ?, 
                price_per_color_page = ?, 
                price_per_bw_duplex = ?, 
                price_per_color_duplex = ? 
             WHERE shop_id = ?`,
            [price_per_bw_page, price_per_color_page, price_per_bw_duplex, price_per_color_duplex, shopId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: 'Shop not found.' });
        }

        res.json({ ok: true, message: 'Prices updated successfully.' });
    } catch (err) {
        console.error('Error updating /shops/prices/:shopId:', err);
        res.status(500).json({ error: 'Server error updating prices.' });
    }
});


// ===============================================
// GET /shops/dashboard-stats/:shopId
// API for fetching all key stats for the dashboard
// ===============================================
router.get('/dashboard-stats/:shopId', async (req, res) => {
    try {
        const { shopId } = req.params;

        // Query for This Month's Stats
        const [currentMonthStats] = await pool.execute(
            `SELECT
                COUNT(CASE WHEN DATE(created_at) = CURDATE() THEN 1 END) AS todays_total_orders,
                SUM(CASE WHEN DATE(created_at) = CURDATE() AND status = 'pending' THEN 1 ELSE 0 END) AS pending_orders,
                SUM(CASE WHEN DATE(created_at) = CURDATE() AND (status = 'accepted' OR status = 'printing') THEN 1 ELSE 0 END) AS in_progress_orders,
                SUM(CASE WHEN DATE(created_at) = CURDATE() AND status = 'completed' THEN 1 ELSE 0 END) AS completed_orders,
                SUM(CASE WHEN DATE(created_at) = CURDATE() AND payment_status = 'paid' THEN amount ELSE 0 END) AS todays_revenue,
                COUNT(*) as monthly_orders,
                (SELECT COUNT(*) FROM (SELECT student_id FROM orders WHERE shop_id = ? AND MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE()) GROUP BY student_id) as new_cust) AS new_customers
            FROM orders
            WHERE shop_id = ? AND MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE())`,
            [shopId, shopId]
        );

        // Query for Last Month's Revenue
        const [lastMonthRevenue] = await pool.execute(
            `SELECT SUM(CASE WHEN payment_status = 'paid' THEN amount ELSE 0 END) AS last_month_revenue
             FROM orders
             WHERE shop_id = ? AND MONTH(created_at) = MONTH(CURDATE() - INTERVAL 1 MONTH) AND YEAR(created_at) = YEAR(CURDATE() - INTERVAL 1 MONTH)`,
            [shopId]
        );

        // Calculate Percentage Change
        const currentRevenue = currentMonthStats[0].todays_revenue || 0;
        const previousRevenue = lastMonthRevenue[0].last_month_revenue || 0;
        let percentageChange = 0;
        if (previousRevenue > 0) {
            percentageChange = ((currentRevenue - previousRevenue) / previousRevenue) * 100;
        } else if (currentRevenue > 0) {
            percentageChange = 100; // Handle case where last month was 0
        }
        
        // Combine all stats into a single object
        const finalStats = {
            ...currentMonthStats[0],
            revenue_change_percentage: percentageChange
        };

        res.json({
            ok: true,
            stats: finalStats
        });

    } catch (err) {
        console.error('Error /shops/dashboard-stats/:shopId:', err);
        res.status(500).json({ error: 'Server error fetching dashboard stats.' });
    }
});



// ===============================================
// POST /shops/register-device
// API to save a shopkeeper's FCM device token
// ===============================================
router.post('/register-device', async (req, res) => {
    try {
        const { shopId, fcmToken } = req.body;
        if (!shopId || !fcmToken) {
            return res.status(400).json({ error: 'Shop ID and FCM token are required.' });
        }

        // "Upsert" logic: This safely inserts a new token or updates the
        // token if a record for this user already exists.
        await pool.execute(
            `INSERT INTO devices (user_type, user_id, fcm_token) 
             VALUES ('shopkeeper', ?, ?) 
             ON DUPLICATE KEY UPDATE fcm_token = ?`,
            [shopId, fcmToken, fcmToken]
        );

        res.json({ ok: true, message: 'Device registered successfully.' });
    } catch (err) {
        console.error('Error /shops/register-device:', err);
        res.status(500).json({ error: 'Server error registering device.' });
    }
});



// ===============================================
// DELETE /shops/unregister-device
// API to remove a shopkeeper's FCM device token upon logout
// ===============================================
router.delete('/unregister-device', async (req, res) => {
    try {
        const { shopId, fcmToken } = req.body;
        if (!shopId || !fcmToken) {
            return res.status(400).json({ error: 'Shop ID and FCM token are required.' });
        }

        // Delete the specific token for the given shop user
        await pool.execute(
            'DELETE FROM devices WHERE user_id = ? AND user_type = "shopkeeper" AND fcm_token = ?',
            [shopId, fcmToken]
        );

        res.json({ ok: true, message: 'Device unregistered successfully.' });
    } catch (err) {
        console.error('Error /shops/unregister-device:', err);
        res.status(500).json({ error: 'Server error unregistering device.' });
    }
});


module.exports = router;