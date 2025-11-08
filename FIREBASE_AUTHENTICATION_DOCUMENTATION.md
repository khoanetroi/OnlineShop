# Tài liệu Firebase Authentication - OnlineShop

## Tổng quan
Ứng dụng sử dụng Firebase Authentication và Firebase Realtime Database để quản lý xác thực người dùng và lưu trữ thông tin profile.

---

## Kiến trúc hệ thống

### 1. Các thành phần chính

#### **AuthRepository** (`com.example.onlineshop.Respository.AuthRepository`)
- Repository pattern xử lý tất cả các thao tác Firebase
- Quản lý Firebase Authentication và Firebase Realtime Database
- Trả về LiveData để observe kết quả bất đồng bộ

#### **UserPreferences** (`com.example.onlineshop.Helper.UserPreferences`)
- Lưu trữ session người dùng local bằng SharedPreferences
- Cache thông tin user để truy cập nhanh offline

#### **UserModel** (`com.example.onlineshop.Domain.UserModel`)
- Model đại diện cho dữ liệu người dùng
- Được serialize để lưu vào Firebase Realtime Database

---

## Luồng dữ liệu chi tiết

### 📝 **1. ĐĂNG KÝ (Register Flow)**

#### **Bước 1: User nhập thông tin**
```java
// RegisterActivity.java - lines 47-51
String name = binding.nameEdt.getText().toString().trim();
String email = binding.emailEdt.getText().toString().trim();
String password = binding.passwordEdt.getText().toString().trim();
String confirmPassword = binding.confirmPasswordEdt.getText().toString().trim();
```

#### **Bước 2: Validation**
```java
// RegisterActivity.java - lines 53-93
- Kiểm tra name không rỗng
- Kiểm tra email hợp lệ (sử dụng Patterns.EMAIL_ADDRESS)
- Kiểm tra password >= 6 ký tự
- Kiểm tra password và confirmPassword khớp nhau
```

#### **Bước 3: Gọi AuthRepository.register()**
```java
// RegisterActivity.java - line 98
authRepository.register(email, password, name).observe(this, result -> {
    // Xử lý kết quả
});
```

#### **Bước 4: Firebase Authentication tạo tài khoản**
```java
// AuthRepository.java - lines 61-84
firebaseAuth.createUserWithEmailAndPassword(email, password)
    .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            UserModel user = new UserModel(firebaseUser.getUid(), email, name);
            saveUserToDatabase(user, result);
        }
    });
```

**Dữ liệu được tạo:**
- Firebase Authentication tạo user với email/password
- Nhận được `uid` (User ID) duy nhất từ Firebase

#### **Bước 5: Lưu thông tin vào Firebase Realtime Database**
```java
// AuthRepository.java - lines 86-93
private void saveUserToDatabase(UserModel user, MutableLiveData<AuthResult> result) {
    DatabaseReference ref = firebaseDatabase.getReference("Users").child(user.getUid());
    ref.setValue(user)
        .addOnSuccessListener(aVoid -> 
            result.setValue(new AuthResult(true, "Registration successful", user)))
}
```

**Cấu trúc dữ liệu trong Firebase Database:**
```
Users/
  └── {uid}/
      ├── uid: "abc123..."
      ├── email: "user@example.com"
      ├── name: "Nguyen Van A"
      ├── phone: null
      ├── address: null
      └── profileImageUrl: null
```

#### **Bước 6: Lưu session local**
```java
// RegisterActivity.java - lines 103-107
userPreferences.saveUserSession(
    result.user.getUid(),
    result.user.getEmail(),
    result.user.getName()
);
```

**Dữ liệu lưu trong SharedPreferences:**
```
UserPrefs:
  - isLoggedIn: true
  - userId: "abc123..."
  - userEmail: "user@example.com"
  - userName: "Nguyen Van A"
```

#### **Bước 7: Chuyển đến MainActivity**
```java
// RegisterActivity.java - lines 109-110
startActivity(new Intent(RegisterActivity.this, MainActivity.class));
finish();
```

---

### 🔐 **2. ĐĂNG NHẬP (Login Flow)**

#### **Bước 1: User nhập thông tin**
```java
// LoginActivity.java - lines 52-53
String email = binding.emailEdt.getText().toString().trim();
String password = binding.passwordEdt.getText().toString().trim();
```

