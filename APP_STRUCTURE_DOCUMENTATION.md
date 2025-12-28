# OnlineShop App - Complete Code Structure Documentation

## 📱 App Overview
An Android e-commerce shopping application built with Java, Firebase Realtime Database, and Material Design. The app allows users to browse products, add items to cart, manage favorites, and complete purchases with an integrated payment system.

**Technology Stack:**
- Language: Java
- Database: Firebase Realtime Database
- Authentication: Firebase Auth
- UI: Android Material Design, View Binding
- Architecture: MVVM with Repositories

---

## 🏗️ Project Structure

```
app/src/main/java/com/example/onlineshop/
├── Activity/              # Full screen activities
├── Fragment/              # Reusable screen fragments
├── Adapter/               # RecyclerView adapters for displaying data
├── Model/                 # Data models/POJO classes
├── Respository/           # Data access layer (Firebase operations)
├── ViewModel/             # Business logic & data management
└── Helper/                # Utility classes
```

---

## 🔐 Authentication Flow

### Entry Point: SplashActivity
**File:** `app/src/main/java/com/example/onlineshop/Activity/SplashActivity.java`

The app starts here. It checks if user is already logged in:
- If logged in → Go to `MainContainerActivity`
- If not logged in → Show "Start" and "Login" buttons

**Key Logic:**
- Uses `UserPreferences` to check login status
- Handles initial app navigation

---

## 📋 Activities (Full Screen Screens)

### 1. **SplashActivity**
**File:** `SplashActivity.java`
- **Purpose:** Welcome/splash screen
- **What it shows:** App logo with "Start" and "Login" buttons
- **Navigation:** Routes to RegisterActivity or LoginActivity

### 2. **LoginActivity**
**File:** `LoginActivity.java`
- **Purpose:** User login screen
- **What it shows:** Email and password input fields
- **Features:**
  - Login with email/password
  - "Forgot Password" link
  - "Register" link for new users
- **Database:** Uses `AuthRepository` for Firebase authentication
- **Data Storage:** Saves user info in `UserPreferences`

### 3. **RegisterActivity**
**File:** `RegisterActivity.java`
- **Purpose:** New user registration
- **What it shows:** Email, password, and confirm password fields
- **Features:**
  - Register new account
  - Validation of inputs
  - Automatic login after registration
- **Database:** Uses `AuthRepository` for Firebase auth

### 4. **ForgotPasswordActivity**
**File:** `ForgotPasswordActivity.java`
- **Purpose:** Password recovery
- **Features:** Email-based password reset

### 5. **MainContainerActivity**
**File:** `MainContainerActivity.java`
- **Purpose:** Main app container with bottom navigation
- **What it shows:** Bottom navigation bar + Fragment container
- **Fragments it holds:**
  - `HomeFragment` (Home)
  - `FavoritesFragment` (Wishlist)
  - `MyOrderFragment` (Orders)
  - `SettingsFragment` (Profile)
- **Navigation Methods:**
  - `navigateToMyCart()` - Go to cart
  - `navigateToEditProfile()` - Edit profile
  - `navigateToChangePassword()` - Change password

### 6. **DetailActivity**
**File:** `DetailActivity.java`
- **Purpose:** Product detail page
- **What it shows:**
  - Product image carousel (PicListAdapter)
  - Product title and price
  - Rating and reviews count
  - Color selection (ColorAdapter)
  - Size selection (SizeAdapter)
  - Quantity selector (+/- buttons)
  - "Add to Cart" button
- **Receives:** ItemsModel object via Intent
- **Database:** Adds items to local cart database

### 7. **PaymentActivity** ⭐ (FIXED)
**File:** `PaymentActivity.java`
- **Purpose:** Checkout and payment processing
- **What it shows:**
  - Order summary (subtotal, tax, shipping)
  - Selected items list
  - Payment method selection
  - "Thanh Toán" (Checkout) button
