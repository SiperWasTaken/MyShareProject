# ShareProject

An Android app developed in my free time as a personal project.

## Description

ShareProject is an app in early development that allows users to:
- Create and organize custom folders
- Upload photos and documents
- Extract text automatically using OCR (Optical Character Recognition)
- Manage digitized documents with a secure authentication system

### Main Use Case
The app was designed to create a dedicated space where:
- **Freelancers** can send invoices and receipts to their accountants
- **Accountants** receive documents with fields already extracted by OCR and AI, making accounting easier

## Current Features

- **User Authentication** (Login/Logout) using Firebase Authentication
- **Folder Creation** organized in a hierarchy
- **File Upload** from camera, gallery, or file system
- **Integrated OCR** for extracting text from images
- **Data Storage** on Firebase Firestore

## Technologies Used

- **Language:** Kotlin
- **Framework:** Android SDK
- **Backend:** Firebase
  - Firebase Authentication (user management)
  - Firebase Firestore (database for folders and OCR data)
- **ML:** ML Kit (OCR)

## Future Updates

- **Improve UI/UX**
- **Share folders between users**
- **AI Agent Integration** to process extracted data and standardize it into JSON format
- **Full PDF Support** (currently limited)
- **Notification System** for shared documents
- **Analytics Dashboard** for accountants

## Project Structure

```
share/
├── app/
│   ├── src/main/java/com/example/shareproject/
│   │   ├── data/
│   │   │   ├── model/          # Data classes (Folder, etc.)
│   │   │   └── repository/     # Repository pattern
│   │   ├── ui/
│   │   │   ├── home/           # Home screen with folder management
│   │   │   ├── login/          # Login screen
│   │   │   ├── registrazione/  # User registration
│   │   │   ├── ricerca/        # Search feature
│   │   │   └── settings/       # Settings
│   │   └── utils/
│   │       ├── OcrHelper.kt    # OCR helper
│   │       └── GeminiHelper.kt # (TODO: Gemini AI integration)
│   └── src/main/res/           # Resources (layout, drawable, strings)
└── gradle/                     # Gradle configuration
```

## Setup and Build

### Requirements
- Android Studio Hedgehog | 2023.1.1 or newer
- JDK 11+
- Android SDK 24+ (minSdk)
- Firebase account configured

## 📝 Notes

This is a personal project developed in my free time for learning and personal use.

The app stores user data in Firebase, but **the app will not run as-is** because required configuration files and API keys have been removed for security reasons. To run this project, you will need to set up your own Firebase project and add the necessary credentials.