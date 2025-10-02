# Emotion📱
*A modern Android application for administering the Depression, Anxiety, and Stress Scale (DASS-42), built with Jetpack Compose and Firebase.*

![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blueviolet)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-%F0%9F%92%BB-blue)
![Firebase](https://img.shields.io/badge/Firebase-%F0%9F%94%A5-orange)
---

## ✨ Features
- ✅ **Secure Authentication** – Firebase Authentication for user registration & login.  
- ✅ **DASS-42 Questionnaire** – Interactive 42-question assessment with a clean Compose UI.  
- ✅ **Automated Scoring** – Calculates Depression, Anxiety, and Stress subscales instantly.  
- ✅ **Result Persistence** – Stores user results in **Cloud Firestore** for history tracking.  
- ✅ **Modern UI** – Fully declarative interface with **Material Design 3**.  
- ✅ **Single-Activity Architecture** – Powered by **Navigation Compose** for smooth transitions.  

---

## 📸 Screenshots
| Login Screen | Questionnaire | Results |
|--------------|---------------|---------|
![WhatsApp Image 2025-10-02 at 12 13 30_0f28f150](https://github.com/user-attachments/assets/d977cb8d-135c-48e4-8f92-eda5d3016e15)
![WhatsApp Image 2025-10-02 at 12 13 31_e5de91c8](https://github.com/user-attachments/assets/6a46186e-e0fa-4bae-bf7a-f2fbbf566ba6)
![WhatsApp Image 2025-10-02 at 12 13 52_020d5215](https://github.com/user-attachments/assets/af1b980c-3937-49d3-a78b-6ef1738b8fbe)
---

## 🛠 Tech Stack
- **Language:** Kotlin  
- **UI Toolkit:** Jetpack Compose + Material Design 3  
- **Architecture:** MVVM + Navigation Compose  
- **Async:** Kotlin Coroutines & Flow  
- **Backend & Database:** Firebase Authentication + Cloud Firestore  
- **Dependency Injection:** Hilt (recommended)  

---

## 📐 Architecture Overview
```mermaid
flowchart TD
    A[UI - Jetpack Compose] --> B[ViewModel - MVVM]
    B --> C[Repository Layer]
    C --> D[Firebase Authentication]
    C --> E[Cloud Firestore]
