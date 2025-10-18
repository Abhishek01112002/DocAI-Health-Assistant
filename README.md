![DocAI Banner](./docai-banner.png)

# 🧠 DocAI — Real-time Health & Medical Assistant by Abhishek

## 📋 Overview
**DocAI** is an intelligent healthcare assistant that integrates **Google Fit API**, **AI**, and **real-time monitoring** to provide personalized health insights, diet recommendations, and emergency alerts.  
It was developed during the **National Hackathon 2025**, combining **AI + IoT + Android** technologies to build a futuristic healthcare ecosystem.

---

## 🚀 Key Features

### 🩺 1. Real-Time Health Monitoring
- Fetches live data from **smartwatches** via **Google Fit API**.
- Tracks:
  - Heart Rate (HRV)
  - Blood Pressure
  - Body Temperature
  - Calories Burned
  - Sleep Patterns

### 📊 2. AI-Driven Analytics
- Detects **anomalies** like sudden HRV spikes or high BP.
- Gives **real-time alerts** using ML inference.
- Recommends **lifestyle adjustments** dynamically.

### 🍽️ 3. Smart Food Recommendations
- Suggests meals for **breakfast, lunch, dinner** based on:
  - Current vitals
  - Activity level
  - Calorie goal
- Uses a lightweight AI model for nutritional guidance.

### ⚠️ 4. Emergency Response System
- Detects medical emergencies automatically.
- Fetches **current location** using GPS.
- Displays nearby:
  - 🏥 Hospitals  
  - 💊 Pharmacies  
  - 🚑 Clinics  
- Provides **voice-based alert** recommending doctor consultation.

### 📄 5. Document Intelligence (DocAI Core)
- Scans and analyzes **medical documents, prescriptions, and reports**.
- Extracts:
  - Patient details  
  - Diagnoses  
  - Medicines and Dosages  
- Stores data securely for future reference.

---

## 🧩 Tech Stack

| Component | Technology Used |
|------------|------------------|
| **Mobile App** | Kotlin (Android Studio) |
| **Backend** | Python (Flask / FastAPI) |
| **AI/ML Models** | Tesseract OCR, NLP, Scikit-learn |
| **APIs** | Google Fit API, Google Maps API |
| **Database** | Firebase / MongoDB |
| **Tools** | GitHub, VS Code, Android Studio, Jupyter Notebook |

---

## 🏗️ System Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   FireBoltt     │    │     DaFit       │    │   Google Fit    │
│   Smartwatch    │───▶│      App        │───▶│      API        │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   DocAI App     │◀───│   AI Analytics  │◀───│  Health Data    │
│   (Android)     │    │   Engine        │    │  Processing     │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  User Interface │    │  Emergency     │    │  Document       │
│  & Dashboard    │    │  Response      │    │  Intelligence   │
│                 │    │  System        │    │  Module         │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## 📱 App Features

### 🏠 Home Dashboard
- **Real-time health metrics** display
- **Personalized greetings** based on time of day
- **Health insights** with last updated timestamps
- **Quick access** to all major features

### 📊 Health Data Tracking
- **Steps Count**: Daily step tracking with deltas
- **Heart Rate**: Average BPM monitoring
- **Calories**: Total calories burned
- **Distance**: Traveled distance in kilometers
- **Comprehensive Summary**: All health data in one view

### 🔗 Google Fit Integration
- **Seamless authentication** with Google Sign-In
- **Permission management** for activity recognition
- **Real-time data sync** from smartwatches
- **Error handling** with user-friendly messages

### 🏥 Consultancy Module
- **Medical consultation** booking
- **Doctor recommendations** based on health data
- **Appointment scheduling** system

### 📱 Device Management
- **Smartwatch connectivity** management
- **Device pairing** and configuration
- **Sync status** monitoring

### 🍎 Diet Planning
- **AI-powered meal recommendations**
- **Nutritional analysis** based on health metrics
- **Calorie tracking** and goal setting

---

## 🛠️ Technical Implementation

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Repository Pattern
- **Database**: Room Database for local storage
- **Async Operations**: Kotlin Coroutines
- **API Integration**: Google Fit API v21.2.0

### Key Dependencies
```kotlin
// Google Fit & Authentication
implementation("com.google.android.gms:play-services-auth:21.2.0")
implementation("com.google.android.gms:play-services-fitness:21.2.0")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")

// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
```