#### **Bước 2: Validation**
```java
// LoginActivity.java - lines 55-71
- Kiểm tra email không rỗng
- Kiểm tra email hợp lệ
- Kiểm tra password không rỗng
```

#### **Bước 3: Gọi AuthRepository.login()**
```java
// LoginActivity.java - line 76
authRepository.login(email, password).observe(this, result -> {
    // Xử lý kết quả
});
```

#### **Bước 4: Firebase Authentication xác thực**
```java
// AuthRepository.java - lines 34-56
firebaseAuth.signInWithEmailAndPassword(email, password)
    .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            loadUserData(user.getUid(), result);
        }
    });
```

**Dữ liệu nhận được:**
- Firebase trả về `FirebaseUser` object
- Lấy được `uid` của user

#### **Bước 5: Load thông tin user từ Firebase Database**
```java
// AuthRepository.java - lines 95-113
private void loadUserData(String uid, MutableLiveData<AuthResult> result) {
    DatabaseReference ref = firebaseDatabase.getReference("Users").child(uid);
    ref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            UserModel user = snapshot.getValue(UserModel.class);
            result.setValue(new AuthResult(true, "Login successful", user));
        }
    });
}
```

**Dữ liệu được load:**
- Đọc toàn bộ thông tin user từ node `Users/{uid}`
- Convert DataSnapshot thành UserModel object
- Bao gồm: uid, email, name, phone, address, profileImageUrl

#### **Bước 6: Lưu session local**
```java
// LoginActivity.java - lines 81-85
userPreferences.saveUserSession(
    result.user.getUid(),
    result.user.getEmail(),
    result.user.getName()
);
```

#### **Bước 7: Chuyển đến MainActivity**
```java
// LoginActivity.java - lines 87-88
startActivity(new Intent(LoginActivity.this, MainActivity.class));
finish();
```

---

### 👤 **3. HIỂN THỊ PROFILE (Profile Display Flow)**

#### **Bước 1: Load dữ liệu từ UserPreferences**
```java
// ProfileActivity.java - lines 40-51
String userId = userPreferences.getUserId();
String userName = userPreferences.getUserName();
String userEmail = userPreferences.getUserEmail();

binding.nameTxt.setText(userName);
binding.emailTxt.setText(userEmail);
```

**Nguồn dữ liệu:**
- Đọc từ SharedPreferences (cache local)
- Hiển thị ngay lập tức (không cần network)

#### **Bước 2: Sync với Firebase Database**
```java
// ProfileActivity.java - lines 53-60
authRepository.getUserProfile(userId).observe(this, user -> {
    if (user != null) {
        binding.nameTxt.setText(user.getName());
        binding.emailTxt.setText(user.getEmail());
    }
});
```

```java
// AuthRepository.java - lines 115-133
public LiveData<UserModel> getUserProfile(String uid) {
    MutableLiveData<UserModel> userData = new MutableLiveData<>();
    DatabaseReference ref = firebaseDatabase.getReference("Users").child(uid);
    
    ref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            UserModel user = snapshot.getValue(UserModel.class);
            userData.setValue(user);
        }
    });
    
    return userData;
}
```

**Nguồn dữ liệu:**
- Đọc từ Firebase Realtime Database
- Đảm bảo dữ liệu luôn mới nhất
- Cập nhật UI khi có thay đổi

---

### 🚪 **4. ĐĂNG XUẤT (Logout Flow)**

#### **Bước 1: Hiển thị dialog xác nhận**
```java
// ProfileActivity.java - lines 87-102
new AlertDialog.Builder(this)
    .setTitle("Logout")
    .setMessage("Are you sure you want to logout?")
    .setPositiveButton("Yes", (dialog, which) -> {
        // Thực hiện logout
    })
    .show();
```

#### **Bước 2: Logout Firebase Authentication**
```java
// ProfileActivity.java - line 92
authRepository.logout();
```

```java
// AuthRepository.java - lines 146-148
public void logout() {
    firebaseAuth.signOut();
}
```

**Tác động:**
- Xóa Firebase Authentication session
- User không còn được xác thực

