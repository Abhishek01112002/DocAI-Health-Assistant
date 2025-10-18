# Google Fit Authentication Troubleshooting Guide

## 🔧 **Your SHA-1 Fingerprint**
```
SHA1: C9:71:5E:B0:39:DF:A2:F8:70:E8:FB:FC:5D:59:D4:1A:08:6E:4D:B3
```

## 📋 **Step-by-Step Fix**

### **1. Google Cloud Console Setup**

1. **Go to [Google Cloud Console](https://console.cloud.google.com/)**
2. **Create New Project:**
   - Click "Select a project" → "New Project"
   - Name: "DoCAI Health App"
   - Click "Create"

3. **Enable Required APIs:**
   - Go to "APIs & Services" → "Library"
   - Search and enable:
     - ✅ **Google Fit API**
     - ✅ **Google Sign-In API**

### **2. Create OAuth 2.0 Credentials**

1. **Go to "APIs & Services" → "Credentials"**
2. **Click "Create Credentials" → "OAuth 2.0 Client IDs"**
3. **Fill in the details:**
   - Application type: **Android**
   - Name: **DoCAI Android Client**
   - Package name: `com.docai.healthapp`
   - SHA-1 certificate fingerprint: `C9:71:5E:B0:39:DF:A2:F8:70:E8:FB:FC:5D:59:D4:1A:08:6E:4D:B3`

4. **Click "Create"**
5. **Download the `google-services.json` file**

### **3. Update Your Project**

1. **Replace the template file:**
   - Delete: `app/google-services.json`
   - Add: Your downloaded `google-services.json`

2. **Sync Gradle:**
   - In Android Studio: File → Sync Project with Gradle Files

### **4. Test Authentication**

1. **Build and install the app**
2. **Try connecting to Google Fit**
3. **Check the logs** for detailed error messages

## 🚨 **Common Issues & Solutions**

### **Issue 1: "Authentication failed"**
**Cause:** Missing or incorrect `google-services.json`
**Solution:** 
- Ensure you've downloaded the correct `google-services.json`
- Verify package name matches exactly: `com.docai.healthapp`
- Check SHA-1 fingerprint is correct

### **Issue 2: "This app is not verified"**
**Cause:** App not verified by Google
**Solution:**
- Click "Advanced" → "Go to DoCAI (unsafe)"
- Or add your email to test users in Google Cloud Console

### **Issue 3: "Network error"**
**Cause:** Internet connection or API not enabled
**Solution:**
- Check internet connection
- Verify Google Fit API is enabled
- Check device has Google Play Services

### **Issue 4: "Permission denied"**
**Cause:** Google Fit permissions not granted
**Solution:**
- Grant all requested permissions
- Check Google Fit app is installed
- Ensure Google account has Google Fit enabled

## 🔍 **Debug Steps**

### **Check Logs:**
1. **Open Android Studio**
2. **Go to View → Tool Windows → Logcat**
3. **Filter by "GoogleFitAuth"**
4. **Look for error messages**

### **Verify Setup:**
1. **Check `google-services.json` contains real values (not "YOUR_*")**
2. **Verify package name in Google Cloud Console matches app**
3. **Ensure SHA-1 fingerprint is exactly: `C9:71:5E:B0:39:DF:A2:F8:70:E8:FB:FC:5D:59:D4:1A:08:6E:4D:B3`**

## 📱 **Testing Checklist**

- [ ] Google Cloud Console project created
- [ ] Google Fit API enabled
- [ ] Google Sign-In API enabled
- [ ] OAuth 2.0 client created with correct SHA-1
- [ ] `google-services.json` downloaded and placed in `app/` folder
- [ ] Gradle synced successfully
- [ ] App builds without errors
- [ ] Device has Google Play Services
- [ ] Google account has Google Fit enabled
- [ ] All permissions granted

## 🆘 **Still Having Issues?**

If authentication still fails after following these steps:

1. **Check the exact error message** in the Toast
2. **Look at Logcat** for detailed error logs
3. **Verify your Google account** has Google Fit enabled
4. **Try with a different Google account**
5. **Ensure your device** has Google Play Services installed

## 📞 **Quick Test**

To test if Google Fit is working:
1. **Open Google Fit app** on your device
2. **Check if it shows your health data**
3. **If Google Fit app works, the API should work too**

---

**Remember:** The most common issue is using the template `google-services.json` file. Make sure you've downloaded the real one from Google Cloud Console!