- **Receives:** Selected items list from MyCartFragment
- **Features:**
  - Payment method selection (Visa, MasterCard, PayPal)
  - Order creation in Firebase
  - Cart cleanup after successful order
- **Fixed Issues:**
  - Firebase type conversion errors (Long vs String)
  - App exit on checkout
  - Error handling with detailed logging

### 8. **OrderDetailActivity**
**File:** `OrderDetailActivity.java`
- **Purpose:** View single order details
- **What it shows:**
  - Order ID and status
  - Order items list (OrderDetailProductAdapter)
  - Order total and date
  - Order tracking info
- **Receives:** OrderModel object via Intent

### 9. **ProductListActivity**
**File:** `ProductListActivity.java`
- **Purpose:** Display product lists by category/type
- **What it shows:** Grid of products (2 columns)
- **Receives via Intent:**
  - `listType`: "popular", "new_arrivals", or "recommended"
  - `title`: Display title
- **Uses:** MainViewModel for data loading

### 10. **SearchActivity**
**File:** `SearchActivity.java`
- **Purpose:** Product search functionality
- **What it shows:**
  - Search input field
  - Filtered product results
- **Features:**
  - Real-time search filtering
  - Loads all products from Firebase
  - Displays matches in grid

### 11. **NotificationActivity**
**File:** `NotificationActivity.java`
- **Purpose:** Display notifications
- **What it shows:** List of notifications (empty in current version)
- **Uses:** NotificationAdapter for display

---

## 📲 Fragments (Reusable Screens)

Fragments are parts of the `MainContainerActivity` and are shown in the bottom navigation.

### 1. **HomeFragment**
**File:** `Fragment/HomeFragment.java`
- **Purpose:** Main shopping home screen
- **What it shows:**
  - User greeting ("Xin chào [Name]")
  - Category list (horizontal scroll)
  - Banner slider (automatic rotation)
  - Popular products
  - New arrivals products
  - Recommended products
- **Database:** Uses `MainViewModel` to load data from Firebase
- **Adapters Used:**
  - `SliderAdapter` - Banner carousel
  - `CategoryAdapter` - Categories
  - `PopularAdapter` - Products

### 2. **MyCartFragment**
**File:** `Fragment/MyCartFragment.java`
- **Purpose:** Shopping cart display
- **What it shows:**
  - List of items in cart
  - Item selection checkboxes
  - Subtotal, tax, shipping, total
  - "Thanh Toán" (Checkout) button
- **Database:** Firebase `Users/{userId}/cart/`
- **Features:**
  - Select/deselect items for checkout
  - Calculate totals based on selected items
  - Navigate to PaymentActivity
- **Adapter:** CartAdapter

### 3. **FavoritesFragment**
**File:** `Fragment/FavoritesFragment.java`
- **Purpose:** Wishlist/favorites
- **What it shows:**
  - All favorited products
  - Search/filter favorites
  - Sort options (Latest, Popular, Cheapest)
- **Database:** Firebase `Users/{userId}/wishlist/`
- **Features:**
  - Filter by search text
  - Sort by different criteria
  - Remove from favorites
- **Adapter:** FavoriteAdapter

### 4. **MyOrderFragment**
**File:** `Fragment/MyOrderFragment.java`
- **Purpose:** View all user orders
- **What it shows:**
  - Two tabs: "My Orders" and "History"
  - List of orders with status
  - Order details, tracking, and completion options
- **Database:** Firebase `Orders/{userId}/`
- **Features:**
  - Tab switching with animation
  - View order details
  - Track orders
  - Mark orders as received
- **Adapter:** OrderAdapter

### 5. **SettingsFragment**
**File:** `Fragment/SettingsFragment.java`
- **Purpose:** User profile and settings
- **What it shows:**
  - User profile information
  - Edit profile button
  - Change password button
  - Logout button
  - About & Help sections
- **Features:**
  - Navigate to EditProfileFragment
  - Navigate to ChangePasswordFragment
  - Logout user

