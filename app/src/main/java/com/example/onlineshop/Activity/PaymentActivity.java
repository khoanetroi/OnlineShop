package com.example.onlineshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.PaymentMethodAdapter;
import com.example.onlineshop.Adapter.PaymentProductAdapter;
import com.example.onlineshop.Model.AppSettingsModel;
import com.example.onlineshop.Model.ItemsModel;
import com.example.onlineshop.Model.OrderModel;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.R;
import com.example.onlineshop.Respository.MainRepository;
import com.example.onlineshop.databinding.ActivityPaymentBinding;
import com.example.onlineshop.databinding.BottomSheetPaymentMethodBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    private ActivityPaymentBinding binding;
    private ArrayList<ItemsModel> items = new ArrayList<>();
    private double subtotal;
    private double tax;
    private double delivery;
    private double total;
    private ManagmentCart managmentCart;
    private PaymentMethodAdapter.PaymentMethod selectedPaymentMethod;
    private BottomSheetDialog paymentMethodBottomSheet;
    private MainRepository mainRepository;
    private AppSettingsModel appSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart = new ManagmentCart(this);
        mainRepository = new MainRepository();

        // Initialize appSettings with default values before loading from Firebase
        appSettings = new AppSettingsModel();
        appSettings.setCurrency("VND");
        appSettings.setCurrencySymbol("đ");
        appSettings.setTaxRate(0.1);
        appSettings.setShippingFee(10);

        loadAppSettings();

        getIntentData();
        setupRecycler();
        setupSummary();
        setupListeners();
        
        updatePaymentMethodPlaceholder();
    }

    private void getIntentData() {
        Serializable selectedExtra = getIntent().getSerializableExtra("selectedItems");
        if (selectedExtra instanceof ArrayList<?>) {
            try {
                items = (ArrayList<ItemsModel>) selectedExtra;
            } catch (Exception ignored) {
            }
        }
        
        if (items == null || items.isEmpty()) {
            Serializable extra = getIntent().getSerializableExtra("cart_items");
            if (extra instanceof ArrayList<?>) {
                try {
                    items = (ArrayList<ItemsModel>) extra;
                } catch (Exception ignored) {
                }
            }
        }

        if (items == null || items.isEmpty()) {
            items = managmentCart.getListCart();
        }

        String subtotalStr = getIntent().getStringExtra("subtotal");
        String taxStr = getIntent().getStringExtra("tax");
        String deliveryStr = getIntent().getStringExtra("delivery");
        String totalStr = getIntent().getStringExtra("total");
        
        if (subtotalStr != null && !subtotalStr.isEmpty()) {
            subtotal = parsePrice(subtotalStr);
            tax = parsePrice(taxStr);
            delivery = parsePrice(deliveryStr);
            total = parsePrice(totalStr);
        } else {
            subtotal = getIntent().getDoubleExtra("subtotal", 0);
            tax = getIntent().getDoubleExtra("tax", 0);
            delivery = getIntent().getDoubleExtra("delivery", 0);
            total = getIntent().getDoubleExtra("total", 0);
        }

        if (subtotal == 0 && !items.isEmpty()) {
            for (ItemsModel item : items) {
                subtotal += item.getPrice() * item.getNumberinCart();
            }
            tax = Math.round((subtotal * 0.1) * 100.0) / 100.0;
            delivery = subtotal > 100 ? 0 : 10;
            total = Math.round((subtotal + tax + delivery) * 100.0) / 100.0;
        }
    }
    
    private double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return 0;
        String cleaned = priceStr.replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setupRecycler() {
        binding.productsView.setLayoutManager(new LinearLayoutManager(this));
        binding.productsView.setAdapter(new PaymentProductAdapter(items));
        
        if (binding.productsLabelTxt != null) {
            binding.productsLabelTxt.setText("Sản Phẩm (" + items.size() + ")");
        }
    }

    private void setupSummary() {
        binding.subtotalTxt.setText(formatPrice(subtotal));
        binding.shippingTxt.setText(formatPrice(tax + delivery));
        binding.totalAmountTxt.setText(formatPrice(total));
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> {
            finish();
            overridePendingTransition(com.example.onlineshop.R.anim.slide_in_left, com.example.onlineshop.R.anim.slide_out_right);
        });
        
        binding.paymentMethodCard.setOnClickListener(v -> showPaymentMethodModal());

        binding.checkoutNowBtn.setOnClickListener(v -> placeOrder());
    }
    
    private void showPaymentMethodModal() {
        BottomSheetPaymentMethodBinding bottomSheetBinding = BottomSheetPaymentMethodBinding.inflate(LayoutInflater.from(this));
        paymentMethodBottomSheet = new BottomSheetDialog(this);
        paymentMethodBottomSheet.setContentView(bottomSheetBinding.getRoot());
        
        List<PaymentMethodAdapter.PaymentMethod> paymentMethods = Arrays.asList(
            new PaymentMethodAdapter.PaymentMethod("Thẻ Master Card", R.drawable.master_card),
            new PaymentMethodAdapter.PaymentMethod("Thẻ Visa", R.drawable.visa),
            new PaymentMethodAdapter.PaymentMethod("PayPal", R.drawable.paypal)
        );
        
        PaymentMethodAdapter adapter = new PaymentMethodAdapter(paymentMethods);
        adapter.setOnPaymentMethodSelectedListener(method -> {
            selectedPaymentMethod = method;
        });
        
        bottomSheetBinding.paymentMethodsView.setLayoutManager(new LinearLayoutManager(this));
        bottomSheetBinding.paymentMethodsView.setAdapter(adapter);
        
        bottomSheetBinding.confirmPaymentBtn.setOnClickListener(v -> {
            PaymentMethodAdapter.PaymentMethod selected = adapter.getSelectedMethod();
            if (selected != null) {
                selectedPaymentMethod = selected;
                updateSelectedPaymentMethod(selected);
                paymentMethodBottomSheet.dismiss();
            } else {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
        
        paymentMethodBottomSheet.show();
    }
    
    private void updateSelectedPaymentMethod(PaymentMethodAdapter.PaymentMethod method) {
        if (method != null) {
            binding.selectedPaymentNameTxt.setText(method.getName());
            binding.selectedPaymentIcon.setImageResource(method.getIconResId());
            binding.selectedPaymentIcon.setVisibility(View.VISIBLE);
            updateCheckoutButtonState(true);
        }
    }
    
    private void updatePaymentMethodPlaceholder() {
        binding.selectedPaymentNameTxt.setText("Chọn Phương Thức Thanh Toán");
        binding.selectedPaymentIcon.setVisibility(View.INVISIBLE);
        updateCheckoutButtonState(false);
    }
    
    private void updateCheckoutButtonState(boolean enabled) {
        binding.checkoutNowBtn.setEnabled(enabled);
        binding.checkoutNowBtn.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void loadAppSettings() {
        mainRepository.loadAppSettings().observe(this, settings -> {
            if (settings != null) {
                appSettings = settings;
                setupSummary();
            } else {
                appSettings = new AppSettingsModel();
                appSettings.setCurrency("VND");
                appSettings.setCurrencySymbol("đ");
                appSettings.setTaxRate(0.1);
                appSettings.setShippingFee(10);
            }
        });
    }

    private String formatPrice(double value) {
        if (appSettings == null) {
            return  String.format(Locale.getDefault(), "%.0f", value);
        }
        String symbol = appSettings.getCurrencySymbol();
        if (symbol == null || symbol.isEmpty()) {
            symbol = "đ";
        }
        return symbol + String.format(Locale.getDefault(), "%.0f", value);
    }

    private void placeOrder() {
        try {
            if (selectedPaymentMethod == null) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = firebaseUser.getUid();

            if (items == null || items.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng của bạn trống", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!binding.checkoutNowBtn.isEnabled()) {
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.checkoutNowBtn.setEnabled(false);
            binding.checkoutNowBtn.setAlpha(0.5f);

            DatabaseReference ordersRef = FirebaseDatabase.getInstance()
                    .getReference("Orders")
                    .child(uid);

            String orderId = ordersRef.push().getKey();
            if (orderId == null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.checkoutNowBtn.setEnabled(true);
                binding.checkoutNowBtn.setAlpha(1.0f);
                Toast.makeText(this, "Không thể tạo đơn hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            long currentTime = System.currentTimeMillis();
            OrderModel order = new OrderModel(
                    orderId,
                    uid,
                    subtotal,
                    tax,
                    delivery,
                    total,
                    currentTime,
                    "Đang Xử Lý",
                    items
            );

            order.setOrderDate(currentTime);
            order.setCreatedAt(currentTime);

            ordersRef.child(orderId).setValue(order)
                    .addOnSuccessListener(unused -> {
                        binding.progressBar.setVisibility(View.GONE);
                        updateCheckoutButtonState(true);

                        removeOrderedItemsFromCart(items);

                        Toast.makeText(PaymentActivity.this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();

                        // Delay navigation to ensure Firebase operations complete
                        binding.getRoot().postDelayed(() -> {
                            try {
                                Intent intent = new Intent(PaymentActivity.this, MainContainerActivity.class);
                                intent.putExtra("select_my_order", true);
                                intent.putExtra("show_orders_tab", true);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                PaymentActivity.this.finish();
                            } catch (Exception e) {
                                android.util.Log.e("PaymentActivity", "Navigation failed", e);
                                Toast.makeText(PaymentActivity.this, "Lỗi điều hướng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }, 500);
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        updateCheckoutButtonState(true);

                        String errorMessage = "Không thể đặt hàng";
                        if (e.getMessage() != null) {
                            errorMessage += ": " + e.getMessage();
                        }
                        Toast.makeText(PaymentActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                        android.util.Log.e("PaymentActivity", "Order placement failed", e);
                        android.util.Log.e("PaymentActivity", "Order details: orderId=" + orderId + ", userId=" + uid + ", items=" + (items != null ? items.size() : 0));
                        android.util.Log.e("PaymentActivity", "Order object: " + order.toString());
                    });
        } catch (Exception e) {
            android.util.Log.e("PaymentActivity", "Unexpected error in placeOrder", e);
            Toast.makeText(this, "Lỗi bất ngờ: " + e.getMessage(), Toast.LENGTH_LONG).show();
            binding.progressBar.setVisibility(View.GONE);
            binding.checkoutNowBtn.setEnabled(true);
            binding.checkoutNowBtn.setAlpha(1.0f);
        }
    }
    
    private void removeOrderedItemsFromCart(ArrayList<ItemsModel> orderedItems) {
        if (orderedItems == null || orderedItems.isEmpty()) return;
        
        try {
            ArrayList<ItemsModel> currentCart = managmentCart.getListCart();
            for (ItemsModel orderedItem : orderedItems) {
                for (int i = currentCart.size() - 1; i >= 0; i--) {
                    if (currentCart.get(i).getTitle().equals(orderedItem.getTitle())) {
                        currentCart.remove(i);
                        break;
                    }
                }
            }

            managmentCart.clearCart();
            for (ItemsModel item : currentCart) {
                managmentCart.insertItem(item);
            }
        } catch (Exception e) {
            android.util.Log.e("PaymentActivity", "Error updating local cart", e);
        }
        
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            try {
                DatabaseReference cartRef = FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(firebaseUser.getUid())
                        .child("cart");

                for (ItemsModel orderedItem : orderedItems) {
                    String title = orderedItem.getTitle();
                    if (title == null) continue;

                    cartRef.orderByChild("title").equalTo(title)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        child.getRef().removeValue().addOnFailureListener(e ->
                                            android.util.Log.e("PaymentActivity", "Error removing cart item from Firebase", e)
                                        );
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    android.util.Log.e("PaymentActivity", "Cart removal cancelled", error.toException());
                                }
                            });
                }
            } catch (Exception e) {
                android.util.Log.e("PaymentActivity", "Error removing Firebase cart items", e);
            }
        }
    }
}
