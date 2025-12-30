**Overview**
- **Project:** OnlineShop (Android)
- **Mục đích file:** Mô tả chi tiết từng screen/màn hình trong app, thành phần UI, layout, luồng dữ liệu, và các lưu ý kỹ thuật để developer dễ hiểu và tiếp tục phát triển.

---

**General Notes**
- **View binding / Data binding:** Project sử dụng ViewBinding/DataBinding (ví dụ: `ActivitySplashBinding`).
- **Session:** `UserPreferences` được dùng để lưu trạng thái đăng nhập.
- **Navigation:** Kết hợp giữa `Activity` (màn hình độc lập) và `Fragment` (các tab/section trong `MainContainerActivity`). Intents được dùng để điều hướng giữa Activity.
- **RecyclerView:** Được sử dụng rộng rãi cho danh sách sản phẩm, giỏ hàng, đơn hàng, favorites. Thường kèm `Adapter`, `ViewHolder`, layout item riêng (ví dụ: `item_product.xml`).
- **Network / Repository:** Tài liệu này giả định kiến trúc có lớp Repository/Network cho API; nếu chưa có, nên thêm để tách UI và data.
- **Firebase:** File cấu hình `google-services.json` và `firebase_database_*.json` có mặt — app có thể dùng Firebase cho DB/Notifications.

---

**Screens (Activities & Fragments)**

**1. `SplashActivity`**
- **Mục đích:** Màn hình khởi động, kiểm tra trạng thái đăng nhập và chuyển tiếp tới `MainContainerActivity` hoặc màn đăng ký/đăng nhập.
- **Entry point:** `SplashActivity` (LAUNCHER) — hiển thị logo/ nút bắt đầu.
- **UI components:** `startBtn` (Button), `textView3` (Login link). Sử dụng `ActivitySplashBinding`.
- **Logic:** Kiểm tra `userPreferences.isLoggedIn()` → nếu true chuyển `MainContainerActivity`, else cho phép người dùng `RegisterActivity` hoặc `LoginActivity`.
- **Notes:** Sử dụng `EdgeToEdge.enable(this)` để hỗ trợ layout full-screen.

**2. `LoginActivity`**
- **Mục đích:** Cho phép người dùng đăng nhập.
- **UI components:** Email/phone input, Password input, `btnLogin`, link `ForgotPasswordActivity`, progress/loading indicator.
- **Validation:** Kiểm tra input, hiển thị lỗi inline, disable `btnLogin` trong khi request.
- **On success:** Lưu trạng thái vào `UserPreferences`, chuyển tới `MainContainerActivity`.
- **States:** loading, success, error, validation errors.

**3. `RegisterActivity`**
- **Mục đích:** Đăng ký tài khoản mới.
- **UI components:** Inputs (name, email, phone, password, confirm), `btnRegister`, checkbox đồng ý điều khoản.
- **Flow:** Validate client-side → gọi API register → nếu success có thể auto-login và chuyển `MainContainerActivity`.

**4. `ForgotPasswordActivity`**
- **Mục đích:** Quên mật khẩu — gửi yêu cầu đặt lại.
- **UI components:** Email/Phone input, `btnSend`, feedback success/error.

**5. `MainContainerActivity`**
- **Mục đích:** Container chính chứa BottomNavigation hoặc Drawer và host các `Fragment` (Home, Favorites, Cart, MyOrder, Settings, ...).
- **Layout:** `FragmentContainerView` hoặc `FrameLayout` để load fragment; `BottomNavigationView` cho chuyển tab.
- **Fragments tiêu biểu được phát hiện:** `HomeFragment`, `FavoritesFragment`, `MyCartFragment`, `MyOrderFragment`, `SettingsFragment`, `EditProfileFragment`, `ChangePasswordFragment`.
- **Navigation:** Khi user chọn mục bottom → FragmentTransaction hoặc Navigation Component replace fragment.

**6. `HomeFragment`**
- **Mục đích:** Hiển thị danh sách sản phẩm, banner, categories, gợi ý.
- **UI components:** Một hoặc nhiều `RecyclerView` (ví dụ: horizontal carousel cho banners, grid/vertical cho sản phẩm). Có thể dùng `ViewPager2` cho slider.
- **RecyclerView details:** Sử dụng `Adapter` với diff util (nếu có) hoặc notify dataset change. Item layout: image, title, price, rating, add-to-cart button.
- **Data flow:** Fragment gọi ViewModel -> Repository -> API, show loading, show data, handle pagination/infinite scroll.

