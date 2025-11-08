# Online Shop - New Features

## Authentication System

### Login Feature
- **Location**: `LoginActivity`
- **Features**:
  - Email and password authentication
  - Form validation
  - Password visibility toggle
  - Navigation to registration
  - Forgot password placeholder
  - Session management with UserPreferences

### Registration Feature
- **Location**: `RegisterActivity`
- **Features**:
  - User registration with email and password
  - Full name collection
  - Password confirmation
  - Minimum password length validation (6 characters)
  - Automatic login after successful registration
  - Firebase Realtime Database integration for user data

### User Profile System
- **Location**: `ProfileActivity`
- **Features**:
  - Display user information (name, email)
  - Profile picture placeholder
  - Edit profile navigation
  - My Orders placeholder
  - Address management placeholder
  - Logout functionality with confirmation dialog
  - Session clearing on logout

### Edit Profile Feature
- **Location**: `EditProfileActivity`
- **Features**:
  - Update user name
  - Add/update phone number
  - Add/update address
  - Email display (read-only)
  - Profile picture change placeholder
  - Real-time data sync with Firebase
  - Form validation

## Technical Implementation

### Firebase Integration
- **Firebase Authentication**: Email/password authentication
- **Firebase Realtime Database**: User profile data storage

### Data Models
- **UserModel**: Stores user information (uid, email, name, phone, address, profileImageUrl)
- **AuthRepository**: Handles all authentication and user data operations

### Session Management
- **UserPreferences**: SharedPreferences-based session management
- Stores: userId, userEmail, userName, isLoggedIn status
- Automatic session check on app launch

### Navigation Flow
1. **Splash Screen** → Checks login status
   - If logged in → MainActivity
   - If not logged in → LoginActivity
2. **Bottom Navigation** → Profile icon navigates to ProfileActivity
3. **Profile** → Edit Profile → Update and return

## How to Use

### First Time Users
1. Launch the app
2. Click "Start" on splash screen
3. Click "Register" on login screen
4. Fill in name, email, password
5. Confirm password and register
6. Automatically logged in to main screen

### Existing Users
1. Launch the app
2. Click "Start" on splash screen
3. Enter email and password
4. Click "Login"
5. Access main screen

### Profile Management
1. Click profile icon in bottom navigation
2. View your profile information
3. Click "Edit Profile" to update details
4. Click "Logout" to sign out

## Future Enhancements
- Password reset functionality
- Profile picture upload
- Order history tracking
- Address book management
- Social media authentication
- Email verification
- Two-factor authentication

## Dependencies Added
```kotlin
implementation(libs.firebase.auth) // Firebase Authentication
```

## Files Created/Modified

### New Files
- `UserModel.java` - User data model
- `UserPreferences.java` - Session management
- `AuthRepository.java` - Authentication logic
- `LoginActivity.java` & `activity_login.xml`
- `RegisterActivity.java` & `activity_register.xml`
- `ProfileActivity.java` & `activity_profile.xml`
- `EditProfileActivity.java` & `activity_edit_profile.xml`
- `purple_bg.xml` - Purple background drawable

### Modified Files
- `build.gradle.kts` - Added Firebase Auth dependency
- `libs.versions.toml` - Added Firebase Auth version
- `AndroidManifest.xml` - Registered new activities
- `SplashActivity.java` - Added authentication check
- `MainActivity.java` - Added profile navigation
- `colors.xml` - Added lightGrey color

## Security Notes
- Passwords are handled by Firebase Authentication (hashed and secure)
- Session data stored in SharedPreferences (local device only)
- User data stored in Firebase Realtime Database with proper structure
- Email validation on registration
- Password minimum length enforcement
