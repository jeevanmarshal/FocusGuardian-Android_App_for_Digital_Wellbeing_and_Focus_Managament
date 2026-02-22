# FocusGuardian

FocusGuardian is an innovative productivity and focus-enhancing application designed to help users manage digital distractions through AI-powered insights, active application monitoring, and personal engagement tracking. The project is strategically split into two primary components: a feature-packed Android Application for the client side, and a robust AI Backend service that handles complex analysis via the Gemini API.

## Project Structure

This repository is structured into distinct branches, each maintaining a specific core component of the FocusGuardian ecosystem. This separation of concerns allows for clean version control and modular development:

- **`android-app` branch**: Contains the complete source code for the FocusGuardian Android application. It handles app usage tracking, rule enforcement, user settings, accessibility services, and directly interacts with the AI backend to display "AI Insights".
- **`ai-backend` branch**: Contains the backend server code built with Node.js. It receives behavioral data from the core Android app and processes it using the Gemini API to generate personalized "AI Insights" for the user.

## Technical Details

FocusGuardian was developed to demonstrate comprehensive full-stack and mobile development skills, successfully integrating low-level Android system APIs (such as UsageStats and AccessibilityService for app monitoring) with a modern remote ML-powered backend ecosystem. 

**Key Highlights:**
- **Productivity & Focus:** A robust tracking and blocking mechanism to improve digital well-being.
- **AI Integration:** Implements the Gemini API to autonomously generate intelligent, actionable user feedback directly from usage metrics.
- **Clean Architecture:** The project emphasizes maintainability, scalable API connections, and neat, presentable code structure suitable for enterprise-level applications.

## Comprehensive Tech Stack & Packages

### 📱 Android Application (`android-app`)
Built with modern standardizations of Android development leveraging the following libraries:
- **Languages:** `Kotlin`, `XML`
- **Core UI & Logic:** `Jetpack Compose` (Material 3)
- **Asynchrony:** `Kotlin Coroutines` & `Flows`
- **Networking:** `Retrofit2`, `Gson`
- **Data Persistence:** `Room Database` (SQLite), `DataStore`
- **Background Tasks:** `WorkManager`
- **Image Handling:** `Coil`
- **System APIs:** `UsageStatsManager`, `AccessibilityService`

### ☁️ AI Backend Server (`ai-backend`)
A scalable microservice architecture leveraging standard REST protocol patterns:
- **Languages:** `JavaScript` (Node.js)
- **Runtime & Gateway:** `Node.js`, `Express.js`
- **AI Processing:** Google `@google/generative-ai` (Gemini API)
- **Security & Utilities:** `helmet` (HTTP securing), `joi` (validation), `cors`
- **Environment Management:** `dotenv`

## Project Language Breakdown
The approximate language composition across the entire FocusGuardian Monorepo ecosystem:
- 🟣 **Kotlin:** `78.5%` (Android UI, business logic, asynchronous data binding)
- 🟡 **JavaScript:** `12.0%` (Node.js AI Backend Controller and endpoint routing)
- 🟠 **XML:** `8.5%` (Android layouts, styling resources & configurations)
- ⚪ **Other:** `1.0%` (JSON schemas, Gradle scripts, configuration files)

## How to Navigate This Repository

To explore the respective parts of the project, please switch to the relevant branch:

```bash
# To view the Android App code
git checkout android-app

# To view the AI Backend server code
git checkout ai-backend
```

_This project was developed by Jeevan Marshal to showcase professional-grade software engineering, mobile development, and AI API integration skills._
