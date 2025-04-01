# RemoteX - Remote Computer Control Application

## 1. Introduction
### 1.1 Project Description
The goal of this project is to create a mobile application for remotely controlling a computer using an Android device. The application allows users to navigate the mouse cursor, perform clicks, and control multimedia playback.

### 1.2 Application Purpose
RemoteX aims to facilitate remote computer control. It is useful in situations where physical access to the mouse or keyboard is inconvenient or impossible.

### 1.3 Functional Requirements
- Connect to the computer via a local network
- Control the mouse cursor (movement, clicks, right-clicks)
- Support basic multimedia commands (play/pause, track change, volume control)

### 1.4 Non-functional Requirements
- The application should respond to user actions in real time
- Intuitive user interface
- Compatibility with Android 8.0+

---

## 2. External Specification
### 2.1 Server Installation and Launch
1. **Python 3** must be installed on the computer.
2. Install the required libraries:
   ```sh
   pip install pyautogui tkinter
   ```
3. Download the **remoteXserver.py** file from the repository and place it on the computer.
4. Start the server script:
   ```sh
   python remoteXserver.py
   ```
5. In the server window, click the **"Start Server"** button.
6. Once the server is running, its IP address and connection status will be displayed.

### 2.2 Application Installation
1. Download the **RemoteX APK** file and install it on an Android device.
2. After launching the application, enter the server's IP address displayed in the server window.
3. Click the **"Connect"** button.

### 2.3 Mouse Cursor Control
- The touchpad area is indicated by a gray rectangle.
- Swiping on the screen moves the cursor on the computer.
- Tapping with one finger performs a left mouse click.
- Long pressing triggers a right mouse click.

### 2.4 Multimedia Control
- The multimedia control buttons are located at the top of the screen.
- **Play** - pauses or resumes playback.
- **Next/Previous** - changes the track.
- **Volume Up/Down** - adjusts the volume.

---

## 3. Internal Specification
### 3.1 Application Structure
The application consists of the following components:
- **MainActivity.kt** – Main activity handling screen transitions.
- **RemoteXClient.kt** – Class responsible for network communication.
- **StartScreen.kt** – Initial screen for entering the server IP address.
- **TouchPadScreen.kt** – Screen for cursor and multimedia control.
- **MediaControls.kt** – UI component handling multimedia commands.
- **remoteXServer.py** – UDP server handling computer control commands.

### 3.2 Technologies Used
- **Android Jetpack Compose** – Modern UI framework.
- **Kotlin** – Programming language for the mobile application.
- **Python + pyautogui + tkinter** – Server script handling computer control.

---

## 4. Conclusions
### 4.1 Issues Encountered During Implementation
- **Cursor movement management** – Responsiveness issues were partially resolved by implementing movement buffering and sending data at regular intervals.

### 4.2 Possible Extensions
- Adding an on-screen keyboard.
- Bluetooth integration for offline operation.
- Multi-touch gesture support.
- Further optimization of cursor movement responsiveness.

---

## Authors
- **taachii** – Project creator

## License
This project is released under the **MIT License**. You are free to modify and distribute the code under the terms of the license.