### 6. **EditProfileFragment**
**File:** `Fragment/EditProfileFragment.java`
- **Purpose:** Edit user profile information
- **What it shows:**
  - Name field
  - Email field (read-only)
  - Phone number field
  - Save button
- **Database:** Firebase `Users/{userId}/profile/`

### 7. **ChangePasswordFragment**
**File:** `Fragment/ChangePasswordFragment.java`
- **Purpose:** Change user password
- **What it shows:**
  - Current password field
  - New password field
  - Confirm password field
  - Visibility toggle buttons
  - Change button
- **Features:**
  - Password validation
  - Re-authenticate user before changing

---

## 🎨 Adapters (RecyclerView Data Adapters)

### 1. **PopularAdapter**
**File:** `Adapter/PopularAdapter.java`
- **Purpose:** Display products in horizontal/grid layout
- **Used in:** HomeFragment, ProductListActivity
- **Shows:** Product image, title, price, rating
- **Features:** Click to open DetailActivity

### 2. **CartAdapter**
**File:** `Adapter/CartAdapter.java`
- **Purpose:** Display items in shopping cart
- **Used in:** MyCartFragment
- **Shows:** Product image, title, price, quantity, color, size
- **Features:**
  - Checkbox selection
  - Quantity adjustment
  - Remove from cart
  - Quantity update to Firebase

### 3. **FavoriteAdapter**
**File:** `Adapter/FavoriteAdapter.java`
- **Purpose:** Display favorited products
- **Used in:** FavoritesFragment
- **Shows:** Product details with remove button
- **Features:** Click to DetailActivity

### 4. **OrderAdapter**
**File:** `Adapter/OrderAdapter.java`
- **Purpose:** Display list of orders
- **Used in:** MyOrderFragment
- **Shows:** Order ID, status, date, total
- **Features:** Click actions (Detail, Track, Receive)

### 5. **OrderDetailProductAdapter**
**File:** `Adapter/OrderDetailProductAdapter.java`
- **Purpose:** Display products within an order
- **Used in:** OrderDetailActivity
- **Shows:** Product details for each item in order

### 6. **CategoryAdapter**
**File:** `Adapter/CategoryAdapter.java`
- **Purpose:** Display product categories
- **Used in:** HomeFragment
- **Shows:** Category icon and name
- **Features:** Click to filter products by category

### 7. **SliderAdapter**
**File:** `Adapter/SliderAdapter.java`
- **Purpose:** Automatic banner carousel
- **Used in:** HomeFragment
- **Shows:** Banner images that rotate automatically
- **Features:**
  - Auto-scroll every 3 seconds
  - Manual scroll support
  - Smooth transitions

### 8. **ColorAdapter**
**File:** `Adapter/ColorAdapter.java`
- **Purpose:** Display color options for a product
- **Used in:** DetailActivity
- **Shows:** Available colors with selection highlight
- **Features:** Single selection mode

### 9. **SizeAdapter**
**File:** `Adapter/SizeAdapter.java`
- **Purpose:** Display size options for a product
- **Used in:** DetailActivity
- **Shows:** Available sizes with selection highlight
- **Features:** Single selection mode

### 10. **PaymentMethodAdapter**
**File:** `Adapter/PaymentMethodAdapter.java`
- **Purpose:** Display payment methods
- **Used in:** PaymentActivity
- **Shows:** Payment options (Visa, MasterCard, PayPal)
- **Features:** Single selection

### 11. **PaymentProductAdapter**
**File:** `Adapter/PaymentProductAdapter.java`
- **Purpose:** Display products in payment summary
- **Used in:** PaymentActivity
- **Shows:** Selected items for checkout

### 12. **PicListAdapter**
**File:** `Adapter/PicListAdapter.java`
- **Purpose:** Display product image gallery
- **Used in:** DetailActivity
- **Shows:** Product images with thumbnail selection

### 13. **NotificationAdapter**
**File:** `Adapter/NotificationAdapter.java`
- **Purpose:** Display notifications
- **Used in:** NotificationActivity

---

