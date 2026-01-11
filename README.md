# Focus Guardian Backend

A production-ready Node.js/Express backend for the Focus Guardian Android App, powered by Google Gemini AI.

## 🚀 Features

- **Usage Analysis Engine**: Analyzes behavioral patterns and focus scores.
- **AI Insight Generator**: Generates concise, context-aware insights using Gemini Pro.
- **Alert Orchestration**: Hybrid rule-based + AI logic for personalized alerts.
- **Production Ready**: Configured for Render Free Tier deployment.

## 🛠 Tech Stack

- **Runtime**: Node.js 18+
- **Framework**: Express.js
- **AI**: Google Gemini API (`@google/generative-ai`)
- **Validation**: Joi
- **Security**: Helmet, CORS, Dotenv

## 📂 Project Structure

```
/src
 ├── index.js                  # App Entry Point
 ├── config/env.js             # Environment Config
 ├── controllers/              # Request Handlers
 ├── routes/                   # API Routes
 ├── services/                 # Business Logic (AI, Analysis)
 └── utils/                    # Prompts & Validators
```

## ⚡ Local Setup

1.  **Clone & Install**
    ```bash
    npm install
    # Note: If nodemon is not installed globally, run: npm install -D nodemon
    ```

2.  **Environment Configuration**
    - Create a `.env` file in the root directory.
    - Copy contents from `.env.example`.
    - Add your Gemini API Key.

    ```bash
    cp .env.example .env
    ```

3.  **Run Locally**
    ```bash
    npm run dev   # Development mode with auto-restart
    # OR
    npm start     # Production mode
    ```

4.  **Testing via Postman**
    - **URL**: `http://localhost:3000`
    - Use the mock payloads in the `/mocks` directory.

## 📡 API Endpoints

### 1. Dashboard Insight
- **POST** `/ai/dashboard-insight`
- **Body**: See `mocks/dashboard_high_distraction.json`

### 2. Alert Generation
- **POST** `/ai/alert-message`
- **Body**: See `mocks/alert_strict_violation.json`

### 3. Weekly Review
- **POST** `/ai/weekly-review`
- **Body**: See `mocks/weekly_review.json`

## 🌍 Deployment (Render)

This project is pre-configured for **Render Web Service (Free Tier)**.

1.  Push code to GitHub/GitLab.
2.  Create a new **Web Service** on Render.
3.  Connect the repository.
4.  **Settings**:
    - **Runtime**: Node
    - **Build Command**: `npm install`
    - **Start Command**: `npm start`
5.  **Environment Variables** (Add these in Render Dashboard):
    - `GEMINI_API_KEY`: [Your Key]
    - `NODE_ENV`: `production`

No code changes are required for deployment.