#### **Bước 3: Xóa session local**
```java
// ProfileActivity.java - line 93
userPreferences.clearSession();
```

```java
// UserPreferences.java - lines 45-48
public void clearSession() {
    editor.clear();
    editor.apply();
}
```

**Dữ liệu bị xóa:**
- isLoggedIn → false
- userId → null
- userEmail → null
- userName → null

#### **Bước 4: Chuyển về LoginActivity**
```java
// ProfileActivity.java - lines 95-98
Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(intent);
finish();
```

---

## Sơ đồ luồng dữ liệu

### Register Flow
```
User Input (RegisterActivity)
    ↓
Validation
    ↓
AuthRepository.register()
    ↓
Firebase Authentication (createUserWithEmailAndPassword)
    ↓ [uid generated]
Firebase Realtime Database (Users/{uid})
    ↓ [UserModel saved]
UserPreferences (SharedPreferences)
    ↓ [Session cached]
MainActivity
```

### Login Flow
```
User Input (LoginActivity)
    ↓
Validation
    ↓
AuthRepository.login()
    ↓
Firebase Authentication (signInWithEmailAndPassword)
    ↓ [uid retrieved]
Firebase Realtime Database (load Users/{uid})
    ↓ [UserModel loaded]
UserPreferences (SharedPreferences)
    ↓ [Session cached]
MainActivity
```

### Profile Display Flow
```
ProfileActivity.onCreate()
    ↓
UserPreferences (read cache) → Display immediately
    ↓
AuthRepository.getUserProfile()
    ↓
Firebase Realtime Database (Users/{uid})
    ↓ [UserModel loaded]
Update UI with fresh data
```

---

## Cấu trúc dữ liệu

### UserModel
```java
public class UserModel {
    private String uid;              // Firebase Auth UID
    private String email;            // Email đăng ký
    private String name;             // Tên người dùng
    private String phone;            // Số điện thoại (optional)
    private String address;          // Địa chỉ (optional)
    private String profileImageUrl;  // URL ảnh đại diện (optional)
}
```

### AuthResult
```java
public static class AuthResult {
    public boolean success;    // Thành công hay thất bại
    public String message;     // Thông báo lỗi/thành công
    public UserModel user;     // Dữ liệu user (nếu thành công)
}
```

### SharedPreferences Keys
```
PREF_NAME = "UserPrefs"
- KEY_IS_LOGGED_IN = "isLoggedIn"
- KEY_USER_ID = "userId"
- KEY_USER_EMAIL = "userEmail"
- KEY_USER_NAME = "userName"
```

---

## Firebase Database Structure

```
firebase-database/
└── Users/
    ├── {uid_1}/
    │   ├── uid: "uid_1"
    │   ├── email: "user1@example.com"
    │   ├── name: "User One"
    │   ├── phone: "0123456789"
    │   ├── address: "123 Street"
    │   └── profileImageUrl: "https://..."
    │
    └── {uid_2}/
        ├── uid: "uid_2"
        ├── email: "user2@example.com"
        └── name: "User Two"
```

---

## Xử lý lỗi

### Lỗi đăng ký
```java
// AuthRepository.java - lines 72-77
if (task.getException() != null) {
    errorMessage = task.getException().getMessage();
}
// Các lỗi phổ biến:
// - Email already exists
// - Weak password
// - Network error
```

### Lỗi đăng nhập
```java
// AuthRepository.java - lines 44-48
if (task.getException() != null) {
    errorMessage = task.getException().getMessage();
}
// Các lỗi phổ biến:
// - Invalid email or password
// - User not found
// - Network error
```

### Lỗi load profile
```java
// AuthRepository.java - lines 127-129
@Override
public void onCancelled(@NonNull DatabaseError error) {
    userData.setValue(null);
}
// Xử lý: Hiển thị dữ liệu cache từ UserPreferences
```

---

## Tính năng Offline

### Firebase Persistence
```java
// AuthRepository.java - lines 24-28
try {
    this.firebaseDatabase.setPersistenceEnabled(true);
} catch (Exception e) {
    // Already enabled
}
```

**Lợi ích:**
- Dữ liệu được cache local
- Hoạt động offline
- Tự động sync khi có network

### SharedPreferences Cache
```java
// UserPreferences.java
- Lưu thông tin user cơ bản
- Truy cập nhanh không cần network
- Persist qua các lần mở app
```