## 📦 Data Models (POJO Classes)

### 1. **ItemsModel**
**File:** `Model/ItemsModel.java`
- **Purpose:** Product information
- **Fields:**
  - title: Product name
  - description: Product description
  - price: Current price
  - oldPrice: Original price (for discounts)
  - rating: Product rating (0-5)
  - review: Number of reviews
  - picUrl: ArrayList of product images
  - color: ArrayList of available colors
  - size: ArrayList of available sizes
  - NumberinCart: Quantity in cart
  - offPercent: Discount percentage

### 2. **OrderModel**
**File:** `Model/OrderModel.java`
- **Purpose:** Order information
- **Fields:**
  - orderId: Unique order ID
  - userId: User who made order
  - subtotal: Items total
  - tax: Tax amount
  - delivery: Shipping fee
  - total: Final total
  - createdAt: Order creation timestamp
  - orderDate: Order date
  - status: Order status (Đang Xử Lý, Đang Giao, Đã Giao, etc.)
  - items: ArrayList of ItemsModel

### 3. **UserModel**
**File:** `Model/UserModel.java`
- **Purpose:** User account information
- **Fields:**
  - id: User ID
  - name: Full name
  - email: Email address
  - phone: Phone number
  - address: Delivery address
  - createdAt: Account creation date

### 4. **CategoryModel**
**File:** `Model/CategoryModel.java`
- **Purpose:** Product category
- **Fields:**
  - id: Category ID
  - name: Category name
  - icon: Category icon URL

### 5. **BannerModel**
**File:** `Model/BannerModel.java`
- **Purpose:** Promotional banner
- **Fields:**
  - url: Banner image URL

### 6. **AppSettingsModel** ⭐ (FIXED)
**File:** `Model/AppSettingsModel.java`
- **Purpose:** Global app configuration
- **Fields:**
  - currency: Currency code
  - currencySymbol: Currency symbol ($, đ, etc.)
  - taxRate: Tax percentage
  - shippingFee: Base shipping cost
  - freeShippingThreshold: Min amount for free shipping
  - maxCartItems: Max items in cart
  - maintenanceMode: Is app in maintenance?
  - returnPolicyDays: Return period days
  - supportEmail: Support email
  - supportPhone: Support phone
- **Fixed Issues:**
  - Added proper type conversion from Firebase (Long to Double/Int)
  - Default value fallback on parse error

### 7. **NotificationModel**
**File:** `Model/NotificationModel.java`
- **Purpose:** User notification
- **Fields:** Title, message, timestamp, type

---

## 🗄️ Repositories (Data Access Layer)

### 1. **AuthRepository**
**File:** `Respository/AuthRepository.java`
- **Purpose:** Handle user authentication
- **Methods:**
  - `registerUser(email, password)` - Create new account
  - `loginUser(email, password)` - Login user
  - `changePassword(oldPass, newPass)` - Update password
  - `resetPassword(email)` - Password recovery
- **Database:** Firebase Authentication

### 2. **MainRepository**
**File:** `Respository/MainRepository.java`
- **Purpose:** Load main app data
- **Methods:**
  - `loadCategory()` - Get product categories
  - `loadBanner()` - Get promotional banners
  - `loadPopular()` - Get popular products
  - `loadNewArrivals()` - Get new products
  - `loadRecommended()` - Get recommended products
  - `loadAppSettings()` - Get app configuration ⭐ (FIXED)
- **Database:** Firebase `Category/`, `Banner/`, `Items/`, `AppSettings/`
- **Return Type:** LiveData for MVVM pattern

### 3. **CartRepository**
**File:** `Respository/CartRepository.java`
- **Purpose:** Manage shopping cart
- **Methods:**
  - `addToCart(item)` - Add item to cart
  - `removeFromCart(itemId)` - Remove item
  - `getCart()` - Get all cart items
  - `clearCart()` - Empty cart
  - `updateQuantity(itemId, quantity)` - Change quantity
- **Database:** Firebase `Users/{userId}/cart/` and local SQLite