### Data Flow Architecture
1. **Data Collection**: FireBoltt → DaFit → Google Fit
2. **Data Processing**: Google Fit API → DocAI App
3. **Data Storage**: Room Database for offline access
4. **Data Analysis**: AI engine for insights and recommendations
5. **User Interface**: Compose UI for real-time display

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android device with Google Play Services
- Google account with Google Fit enabled
- FireBoltt smartwatch connected to DaFit app

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/DocAI_gfit.git
   cd DocAI_gfit
   ```

2. **Google Cloud Console Setup**
   - Create a new project in [Google Cloud Console](https://console.cloud.google.com/)
   - Enable Google Fit API and Google Sign-In API
   - Create OAuth 2.0 credentials for Android
   - Download `google-services.json` and place it in `app/` directory

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Device Setup**
   - Install the app on your Android device
   - Grant required permissions (Activity Recognition, Body Sensors)
   - Sign in with your Google account
   - Connect to Google Fit

### Configuration

#### Google Fit API Setup
1. Add your package name: `com.example.docai`
2. Add SHA-1 fingerprint:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
3. Download and replace `google-services.json`

#### Permissions Required
```xml
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
<uses-permission android:name="android.permission.BODY_SENSORS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

---

## 📊 Data Models

### Health Data Structure
```kotlin
data class HealthData(
    val steps: Int,
    val heartRate: Float,
    val calories: Float,
    val distance: Float,
    val timestamp: Long,
    val lastUpdated: Long
)
```

### Health Insight Model
```kotlin
data class HealthInsight(
    val title: String,
    val value: String,
    val iconRes: Int,
    val subtitle: String = ""
)
```

---

## 🔧 API Integration

### Google Fit API Endpoints
- **Steps**: `DataType.TYPE_STEP_COUNT_DELTA`
- **Heart Rate**: `DataType.TYPE_HEART_RATE_BPM`
- **Calories**: `DataType.TYPE_CALORIES_EXPENDED`
- **Distance**: `DataType.TYPE_DISTANCE_DELTA`

### Authentication Flow
1. Google Sign-In with Fitness scopes
2. OAuth consent for health data access
3. Permission validation and error handling
4. Real-time data fetching with coroutines

---

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
- Health data fetching functions
- Permission handling
- Google Fit authentication
- Database operations

---

## 🐛 Troubleshooting

### Common Issues

1. **Google Fit Authentication Failed**
   - Ensure Google Play Services is installed
   - Check internet connection
   - Verify Google account is signed in

2. **No Data Available**
   - Ensure DaFit is synced with Google Fit
   - Check if FireBoltt is connected to DaFit
   - Wait for data sync (can take a few minutes)

3. **Permission Denied Errors**
   - Grant all required permissions in device settings
   - Reinstall app if permissions are stuck

4. **Build Errors**
   - Sync Gradle files
   - Check google-services.json configuration
   - Verify package name matches

### Debug Steps
1. Check Google Fit data in the Google Fit app
2. Verify permissions in device settings
3. Test connection using "Connect to Google Fit" button
4. Check logs for detailed error messages

---

## 📈 Future Enhancements

### Planned Features
- [ ] **AI-powered health predictions**
- [ ] **Telemedicine integration**
- [ ] **Wearable device compatibility expansion**
- [ ] **Advanced analytics dashboard**
- [ ] **Social health challenges**
- [ ] **Integration with more health platforms**

### Technical Improvements
- [ ] **Offline data synchronization**
- [ ] **Advanced caching mechanisms**
- [ ] **Performance optimizations**
- [ ] **Enhanced security measures**

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding standards
- Write unit tests for new features
- Update documentation for API changes
- Ensure all tests pass before submitting

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Abhishek** - *Initial work* - [GitHub Profile](https://github.com/yourusername)

### Acknowledgments
- Google Fit API team for excellent documentation
- Android community for best practices
- National Hackathon 2025 organizers
- All contributors and testers

---

## 📞 Support

For support, email your-email@example.com or join our [Discord community](https://discord.gg/your-discord).

---

## 🏆 Awards & Recognition

- **🥇 Winner** - National Hackathon 2025
- **🏅 Best AI Integration** - Healthcare Innovation Award
- **⭐ Featured** - Google Developer Community

---

<div align="center">

### 🌟 Star this repository if you found it helpful!

[![GitHub stars](https://img.shields.io/github/stars/yourusername/DocAI_gfit?style=social)](https://github.com/yourusername/DocAI_gfit/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/yourusername/DocAI_gfit?style=social)](https://github.com/yourusername/DocAI_gfit/network/members)
[![GitHub issues](https://img.shields.io/github/issues/yourusername/DocAI_gfit)](https://github.com/yourusername/DocAI_gfit/issues)

</div>