**7. `ProductListActivity`**
- **Mục đích:** Danh sách sản phẩm theo category hoặc kết quả tìm kiếm.
- **UI components:** `RecyclerView` (GridLayoutManager hoặc Linear), toolbar với filter/sort, pagination (Load more), swipe-to-refresh.
- **Adapter:** `ProductAdapter` trả về `ViewHolder` chứa item click -> mở `DetailActivity`.
- **States:** empty (no results), loading, error, content.

**8. `DetailActivity` (Product detail)**
- **Mục đích:** Hiển thị chi tiết sản phẩm, hình ảnh, mô tả, thông số kỹ thuật, reviews.
- **UI components:** Image carousel (`ViewPager2`), title, price, variant selector (size/color), `btnAddToCart`, `btnBuyNow`, reviews section (RecyclerView), related products (RecyclerView horizontal).
- **Logic:** Add to cart cập nhật local cart DB hoặc gọi API; Buy Now chuyển sang `PaymentActivity` hoặc Checkout.

**9. `MyCartFragment` / Cart screen**
- **Mục đích:** Hiển thị các mục trong giỏ hàng, tổng tiền, thao tác thay đổi số lượng và xóa.
- **UI components:** `RecyclerView` cho cart items, each item có + / - / delete, `btnCheckout`.
- **Adapter:** `CartAdapter` với callback để cập nhật quantity; cập nhật ViewModel/Repository ngay.
- **States:** empty cart, loading, error.

**10. `PaymentActivity` / Checkout**
- **Mục đích:** Thực hiện thanh toán — chọn địa chỉ, phương thức thanh toán, review order.
- **UI components:** Address selector, payment methods, order summary, `btnPay`.
- **Notes:** Thực hiện validation, show confirmation, handle payment gateway callbacks.

**11. `MyOrderFragment` / `OrderDetailActivity`**
- **Mục đích:** Liệt kê các đơn hàng và xem chi tiết từng đơn.
- **UI components:** `RecyclerView` cho orders (adapter hiển thị status, date, total), chi tiết đơn hiển thị list sản phẩm (RecyclerView), trạng thái giao hàng.

**12. `FavoritesFragment` (Yêu thích)**
- **Mục đích:** Hiển thị danh sách sản phẩm đánh dấu yêu thích.
- **UI components:** `RecyclerView` (grid), nút để bỏ thích hoặc chuyển tới detail.

**13. `SettingsFragment`, `EditProfileFragment`, `ChangePasswordFragment`**
- **Mục đích:** Quản lý tài khoản, cấu hình app, đổi mật khẩu.
- **UI components:** Forms, switches, sign out button.

**14. `SearchActivity`**
- **Mục đích:** Tìm kiếm sản phẩm.
- **UI components:** SearchView / EditText, suggestions, `RecyclerView` cho kết quả.
- **Behavior:** Debounce input, call search API, support sort/filter.

**15. `NotificationActivity`**
- **Mục đích:** Hiển thị thông báo (promotions, order updates).
- **UI components:** `RecyclerView` cho list notifications.

---

**Chi tiết kỹ thuật cho mỗi screen (mẫu checklist)**
- **Purpose:** Mô tả ngắn.
- **Entry point / Navigation:** Từ đâu tới đây, Intent/Fragment Transaction, args cần truyền.
- **Layout file:** tên layout (ví dụ: `activity_detail.xml`, `fragment_home.xml`), phần tử chính.
- **UI components:** Buttons, EditTexts, RecyclerView, Toolbars, BottomNavigation.
- **Data flow:** ViewModel -> Repository -> API / Local DB / SharedPreferences.
- **Adapter:** Tên adapter, ViewHolder, item layout, cách xử lý click.
- **States to handle:** Loading, Empty, Error, Content.
- **Edge cases & Validation:** Input validation, network error, session expiry.
- **Accessibility & Localization:** ContentDescription cho ảnh, strings.xml cho mọi text.
- **Testing:** UI tests (Espresso) cho flows chính, unit tests cho ViewModel/Repository.
- **Performance:** Use pagination, DiffUtil for RecyclerView, lazy image loading (Glide/Picasso), avoid heavy work on UI thread.