### 4. **OrderRepository**
**File:** `Respository/OrderRepository.java`
- **Purpose:** Manage orders
- **Methods:**
  - `createOrder(order)` - Save new order
  - `getOrders(userId)` - Get user's orders
  - `updateOrderStatus(orderId, status)` - Change order status
  - `getOrderDetails(orderId)` - Get single order
- **Database:** Firebase `Orders/{userId}/`

---

## 🧠 ViewModels (Business Logic)

### 1. **MainViewModel**
**File:** `ViewModel/MainViewModel.java`
- **Purpose:** Provide data to HomeFragment and ProductListActivity
- **Methods:**
  - `loadCategory()` - Returns LiveData<ArrayList<CategoryModel>>
  - `loadBanner()` - Returns LiveData<ArrayList<BannerModel>>
  - `loadPopular()` - Returns LiveData<ArrayList<ItemsModel>>
  - `loadNewArrivals()` - Returns LiveData<ArrayList<ItemsModel>>
  - `loadRecommended()` - Returns LiveData<ArrayList<ItemsModel>>
- **Data Source:** MainRepository
- **Pattern:** MVVM with LiveData observers

---

## 🛠️ Helper/Utility Classes

### 1. **ManagmentCart**
**File:** `Helper/ManagmentCart.java`
- **Purpose:** Local cart management
- **Methods:**
  - `insertItem(item)` - Add to local cart
  - `getListCart()` - Get all cart items
  - `getNumberCart()` - Get total items count
  - `removeItem(itemId)` - Remove item
  - `clearCart()` - Empty cart
- **Database:** Local SQLite database

### 2. **UserPreferences**
**File:** `Helper/UserPreferences.java`
- **Purpose:** Store user data locally
- **Methods:**
  - `saveUser(userId, name, email)` - Save user info
  - `getUser()` - Get current user
  - `isLoggedIn()` - Check login status
  - `logout()` - Clear user data
  - `getUserId()` - Get user ID
  - `getUserEmail()` - Get user email
- **Storage:** Android SharedPreferences

---

## 🔄 Data Flow Examples

### Example 1: Adding Product to Cart
```
DetailActivity 
  → User clicks "Add to Cart"
  → CartAdapter.insertItem() 
  → ManagmentCart.insertItem() (saves to SQLite)
  → Firebase Cart created for logged-in user
  → Toast confirmation shown
```

### Example 2: Checkout Process
```
MyCartFragment
  → User selects items & clicks "Thanh Toán"
  → PaymentActivity receives selected items
  → User selects payment method
  → clicks "Thanh Toán NGAY"
  → placeOrder() called
  → Order saved to Firebase: Orders/{userId}/{orderId}
  → Cart items removed from Firebase
  → Navigate to MyOrderFragment
  → User sees new order
```

### Example 3: Loading Home Screen
```
HomeFragment
  → MainViewModel.loadPopular() 
  → MainRepository queries Firebase "Items"
  → Returns LiveData<ArrayList<ItemsModel>>
  → PopularAdapter displays products
  → User can click to DetailActivity
```

---

## 🗂️ Database Structure (Firebase Realtime Database)

```
├── Users/
│   └── {userId}/
│       ├── profile/
│       │   ├── name
│       │   ├── email
│       │   └── phone
│       ├── cart/
│       │   └── {itemId}/
│       │       ├── title
│       │       ├── price
│       │       ├── picUrl
│       │       ├── color
│       │       ├── size
│       │       └── quantity
│       └── wishlist/
│           └── {itemId}/ (same as cart)
│
├── Items/
│   └── {itemId}/
│       ├── title
│       ├── description
│       ├── price
│       ├── oldPrice
│       ├── rating
│       ├── review
│       ├── picUrl[]
│       ├── color[]
│       └── size[]
│
├── Orders/
│   └── {userId}/
│       └── {orderId}/
│           ├── orderId
│           ├── userId
│           ├── subtotal
│           ├── tax
│           ├── delivery
│           ├── total
│           ├── status
│           ├── createdAt
│           └── items[]
│
├── Category/
│   └── {categoryId}/
│       ├── id
│       ├── name
│       └── icon
│
├── Banner/
│   └── {bannerId}/
│       └── url
│
└── AppSettings/
    ├── currency
    ├── currencySymbol
    ├── taxRate
    ├── shippingFee
    ├── freeShippingThreshold
    └── maxCartItems
```

