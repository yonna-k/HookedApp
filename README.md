# Hooked! (Crochet Social Media App)
This project is a social media crochet app for Android, designed for crocheters to share and upload their creations and patterns. Users can create accounts, log in using Firebase Authentication, and interact with the app by uploading images, and patterns. All posts are stored securely using Firebase Realtime Database and Firebase Storage.

## Prerequisites
Before running the APK, ensure that you have the following installed on your system:

- Android Studio
- Java Development Kit (JDK): Android Studio comes with a JDK, but ensure it's correctly configured in `File > Project Structure > SDK Location`.
- APK file: Located in 'Releases'
- Android Emulator: Part of Android Studio's setup.

## Download
The latest APK release can be downloaded from the [Releases Section]() of this repository.

## Steps to Run APK on Android Emulator
### 1. Open Android Studio
   - Launch Android Studio. You don't need to open a specific project, just Android Studio's main interface.
### 2. Create an Emulator
   - Go to `Tools > Device Manager`
   - Choose `Create Virtual Device`
   - Click `New Hardware Profile` and edit the following properties:
     - Name: S22
     - Screen Size: 6.1 inch
     - Resolution: 1080 x 2340 px
     - RAM : 2048 MB
    
   - Choose "Upside Down Cake" as the system image
   - Click 'Next' and 'Finish' to create the emulator

   To start the emulator, click the play button next to it

### 3. Install APK on Emulator (please download the APK from 'Releases' first)
   
   Now that the emulator is running, you can install the APK on it.
   - Drag and drop the APK onto the running emulator's screen
   - The app should be installed and you can click to run it

   Alternatively, you can install the APK from the terminal:
   - Open the terminal: `View > Tool Windows > Terminal`
   - Run the following command:
     ```bash
     adb install path/to/your/apk/file.apk
   - Replace `path/to/your/apk/file.apk` with the actual path to the APK file on your computer.

   - Click to run the app

## Explanation of Features