---

**Ví dụ chi tiết: `ProductListActivity`**
- **Layout:** `activity_product_list.xml` gồm `Toolbar`, `SwipeRefreshLayout`, `RecyclerView` (id: `recyclerProducts`).
- **RecyclerView config:** `recyclerProducts.setLayoutManager(new GridLayoutManager(context, 2));`
- **Adapter:** `ProductAdapter`:
  - `onCreateViewHolder` inflate `item_product.xml` (image, name, price, rating, favorite button)
  - `onBindViewHolder` bind dữ liệu, set click listener open `DetailActivity` với product id.
  - Sử dụng `ListAdapter` + `DiffUtil.ItemCallback<Product>` nếu có.
- **Pagination:** Listen scroll → khi near end gọi API tiếp theo và append to list. Show loading item ở cuối.
- **Filters/Sort:** Toolbar chứa filter icon → show bottom sheet để chọn.

---

**Data Models (đề xuất)**
- `Product { id, title, description, price, images[], rating, stock, categoryId }`
- `CartItem { productId, quantity, selectedOptions }`
- `Order { id, items[], total, status, createdAt }`
- `User { id, name, email, phone, addresses[] }`

---

**Best Practices & Recommendations**
- Tách UI và business logic: sử dụng `ViewModel` + `Repository`.
- Dùng `LiveData` / `StateFlow` để observe trạng thái UI.
- Sử dụng `DiffUtil` + `ListAdapter` cho RecyclerView.
- Caching: local DB (Room) cho cart, favorites, caching sản phẩm.
- Error handling: hiển thị Snackbar cho lỗi ngắn, dialog cho lỗi quan trọng.
- Security: validate inputs, tránh leak token, dùng HTTPS cho API.
- Internationalization: mọi text trong `strings.xml`.
- Accessibility: contentDescription cho image, đủ contrast, kích thước target touch >= 48dp.

---

**Checklist triển khai/handover cho mỗi màn hình**
- Tên màn hình và file liên quan (`Activity`/`Fragment` + layout).
- Mô tả flow chính (user story).
- API endpoints cần gọi (method, params, response model).
- Data models và mapping JSON.
- Unit tests cho ViewModel / Repository.
- UI tests cho flow quan trọng.

---

**Kết luận & bước tiếp theo**
- Tôi đã soạn bản mô tả tổng quát và chi tiết cho các màn hình chính tìm được trong project.
- Nếu bạn muốn, tôi sẽ:
  - Bổ sung tên file layout và tên adapter cụ thể bằng cách quét thêm repository.
  - Sinh checklist kỹ thuật cho từng màn hình dưới dạng tasks commit-ready.
  - Chỉnh sửa theo phong cách viết của team bạn.

Vui lòng cho biết bạn muốn tôi cập nhật thêm chi tiết nào (ví dụ: endpoints API, tên layout file, hoặc sơ đồ navigation).

---

**Lý thuyết & Q&A (dùng để ôn thi với giáo viên)**

1) HomeFragment — "Trang Home dùng view gì?" 
- Trả lời: Trang Home thường dùng một combination của `ConstraintLayout` hoặc `CoordinatorLayout` làm container chính; hiển thị `ViewPager2` cho banner/slider, và dùng một hoặc nhiều `RecyclerView` để hiển thị danh sách sản phẩm (horizontal carousel cho các category/related items, grid/vertical cho product list). `SwipeRefreshLayout` có thể bọc `RecyclerView` để hỗ trợ pull-to-refresh.

2) Tại sao dùng `RecyclerView` thay vì `ListView`? 
- Trả lời: `RecyclerView` linh hoạt hơn, hỗ trợ `LayoutManager` (Linear/Grid/ StaggeredGrid), tái sử dụng `ViewHolder` hiệu quả, tích hợp `ItemAnimator`, `ItemDecoration`, và dễ dàng dùng `ListAdapter` + `DiffUtil` để tối ưu cập nhật dữ liệu.