---

## 🐛 Recent Bug Fixes

### Issue 1: Payment Crash (FIXED ✓)
**Problem:** App crashed when clicking "Thanh Toán" button
**Root Cause:** Firebase type conversion error - Long values stored as String fields
**Solution:** 
- Added try-catch in `MainRepository.loadAppSettings()`
- Manual field-by-field parsing with type checking
- Proper conversion using `((Number) value).doubleValue()`
**Files Modified:**
- `Respository/MainRepository.java`
- `Activity/PaymentActivity.java`

### Issue 2: Silent App Exit (FIXED ✓)
**Problem:** App exited without crashing
**Root Cause:** 
- Missing error handling in order placement
- Firebase operations completing too quickly before navigation
**Solutions:**
- Added comprehensive error handling with try-catch
- Delayed navigation (500ms) to ensure Firebase operations complete
- Detailed logging for debugging
- Graceful fallback on errors
**Files Modified:**
- `Activity/PaymentActivity.java`

---

## 🔐 Security Features

1. **Firebase Authentication** - Secure user login/registration
2. **User Preferences** - Local encrypted storage of user session
3. **Database Rules** - Firebase security rules (configured server-side)
4. **No Hardcoded Credentials** - Config in Firebase Console

---

## 📝 File Naming Conventions

| Component | Naming Pattern | Example |
|-----------|---|---|
| Activities | `{Name}Activity.java` | `LoginActivity.java` |
| Fragments | `{Name}Fragment.java` | `HomeFragment.java` |
| Adapters | `{Name}Adapter.java` | `PopularAdapter.java` |
| Models | `{Name}Model.java` | `ItemsModel.java` |
| Repositories | `{Name}Repository.java` | `AuthRepository.java` |
| ViewModels | `{Name}ViewModel.java` | `MainViewModel.java` |
| Helpers | `{Name}.java` | `ManagmentCart.java` |
| Layouts | `activity_{name}.xml` or `fragment_{name}.xml` | `activity_login.xml` |

---

## 🚀 How to Navigate the Code

### If you want to understand **User Registration**:
1. Start: `SplashActivity.java` (entry)
2. Then: `RegisterActivity.java` (registration UI)
3. Then: `AuthRepository.registerUser()` (Firebase logic)
4. Then: `UserPreferences.saveUser()` (local storage)
5. End: `MainContainerActivity.java` (main app)

### If you want to understand **Shopping Process**:
1. Start: `HomeFragment.java` (browse)
2. Then: `DetailActivity.java` (view details)
3. Then: `CartAdapter` + `CartRepository` (add to cart)
4. Then: `MyCartFragment.java` (view cart)
5. Then: `PaymentActivity.java` (checkout)
6. Then: `OrderModel` + `OrderRepository` (save order)
7. End: `MyOrderFragment.java` (view orders)

### If you want to understand **Data Loading**:
1. Start: `MainViewModel.java`
2. Then: `MainRepository.java` (Firebase queries)
3. Then: `Models` (data classes)
4. Then: `Adapters` (display data)

---

## 📞 Contact & Support

This app was built with AI assistance. All code follows Android best practices and uses Firebase for backend services.

**Key Technologies:**
- ✅ Firebase Realtime Database
- ✅ Firebase Authentication
- ✅ MVVM Architecture
- ✅ View Binding
- ✅ LiveData Observers
- ✅ RecyclerView with Adapters
- ✅ Bottom Navigation
- ✅ Material Design

---

**Last Updated:** December 27, 2025
**Version:** 1.0
