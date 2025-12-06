package com.example.onlineshop.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.CartAdapter;
import com.example.onlineshop.Adapter.OrderAdapter;
import com.example.onlineshop.Domain.AppSettingsModel;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.Domain.OrderModel;
import com.example.onlineshop.Helper.ChangeNumberItemsListener;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.R;
import com.example.onlineshop.Respository.MainRepository;
import com.example.onlineshop.Respository.OrderRepository;
import com.example.onlineshop.databinding.ActivityMyOrderBinding;

import java.util.ArrayList;
import java.util.Locale;

public class MyOrderFragment extends Fragment {
    private ActivityMyOrderBinding binding;
    private OrderRepository orderRepository;
    private MainRepository mainRepository;
    private OrderAdapter orderAdapter;
    private CartAdapter cartAdapter;
    private ManagmentCart managmentCart;
    private AppSettingsModel appSettings;
    private boolean isCartTab = true;
    private ArrayList<OrderModel> allOrders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityMyOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        orderRepository = new OrderRepository();
        mainRepository = new MainRepository();
        managmentCart = new ManagmentCart(requireContext());
        
        loadAppSettings();
        setupListeners();
        setupCartRecyclerView();
        setupOrdersRecyclerView();
        setupTabs();
        loadOrders();
    }

    private void loadAppSettings() {
        mainRepository.loadAppSettings().observe(getViewLifecycleOwner(), settings -> {
            if (settings != null) {
                appSettings = settings;
                if (cartAdapter != null && !cartAdapter.getSelectedItems().isEmpty()) {
                    updateCheckoutModal();
                }
            } else {
                appSettings = new AppSettingsModel();
                appSettings.setCurrency("USD");
                appSettings.setCurrencySymbol("$");
                appSettings.setTaxRate(0.1);
                appSettings.setShippingFee(10);
                appSettings.setFreeShippingThreshold(100);
            }
        });
    }

    private void setupListeners() {
        binding.bagIcon.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.onlineshop.Activity.NotificationActivity.class);
            startActivity(intent);
        });
    }

    private void setupCartRecyclerView() {
        binding.cartView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        cartAdapter = new CartAdapter(managmentCart.getListCart(), requireContext(), new ChangeNumberItemsListener() {
            @Override
            public void changed() {
                updateCheckoutModal();
                updateCartDisplay();
            }
        });
        cartAdapter.setSelectionListener(new CartAdapter.OnItemSelectionChangedListener() {
            @Override
            public void onSelectionChanged(int selectedCount) {
                if (selectedCount > 0) {
                    updateCheckoutModal();
                    binding.checkoutModalContainer.setVisibility(View.VISIBLE);
                } else {
                    binding.checkoutModalContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onItemChecked(ItemsModel item, int position) {
                updateCheckoutModal();
                binding.checkoutModalContainer.setVisibility(View.VISIBLE);
            }
        });
        binding.cartView.setAdapter(cartAdapter);
    }

    private void setupOrdersRecyclerView() {
        binding.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        orderAdapter = new OrderAdapter(new ArrayList<>());
        orderAdapter.setActionListener(new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onDetailClick(OrderModel order) {
            }

            @Override
            public void onTrackingClick(OrderModel order) {
            }

            @Override
            public void onReceiveOrderClick(OrderModel order) {
                orderRepository.updateOrderStatus(order.getOrderId(), "Đã Giao");
                loadOrders();
            }
        });
        binding.ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void setupTabs() {
        binding.cartTab.setOnClickListener(v -> switchToCartTab());
        binding.myOrderTab.setOnClickListener(v -> switchToOrdersTab());
        
        switchToCartTab();
    }

    private void switchToCartTab() {
        isCartTab = true;
        binding.cartTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        binding.cartTab.setTypeface(null, android.graphics.Typeface.BOLD);
        binding.myOrderTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey));
        binding.myOrderTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        binding.tabIndicator.post(() -> {
            binding.tabIndicator.animate()
                    .translationX(0)
                    .setDuration(200)
                    .start();
        });
        
        binding.cartContainer.setVisibility(View.VISIBLE);
        binding.ordersContainer.setVisibility(View.GONE);
        
        updateCartDisplay();
    }

    private void switchToOrdersTab() {
        isCartTab = false;
        binding.myOrderTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        binding.myOrderTab.setTypeface(null, android.graphics.Typeface.BOLD);
        binding.cartTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey));
        binding.cartTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        binding.tabIndicator.post(() -> {
            float translationX = binding.myOrderTab.getLeft() - binding.cartTab.getLeft();
            binding.tabIndicator.animate()
                    .translationX(translationX)
                    .setDuration(200)
                    .start();
        });
        
        binding.cartContainer.setVisibility(View.GONE);
        binding.ordersContainer.setVisibility(View.VISIBLE);
        binding.checkoutModalContainer.setVisibility(View.GONE);
        
        updateOrdersDisplay();
    }

    private void loadOrders() {
        orderRepository.loadAllOrders().observe(getViewLifecycleOwner(), orders -> {
            allOrders = orders != null ? orders : new ArrayList<>();
            updateOrdersDisplay();
        });
    }

    private void updateCartDisplay() {
        if (managmentCart.getListCart().isEmpty()) {
            binding.emptyCartTxt.setVisibility(View.VISIBLE);
            binding.cartScrollView.setVisibility(View.GONE);
            binding.checkoutModalContainer.setVisibility(View.GONE);
        } else {
            binding.emptyCartTxt.setVisibility(View.GONE);
            binding.cartScrollView.setVisibility(View.VISIBLE);
            cartAdapter = new CartAdapter(managmentCart.getListCart(), requireContext(), new ChangeNumberItemsListener() {
                @Override
                public void changed() {
                    updateCheckoutModal();
                    updateCartDisplay();
                }
            });
            cartAdapter.setSelectionListener(new CartAdapter.OnItemSelectionChangedListener() {
                @Override
                public void onSelectionChanged(int selectedCount) {
                    if (selectedCount > 0) {
                        updateCheckoutModal();
                        binding.checkoutModalContainer.setVisibility(View.VISIBLE);
                    } else {
                        binding.checkoutModalContainer.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onItemChecked(ItemsModel item, int position) {
                    updateCheckoutModal();
                    binding.checkoutModalContainer.setVisibility(View.VISIBLE);
                }
            });
            binding.cartView.setAdapter(cartAdapter);
        }
    }

    private void updateOrdersDisplay() {
        if (allOrders.isEmpty()) {
            binding.emptyOrdersTxt.setVisibility(View.VISIBLE);
            binding.ordersRecyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyOrdersTxt.setVisibility(View.GONE);
            binding.ordersRecyclerView.setVisibility(View.VISIBLE);
            orderAdapter.setOrders(allOrders);
        }
    }

    private void updateCheckoutModal() {
        if (cartAdapter == null) return;
        
        ArrayList<ItemsModel> selectedItems = cartAdapter.getSelectedItems();
        
        if (selectedItems.isEmpty()) {
            binding.checkoutModalContainer.setVisibility(View.GONE);
            return;
        }

        if (appSettings == null) {
            return;
        }
        
        double rawSubtotal = 0;
        for (ItemsModel item : selectedItems) {
            rawSubtotal += item.getPrice() * item.getNumberinCart();
        }

        double taxRate = appSettings.getTaxRate();
        double shippingFee = appSettings.getShippingFee();
        
        if (rawSubtotal >= appSettings.getFreeShippingThreshold()) {
            shippingFee = 0;
        }
        
        double calculatedTax = Math.round((rawSubtotal * taxRate) * 100.0) / 100.0;
        double calculatedSubtotal = Math.round(rawSubtotal * 100.0) / 100.0;
        double calculatedTotal = Math.round((calculatedSubtotal + calculatedTax + shippingFee) * 100.0) / 100.0;

        final double finalShippingFee = shippingFee;
        final double finalCalculatedSubtotal = calculatedSubtotal;
        final double finalCalculatedTax = calculatedTax;
        final double finalCalculatedTotal = calculatedTotal;

        binding.subtotalTxt.setText(formatPrice(calculatedSubtotal));
        binding.deliveryTxt.setText(formatPrice(shippingFee));
        binding.taxTxt.setText(formatPrice(calculatedTax));
        binding.totalAmountTxt.setText(formatPrice(calculatedTotal));

        binding.checkoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.onlineshop.Activity.PaymentActivity.class);
            intent.putExtra("cart_items", selectedItems);
            intent.putExtra("subtotal", finalCalculatedSubtotal);
            intent.putExtra("tax", finalCalculatedTax);
            intent.putExtra("delivery", finalShippingFee);
            intent.putExtra("total", finalCalculatedTotal);
            startActivity(intent);
        });
    }

    private String formatPrice(double value) {
        String symbol = appSettings != null ? appSettings.getCurrencySymbol() : "$";
        return symbol + String.format(Locale.getDefault(), "%.2f", value);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isCartTab) {
            updateCartDisplay();
        } else {
            loadOrders();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