3) Fragment vs Activity — khác nhau thế nào? Khi nào dùng Fragment?
- Trả lời: `Activity` là container cấp hệ thống (task, back stack), `Fragment` là thành phần UI có vòng đời phụ thuộc Activity, dùng để chia nhỏ UI, tái sử dụng, và host nhiều màn hình bên trong một Activity (vd: BottomNavigation hay ViewPager). Dùng Fragment khi bạn cần nhiều màn hình nhỏ trong cùng một container hoặc khi muốn reuse UI trên tablet/phone.

4) View Binding / Data Binding — sử dụng ra sao và lợi ích?
- Trả lời: View Binding tạo class binding an toàn cho views (không cần `findViewById`), giúp tránh NPE và mã rõ ràng hơn. Data Binding còn hỗ trợ binding expressions, two-way binding và binding adapters.

5) Kiến trúc MVVM trong app Android — mô tả ngắn gọn
- Trả lời: MVVM tách UI (View/Fragment/Activity) khỏi logic (ViewModel) và data (Repository). `ViewModel` expose `LiveData`/`StateFlow` để View observe, `Repository` quản lý nguồn dữ liệu (API, DB). Giúp test được logic, tách phụ thuộc và dễ maintain.

6) ViewModel vs LiveData vs StateFlow — khi nào dùng gì?
- Trả lời: `ViewModel` giữ trạng thái UI qua cấu hình thay đổi. `LiveData` là observable lifecycle-aware, phù hợp với XML/Activities/Fragments. `StateFlow` (coroutines) cho streaming state modern, dễ combine, testable; cả hai đều dùng trong ViewModel tuỳ dự án.

7) Cách implement pagination trong `RecyclerView`?
- Trả lời: Sử dụng `RecyclerView.OnScrollListener` để detect khi cuộn tới cuối và gọi API tiếp theo; hoặc dùng Paging 3 library (khuyến nghị) để quản state, caching và retry tự động.

8) Làm sao xử lý click item trong `RecyclerView`? 
- Trả lời: Adapter dùng callback interface hoặc lambda truyền từ Activity/Fragment vào `ViewHolder`. Không xử lý navigation trực tiếp trong Adapter; Adapter chỉ gọi callback, View (Fragment/Activity) thực hiện navigation.

9) Navigation giữa screens — dùng Intent hay Navigation Component?
- Trả lời: Có thể dùng `Intent` để start Activity; cho các Fragment khuyên dùng `Navigation Component` (safe args) để dễ quản back stack, deep links và transitions.

10) Xử lý trạng thái UI (loading, empty, error) — best practice?
- Trả lời: Model hoá UI state (ví dụ: sealed class { Loading, Success(data), Empty, Error(msg) }) trong ViewModel; View observe state và render từng layout/placeholder tương ứng. Giữ UI logic trong View, business logic trong ViewModel.

11) Accessibility & Localization — những điểm cần chuẩn bị?
- Trả lời: Đưa toàn bộ text vào `strings.xml`, cung cấp `contentDescription` cho image, đảm bảo contrast, kích thước target touch >=48dp, hỗ trợ TalkBack, test với Locale khác.

12) Unit test & UI test — viết ở đâu và test gì?
- Trả lời: Unit test cho ViewModel/Repository (mock API/DAO). UI test (Espresso) cho flows chính: đăng nhập, thêm giỏ hàng, checkout. Test edge cases như mất mạng, token expired.

13) Ví dụ câu hỏi phỏng vấn ngắn (giáo viên có thể hỏi):
- "Khi user nhấn Add to Cart, dữ liệu phải cập nhật chỗ nào?" → ViewModel cập nhật Repository, Repository lưu local (Room) hoặc call API; View observe thay đổi và cập nhật UI.
- "Làm sao đảm bảo hình ảnh load nhanh?" → Dùng image caching và lazy loading (Glide/Picasso), tối ưu kích thước ảnh, dùng placeholder.
- "Làm sao bảo mật token?" → Lưu token an toàn (EncryptedSharedPreferences/Keystore), dùng HTTPS.

---

Ghi chú: Tôi đã thêm phần lý thuyết & Q&A vào tài liệu. Bạn muốn tôi chuyển các câu hỏi này thành flashcards (một file riêng) để tiện ôn không?
