# FocusGuardian - AI Engine & Backend Service

Welcome to the server-side architecture of the FocusGuardian application! 

## Overview
This **Focus Guardian AI Backend** is the core cloud element ensuring full integration with our LLM intelligence layer, designed purposefully to generate comprehensive behavioral analysis metrics from user-supplied tracking analytics. The server provides a fast, resilient API architecture for processing detailed smartphone usage insights and outputting synthesized, human-readable qualitative data using the powerful Gemini API model schema. 

## Key Architecture Breakdown
1. **Gemini API Integration Layer:** Employs precise, multi-turn system prompt curation methodologies that contextually comprehend the metrics sent by the Android application module (such as raw screen times, application transitions, and user thresholds).
2. **RESTful Architecture:** Built natively on Node.js/Express, ensuring lightweight but powerful endpoint scaling capabilities. Designed cleanly using middleware error handling, modular routing patterns, and proper decoupled controller patterns.
3. **Environment Isolation Strategies:** Uses structured `.env` strategies securely to parse necessary API keys away from public repository viewing.
4. **Behavioral Insight Engine:** The controller receives unformatted `JSON` data blocks documenting long-session user interaction behaviors, securely formats and parses this data up to the AI pipeline, and processes the output into immediate real-time dashboard updates rendered back natively on the user's Android client app overlay dashboard.
5. **Robust Mock Services:** Complete mocking environment configured internally to mock complex analytical responses in case of rate-limiting or non-ideal downstream dependencies, enabling seamless development iterations. 

## Tech Stack & Packages Used
This backend serves as a scalable API microservice leveraging the following libraries and cloud tools:

- **Language**: `JavaScript`
- **Core Runtime**: `Node.js` v18+
- **Framework**: `Express.js` (`^5.2.1`) for robust REST API routing.
- **AI Integration**: `@google/generative-ai` (`^0.24.1`) - Google's official Gemini AI SDK.
- **Security & Validation**: 
  - `helmet` (`^8.1.0`) for securing HTTP headers.
  - `joi` (`^18.0.2`) for payload data validation.
  - `cors` (`^2.8.5`) for Cross-Origin Resource Sharing.
- **Environment & Logging**: `dotenv` (`^17.2.3`) for secret management and `morgan` (`^1.10.1`) for HTTP request logging.
- **Development Tools**: `nodemon` (`^3.1.11`) for hot-reloading.
- **Port Assignment:** Default 3000

## Quickstart

If deploying this backend locally for the Android app architecture implementation, run these commands globally:

```bash
# Clone the complete project environment
git clone <repository_url>
cd FocusGuardian_Final
git checkout ai-backend

# Install root dependencies
npm install

# Build environment file
cp .env.example .env

# Run server on localhost 
npm run dev 
# The application should now start accepting data inputs from the Kotlin Android system natively on your LAN. 
```

## For Recruiters 

This backend was built strictly by Jeevan Marshal. It demonstrates an advanced understanding of LLM interfacing capabilities, real-time JSON parsing mechanics over standard REST protocols, scalable microservices architectures, Node.js development standards, and modular implementation methodologies for highly functional frontend ecosystems.
