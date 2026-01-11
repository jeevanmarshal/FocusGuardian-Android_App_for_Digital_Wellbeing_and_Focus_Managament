const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const config = require('./config/env');
const aiRoutes = require('./routes/ai.routes');

const app = express();

// Security & Middleware
app.use(helmet());
app.use(cors()); // Allow all for now to support Android Emulator + Localhost
app.use(express.json());
app.use(morgan('dev')); // 'dev' for local, 'combined' for prod usually

// Routes
app.use('/ai', aiRoutes);

// Health Check
app.get('/', (req, res) => {
    res.json({ status: 'active', service: 'Focus Guardian Backend', version: '1.0.0' });
});

// 404 Handler
app.use((req, res) => {
    res.status(404).json({ error: 'Endpoint not found' });
});

// Start Server
if (require.main === module) {
    app.listen(config.port, () => {
        console.log(`\n🚀 Focus Guardian Backend running on port ${config.port}`);
        console.log(`👉 Health Check: http://localhost:${config.port}/`);
        console.log(`👉 AI Dashboard: http://localhost:${config.port}/ai/dashboard-insight`);
    });
}

module.exports = app; // Export for testing
