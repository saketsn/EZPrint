require('dotenv').config();
const express = require('express');
const cors = require('cors');
const path = require('path');
const pool = require('./db');

const app = express();
const PORT = process.env.PORT || 3000;

// --- Middleware ---
app.use(cors()); 
app.use(express.json()); 
app.use(express.urlencoded({ extended: true }));

// --- API Routes ---
const shopsRouter = require('./routes/shops');
const ordersRouter = require('./routes/orders');
app.use('/api/shops', shopsRouter);
app.use('/api/orders', ordersRouter);

// --- Static File Serving ---
app.use('/student', express.static(path.join(__dirname, '..', 'student')));
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));
app.use('/uploads/profile', express.static(path.join(__dirname, 'uploads/profile')));

// --- NEW: Route for Clean Student URL ---
app.get('/order/:shopId', (req, res) => {
    res.sendFile(path.join(__dirname, '..', 'student', 'index.html'));
});


// --- Root route for testing ---
app.get('/', (req, res) => {
    res.send('EZPrint Server is running!');
});

// Start server accessible on local network
app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running at http://0.0.0.0:${PORT}`);
    console.log(`Student app should be accessed via a URL like: http://<your-local-ip>:${PORT}/order/1`);
});