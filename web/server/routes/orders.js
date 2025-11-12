const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const pool = require('../db');
const { sendPushNotification } = require('../fcm-helper');

const router = express.Router();

// --- Helper function to generate a unique order code ---
const generateOrderCode = (shopId, usnId) => {
    const date = new Date();
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const randomFourDigits = Math.floor(1000 + Math.random() * 9000);
    const sanitizedUsn = usnId ? usnId.replace(/[^a-zA-Z0-9]/g, '') : 'NOUSN';

    return `EZP-${shopId}-${sanitizedUsn}-${year}${month}${day}-${hours}${minutes}-${randomFourDigits}`;
};


// --- File Upload Setup (Multer) ---
const uploadsDir = path.join(__dirname, '..', 'uploads');
fs.mkdirSync(uploadsDir, { recursive: true });

const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        if (!req.orderCode) {
            req.orderCode = generateOrderCode(req.body.shopId, req.body.usnId);
        }
        const orderUploadPath = path.join(uploadsDir, req.orderCode);
        fs.mkdirSync(orderUploadPath, { recursive: true });
        cb(null, orderUploadPath);
    },
    filename: function (req, file, cb) {
        const studentIdentifier = req.body.usnId ? req.body.usnId.replace(/[^a-zA-Z0-9]/g, '') : 'nousn';
        const fileType = file.fieldname === 'idProof' ? 'ID' : 'PRINT';
        const timestamp = Date.now();
        const originalExtension = path.extname(file.originalname);
        const newFilename = `shop${req.body.shopId}-${fileType}-${studentIdentifier}-${timestamp}${originalExtension}`;
        cb(null, newFilename);
    }
});

const upload = multer({ storage: storage });

// ==============================================================
// This api is for new Orders, when student places a new order ==
// It also sends notification via Firebase ======================
// ==============================================================