---

## LiveData Pattern

### Tại sao sử dụng LiveData?
```java
// AuthRepository.java - line 31
public LiveData<AuthResult> login(String email, String password)
```

**Ưu điểm:**
1. **Lifecycle-aware**: Tự động unsubscribe khi Activity destroyed
2. **Asynchronous**: Không block UI thread
3. **Observable**: UI tự động update khi có data mới
4. **Thread-safe**: An toàn khi switch threads

### Cách sử dụng
```java
// LoginActivity.java - line 76
authRepository.login(email, password).observe(this, result -> {
    // Callback này chạy trên main thread
    if (result.success) {
        // Xử lý thành công
    } else {
        // Hiển thị lỗi
    }
});
```

---

## Best Practices được áp dụng

### 1. Repository Pattern
- Tách biệt logic Firebase khỏi UI
- Dễ test và maintain
- Single source of truth

### 2. Data Validation
- Validate trước khi gửi Firebase
- Giảm network calls không cần thiết
- Cải thiện UX

### 3. Error Handling
- Xử lý tất cả exception cases
- Hiển thị message rõ ràng cho user
- Fallback strategies

### 4. Caching Strategy
- SharedPreferences cho quick access
- Firebase persistence cho offline support
- Dual-layer caching

### 5. Security
- Password minimum 6 characters
- Email validation
- Firebase Security Rules (cần config)

---

## Cấu hình Firebase cần thiết

### 1. Firebase Console Setup
```
1. Tạo project trên Firebase Console
2. Enable Authentication → Email/Password
3. Enable Realtime Database
4. Download google-services.json
5. Đặt vào app/ folder
```

### 2. Build.gradle dependencies
```gradle
// Project level
classpath 'com.google.gms:google-services:4.x.x'

// App level
implementation 'com.google.firebase:firebase-auth:x.x.x'
implementation 'com.google.firebase:firebase-database:x.x.x'
apply plugin: 'com.google.gms.google-services'
```

### 3. Firebase Database Rules (khuyến nghị)
```json
{
  "rules": {
    "Users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

---

## Testing Checklist

### Register
- ✅ Validate empty fields
- ✅ Validate email format
- ✅ Validate password length
- ✅ Validate password match
- ✅ Handle duplicate email
- ✅ Save to Firebase Auth
- ✅ Save to Firebase Database
- ✅ Save to SharedPreferences
- ✅ Navigate to MainActivity

### Login
- ✅ Validate empty fields
- ✅ Validate email format
- ✅ Handle wrong credentials
- ✅ Load from Firebase Database
- ✅ Save to SharedPreferences
- ✅ Navigate to MainActivity

### Profile
- ✅ Load from cache first
- ✅ Sync with Firebase
- ✅ Update UI on data change
- ✅ Handle logout
- ✅ Clear all sessions

---

## Tóm tắt

### Dữ liệu được lưu ở đâu?

1. **Firebase Authentication**
   - Email và password (encrypted)
   - UID (unique identifier)

2. **Firebase Realtime Database**
   - UserModel complete (uid, email, name, phone, address, profileImageUrl)
   - Path: `Users/{uid}`

3. **SharedPreferences (Local)**
   - isLoggedIn, userId, userEmail, userName
   - Cache cho quick access

### Luồng dữ liệu chính

**Register**: Input → Validation → Firebase Auth → Firebase DB → SharedPreferences → MainActivity

**Login**: Input → Validation → Firebase Auth → Load from Firebase DB → SharedPreferences → MainActivity

**Profile**: Load from SharedPreferences (instant) → Sync with Firebase DB (fresh data) → Update UI

**Logout**: Firebase Auth signOut → Clear SharedPreferences → LoginActivity

---

## Liên hệ và hỗ trợ

Nếu có thắc mắc về implementation, vui lòng tham khảo:
- Firebase Documentation: https://firebase.google.com/docs
- Android LiveData: https://developer.android.com/topic/libraries/architecture/livedata
- Repository Pattern: https://developer.android.com/codelabs/android-room-with-a-view

---

**Document Version**: 1.0  
**Last Updated**: November 8, 2024  
**Author**: OnlineShop Development Team
