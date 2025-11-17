package com.example.onlineshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.PaymentMethodAdapter;
import com.example.onlineshop.Adapter.PaymentProductAdapter;
import com.example.onlineshop.Domain.AppSettingsModel;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.Domain.OrderModel;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.R;
import com.example.onlineshop.Respository.MainRepository;
import com.example.onlineshop.databinding.ActivityPaymentBinding;
import com.example.onlineshop.databinding.BottomSheetPaymentMethodBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        loadAppSettings();

        getIntentData();
        setupRecycler();
        setupSummary();
        setupListeners();
        
        updatePaymentMethodPlaceholder();
    }

    private void getIntentData() {
        Serializable extra = getIntent().getSerializableExtra("cart_items");
        if (extra instanceof ArrayList<?>) {
            try {
                items = (ArrayList<ItemsModel>) extra;
            } catch (Exception ignored) {
            }
        }

        if (items == null || items.isEmpty()) {
            items = managmentCart.getListCart();
        }

        subtotal = getIntent().getDoubleExtra("subtotal", 0);
        tax = getIntent().getDoubleExtra("tax", 0);
        delivery = getIntent().getDoubleExtra("delivery", 0);
        total = getIntent().getDoubleExtra("total", 0);

        if (subtotal == 0) {
            subtotal = managmentCart.getTotalFee();
            double percentTax = 0.02;
            double defaultDelivery = 10;
            tax = Math.round((subtotal * percentTax) * 100.0) / 100.0;
            delivery = defaultDelivery;
            total = Math.round((subtotal + tax + delivery) * 100.0) / 100.0;
        }
    }

    private void setupRecycler() {
        binding.productsView.setLayoutManager(new LinearLayoutManager(this));
        binding.productsView.setAdapter(new PaymentProductAdapter(items));
        
        if (binding.productsLabelTxt != null) {
            binding.productsLabelTxt.setText("Products (" + items.size() + ")");
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
            new PaymentMethodAdapter.PaymentMethod("Master Card", R.drawable.master_card),
            new PaymentMethodAdapter.PaymentMethod("Visa", R.drawable.visa),
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
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
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
        binding.selectedPaymentNameTxt.setText("Select Payment Method");
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
                appSettings.setCurrency("USD");
                appSettings.setCurrencySymbol("$");
                appSettings.setTaxRate(0.1);
                appSettings.setShippingFee(10);
            }
        });
    }

    private String formatPrice(double value) {
        String symbol = appSettings != null ? appSettings.getCurrencySymbol() : "$";
        return symbol + String.format(Locale.getDefault(), "%.2f", value);
    }

    private void placeOrder() {
        if (selectedPaymentMethod == null) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Please login to place an order", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = firebaseUser.getUid();

        if (items == null || items.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Failed to create order", Toast.LENGTH_SHORT).show();
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
                "On Progress",
                items
        );
        
        order.setOrderDate(currentTime);
        order.setCreatedAt(currentTime);

        ordersRef.child(orderId).setValue(order)
                .addOnSuccessListener(unused -> {
                    binding.progressBar.setVisibility(View.GONE);
                    updateCheckoutButtonState(true);

                    managmentCart.clearCart();

                    Toast.makeText(PaymentActivity.this, "Order placed successfully", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(PaymentActivity.this, MainContainerActivity.class);
                    intent.putExtra("select_my_order", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    updateCheckoutButtonState(true);
                    
                    String errorMessage = "Failed to place order";
                    if (e.getMessage() != null) {
                        errorMessage += ": " + e.getMessage();
                    }
                    Toast.makeText(PaymentActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    
                    android.util.Log.e("PaymentActivity", "Order placement failed", e);
                    android.util.Log.e("PaymentActivity", "Order details: orderId=" + orderId + ", userId=" + uid + ", items=" + (items != null ? items.size() : 0));
                    android.util.Log.e("PaymentActivity", "Order object: " + order.toString());
                });
    }
}