router.post('/submit', upload.fields([
    { name: 'idProof', maxCount: 1 },
    { name: 'printFiles', maxCount: 10 } 
]), async (req, res) => {
    
    const connection = await pool.getConnection();
    
    try {
        await connection.beginTransaction();

        const { fullName, usnId, phoneNo, emailId, shopId, additionalNotes } = req.body;
        let studentId;

        const [existingStudents] = await connection.execute(
            'SELECT student_id FROM students WHERE usn_or_id = ? OR phone = ? LIMIT 1',
            [usnId, phoneNo]
        );

        if (existingStudents.length > 0) {
            studentId = existingStudents[0].student_id;
        } else {
            const [newStudentResult] = await connection.execute(
                'INSERT INTO students (student_name, usn_or_id, email, phone) VALUES (?, ?, ?, ?)',
                [fullName, usnId, emailId || null, phoneNo]
            );
            studentId = newStudentResult.insertId;
        }

        const orderCode = req.orderCode;

        const [orderResult] = await connection.execute(
            `INSERT INTO orders (shop_id, student_id, order_code, notes) VALUES (?, ?, ?, ?)`,
            [shopId, studentId, orderCode, additionalNotes]
        );
        const orderId = orderResult.insertId;

        const { idProof, printFiles } = req.files;

        // --- Handle ID Proof ---
        if (idProof && idProof.length > 0) {
            const idProofFile = idProof[0];
            const idProofUrl = `uploads/${orderCode}/${idProofFile.filename}`;
            await connection.execute(
                'INSERT INTO documents (order_id, doc_type, file_url) VALUES (?, ?, ?)',
                [orderId, 'id_proof', idProofUrl]
            );
        }

        // --- Handle Print Files and Calculate Notification Details in ONE LOOP ---
        let totalCopies = 0;
        let hasColor = false;
        let hasBlackAndWhite = false;
        let filesMetadata = [];

        try {
            if (req.body.filesMetadata) {
                filesMetadata = JSON.parse(req.body.filesMetadata);
            }
        } catch (parseError) {
            throw new Error('Invalid filesMetadata JSON format.');
        }

        if (printFiles && printFiles.length > 0) {
            for (const file of printFiles) {
                const metadata = filesMetadata.find(m => m.originalName === file.originalname);
                
                if (!metadata) {
                    console.warn(`Could not find metadata for file: ${file.originalname}`);
                    continue; // Skip file if no metadata is found
                }

                // Calculate stats for the notification
                totalCopies += parseInt(metadata.copies, 10) || 0;
                if (metadata.options.isColor) hasColor = true;
                else hasBlackAndWhite = true;

                // Insert document into the database
                const { copies, options } = metadata;
                const fileUrl = `uploads/${orderCode}/${file.filename}`;
                
                await connection.execute(
                    `INSERT INTO documents (order_id, doc_type, file_url, copies, is_color, is_duplex, orientation, page_range) 
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
                    [orderId, 'print_file', fileUrl, copies, options.isColor, options.isDuplex, options.orientation, options.pageRange]
                );
            }
        }

        await connection.commit();
        
        console.log(`--- Order ${orderCode} saved successfully! ---`);

        // --- Prepare and Send Push Notification ---
        let printType = "N/A";
        if (hasColor && hasBlackAndWhite) printType = "Color/B&W";
        else if (hasColor) printType = "Color Only";
        else if (hasBlackAndWhite) printType = "B&W Only";

        const notificationTitle = 'New Order Received!';
        const notificationBody = `You have a new order (${orderCode}) from ${fullName}.`;
        
        const orderDataPayload = {
            orderId: String(orderId),
            orderCode: orderCode,
            customerName: fullName,
            fileCount: String(printFiles ? printFiles.length : 0),
            totalCopies: String(totalCopies),
            printType: printType
        };
        
        await sendPushNotification(shopId, notificationTitle, notificationBody, orderDataPayload);
        
        res.status(201).json({ 
            success: true, 
            message: 'Order submitted successfully!',
            orderCode: orderCode 
        });

    } catch (error) {
        await connection.rollback();
        console.error('Error processing order:', error);
        res.status(500).json({ 
            success: false, 
            message: 'An error occurred on the server while processing your order.' 
        });
    } finally {
        connection.release();
    }
});


// ===============================================
// GET /orders/recent/:shopId (UPDATED)
// API for fetching the 5 most recent orders for a shop
// ===============================================
router.get('/recent/:shopId', async (req, res) => {
    try {
        const { shopId } = req.params;

        // UPDATED QUERY: This query now correctly joins with the 'students' table
        // and calculates the file_count using a subquery on the 'documents' table.
        const [orders] = await pool.execute(
            `SELECT 
                o.order_id, 
                o.order_code AS order_code, 
                s.student_name AS student_name, 
                o.status,
                (SELECT COUNT(*) FROM documents d WHERE d.order_id = o.order_id AND d.doc_type = 'print_file') AS file_count
             FROM orders o
             JOIN students s ON o.student_id = s.student_id
             WHERE o.shop_id = ?
             ORDER BY o.created_at DESC
             LIMIT 5`,
            [shopId]
        );

        res.json({
            ok: true,
            orders: orders
        });

    } catch (err) {
        console.error('Error /orders/recent/:shopId:', err);
        res.status(500).json({ error: 'Server error fetching recent orders.' });
    }
});



// ===============================================
// GET /orders/details/:orderId (UPDATED)
// Now includes the 'is_printed' status for each document
// ===============================================
router.get('/details/:orderId', async (req, res) => {
    try {
        const { orderId } = req.params;

        const [orderRows] = await pool.execute(
            `SELECT o.*, s.student_name, s.usn_or_id, s.email AS student_email, s.phone AS student_phone
             FROM orders o JOIN students s ON o.student_id = s.student_id
             WHERE o.order_id = ?`,
            [orderId]
        );

        if (orderRows.length === 0) {
            return res.status(404).json({ error: 'Order not found.' });
        }
        
        // This query now also selects the 'is_printed' column
        const [documentRows] = await pool.execute(
            'SELECT * FROM documents WHERE order_id = ?',
            [orderId]
        );

        const orderDetails = { ...orderRows[0], documents: documentRows };
        
        res.json({ ok: true, order: orderDetails });

    } catch (err) {
        console.error('Error /orders/details/:orderId:', err);
        res.status(500).json({ error: 'Server error fetching order details.' });
    }
});

// ===============================================
// PUT /orders/documents/:documentId/mark-printed
// NEW API to update the print status of a single document
// ===============================================
router.put('/documents/:documentId/mark-printed', async (req, res) => {
    try {
        const { documentId } = req.params;

        const [result] = await pool.execute(
            'UPDATE documents SET is_printed = 1 WHERE document_id = ?',
            [documentId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: 'Document not found.' });
        }

        res.json({ ok: true, message: 'Document marked as printed.' });

    } catch (err) {
        console.error('Error marking document as printed:', err);
        res.status(500).json({ error: 'Server error updating document status.' });
    }
});


// ===============================================
// PUT /orders/update-status/:orderId (REPLACES /complete-payment)
// Updates an order's status and optionally the final amount.
// ===============================================
router.put('/update-status/:orderId', async (req, res) => {
    try {
        const { orderId } = req.params;
        const { status, amount } = req.body;

        // Validate the incoming status
        const allowedStatuses = ['accepted', 'printing', 'ready', 'completed', 'rejected', 'cancelled'];
        if (!status || !allowedStatuses.includes(status)) {
            return res.status(400).json({ error: 'A valid status is required.' });
        }

        let query = 'UPDATE orders SET status = ?';
        const queryParams = [status];

        // If the order is being marked as 'completed', also update the amount and payment status
        if (status === 'completed') {
            if (amount == null) {
                return res.status(400).json({ error: 'Final amount is required for completed orders.' });
            }
            query += ", amount = ?, payment_status = 'paid'";
            queryParams.push(amount);
        }

        query += ' WHERE order_id = ?';
        queryParams.push(orderId);

        const [result] = await pool.execute(query, queryParams);

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: 'Order not found.' });
        }

        res.json({ ok: true, message: `Order status updated to ${status}.` });

    } catch (err) {
        console.error('Error /orders/update-status:', err);
        res.status(500).json({ error: 'Server error updating order status.' });
    }
});


// ===============================================
// GET /orders/all/:shopId?filter=...
// API for fetching all orders with a time filter
// ===============================================
router.get('/all/:shopId', async (req, res) => {
    try {
        const { shopId } = req.params;
        const { filter } = req.query; // 'daily', 'weekly', or 'monthly'

        let dateFilterClause = '';
        switch (filter) {
            case 'weekly':
                // Fetches orders from the last 7 days
                dateFilterClause = 'AND o.created_at >= CURDATE() - INTERVAL 7 DAY';
                break;
            case 'monthly':
                // Fetches orders from the current month
                dateFilterClause = 'AND MONTH(o.created_at) = MONTH(CURDATE()) AND YEAR(o.created_at) = YEAR(CURDATE())';
                break;
            case 'daily':
            default:
                // Fetches orders from today
                dateFilterClause = 'AND DATE(o.created_at) = CURDATE()';
                break;
        }

        const [orders] = await pool.execute(
            `SELECT 
                o.order_id, 
                o.order_code AS order_code, 
                s.student_name AS student_name, 
                o.status,
                o.created_at,
                (SELECT COUNT(*) FROM documents d WHERE d.order_id = o.order_id AND d.doc_type = 'print_file') AS file_count
             FROM orders o
             JOIN students s ON o.student_id = s.student_id
             WHERE o.shop_id = ? ${dateFilterClause}
             ORDER BY o.created_at DESC`,
            [shopId]
        );

        res.json({
            ok: true,
            orders: orders
        });

    } catch (err) {
        console.error('Error /orders/all/:shopId:', err);
        res.status(500).json({ error: 'Server error fetching all orders.' });
    }
});



// ===============================================
// GET /orders/pending/:shopId
// NEW API for fetching all orders with a 'pending' status
// ===============================================
router.get('/pending/:shopId', async (req, res) => {
    try {
        const { shopId } = req.params;

        const [orders] = await pool.execute(
            `SELECT 
                o.order_id, 
                o.order_code AS order_code, 
                s.student_name AS student_name, 
                o.status,
                o.created_at,
                (SELECT COUNT(*) FROM documents d WHERE d.order_id = o.order_id AND d.doc_type = 'print_file') AS file_count
             FROM orders o
             JOIN students s ON o.student_id = s.student_id
             WHERE o.shop_id = ? AND o.status = 'pending'
             ORDER BY o.created_at DESC`,
            [shopId]
        );

        res.json({ ok: true, orders: orders });

    } catch (err) {
        console.error('Error /orders/pending/:shopId:', err);
        res.status(500).json({ error: 'Server error fetching pending orders.' });
    }
});

// ===============================================
// DELETE /orders/:orderId
// NEW API for deleting an order
// ===============================================
router.delete('/:orderId', async (req, res) => {
    try {
        const { orderId } = req.params;

        // Note: The ON DELETE CASCADE constraint in your database will automatically
        // delete all associated documents when an order is deleted.
        const [result] = await pool.execute(
            'DELETE FROM orders WHERE order_id = ?',
            [orderId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: 'Order not found.' });
        }

        res.json({ ok: true, message: 'Order deleted successfully.' });

    } catch (err) {
        console.error('Error DELETE /orders/:orderId:', err);
        res.status(500).json({ error: 'Server error deleting order.' });
    }
});


module.exports = router;