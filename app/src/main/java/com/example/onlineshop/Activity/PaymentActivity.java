package com.example.onlineshop.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.PaymentMethodAdapter;
import com.example.onlineshop.Adapter.PaymentProductAdapter;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.Domain.OrderModel;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart = new ManagmentCart(this);

        getIntentData();
        setupRecycler();
        setupSummary();
        setupListeners();
    }

    private void getIntentData() {
        Serializable extra = getIntent().getSerializableExtra("cart_items");
        if (extra instanceof ArrayList<?>) {
            try {
                //noinspection unchecked
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
        
        // Update products label with count
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
        
        // Payment method card click to show modal
        binding.paymentMethodCard.setOnClickListener(v -> showPaymentMethodModal());

        binding.checkoutNowBtn.setOnClickListener(v -> placeOrder());
    }
    
    private void showPaymentMethodModal() {
        BottomSheetPaymentMethodBinding bottomSheetBinding = BottomSheetPaymentMethodBinding.inflate(LayoutInflater.from(this));
        paymentMethodBottomSheet = new BottomSheetDialog(this);
        paymentMethodBottomSheet.setContentView(bottomSheetBinding.getRoot());
        
        // Create payment methods list
        List<PaymentMethodAdapter.PaymentMethod> paymentMethods = Arrays.asList(
            new PaymentMethodAdapter.PaymentMethod("Master Card", R.drawable.master_card),
            new PaymentMethodAdapter.PaymentMethod("Visa", R.drawable.visa),
            new PaymentMethodAdapter.PaymentMethod("PayPal", R.drawable.paypal)
        );
        
        PaymentMethodAdapter adapter = new PaymentMethodAdapter(paymentMethods);
        adapter.setOnPaymentMethodSelectedListener(method -> {
            selectedPaymentMethod = method;
            // Don't dismiss here, let user confirm
        });
        
        bottomSheetBinding.paymentMethodsView.setLayoutManager(new LinearLayoutManager(this));
        bottomSheetBinding.paymentMethodsView.setAdapter(adapter);
        
        // Confirm button in modal
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
        }
    }

    private String formatPrice(double value) {
        return "$" + String.format(Locale.US, "%.2f", value);
    }

    private void placeOrder() {
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

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.checkoutNowBtn.setEnabled(false);

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

        OrderModel order = new OrderModel(
                orderId,
                uid,
                subtotal,
                tax,
                delivery,
                total,
                System.currentTimeMillis(),
                "Pending",
                items
        );

        ordersRef.child(orderId).setValue(order)
                .addOnSuccessListener(unused -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.checkoutNowBtn.setEnabled(true);

                    // Clear local cart after successful order
                    managmentCart.clearCart();

                    Toast.makeText(PaymentActivity.this, "Order placed successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.checkoutNowBtn.setEnabled(true);
                    Toast.makeText(PaymentActivity.this, "Failed to place order", Toast.LENGTH_SHORT).show();
                });
    }
}
