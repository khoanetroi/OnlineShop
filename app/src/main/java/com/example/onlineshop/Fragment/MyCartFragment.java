package com.example.onlineshop.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.CartAdapter;
import com.example.onlineshop.Domain.AppSettingsModel;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.Helper.ChangeNumberItemsListener;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.R;
import com.example.onlineshop.Respository.MainRepository;
import com.example.onlineshop.databinding.ActivityCartBinding;

import java.util.ArrayList;
import java.util.Locale;

public class MyCartFragment extends Fragment {
    private ActivityCartBinding binding;
    private ManagmentCart managmentCart;
    private CartAdapter cartAdapter;
    private MainRepository mainRepository;
    private AppSettingsModel appSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        managmentCart = new ManagmentCart(requireContext());
        mainRepository = new MainRepository();
        loadAppSettings();
        setVariable();
        initCartList();
    }

    private void loadAppSettings() {
        mainRepository.loadAppSettings().observe(getViewLifecycleOwner(), settings -> {
            if (settings != null) {
                appSettings = settings;
                // Update checkout modal if cart already has items
                if (cartAdapter != null && !cartAdapter.getSelectedItems().isEmpty()) {
                    updateCheckoutModal();
                }
            } else {
                // Use default settings if failed to load
                appSettings = new AppSettingsModel();
                appSettings.setCurrency("USD");
                appSettings.setCurrencySymbol("$");
                appSettings.setTaxRate(0.1);
                appSettings.setShippingFee(10);
                appSettings.setFreeShippingThreshold(100);
            }
        });
    }

    private void initCartList() {
        if(managmentCart.getListCart().isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollView2.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollView2.setVisibility(View.VISIBLE);
        }

        binding.cartView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        cartAdapter = new CartAdapter(managmentCart.getListCart(), requireContext(), new ChangeNumberItemsListener() {
            @Override
            public void changed() {
                updateCheckoutModal();
                // Refresh cart list if needed
                if(managmentCart.getListCart().isEmpty()) {
                    binding.emptyTxt.setVisibility(View.VISIBLE);
                    binding.scrollView2.setVisibility(View.GONE);
                    binding.checkoutModalContainer.setVisibility(View.GONE);
                }
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
        
        // Update checkout modal on first load if items exist
        if (!managmentCart.getListCart().isEmpty()) {
            updateCheckoutModal();
        }
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> {
            if (requireActivity() instanceof com.example.onlineshop.Activity.MainContainerActivity) {
                requireActivity().onBackPressed();
            }
        });
        
        binding.notificationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.onlineshop.Activity.NotificationActivity.class);
            startActivity(intent);
        });
    }

    private void updateCheckoutModal() {
        ArrayList<ItemsModel> selectedItems = cartAdapter.getSelectedItems();
        
        if (selectedItems.isEmpty()) {
            binding.checkoutModalContainer.setVisibility(View.GONE);
            return;
        }

        // Wait for AppSettings to load
        if (appSettings == null) {
            return;
        }
        
        // Calculate prices using Firebase AppSettings
        double rawSubtotal = 0;
        for (ItemsModel item : selectedItems) {
            rawSubtotal += item.getPrice() * item.getNumberinCart();
        }

        // Get settings from Firebase AppSettings
        double taxRate = appSettings.getTaxRate();
        double shippingFee = appSettings.getShippingFee();
        
        // Check for free shipping threshold
        if (rawSubtotal >= appSettings.getFreeShippingThreshold()) {
            shippingFee = 0;
        }
        
        double calculatedTax = Math.round((rawSubtotal * taxRate) * 100.0) / 100.0;
        double calculatedSubtotal = Math.round(rawSubtotal * 100.0) / 100.0;
        double calculatedTotal = Math.round((calculatedSubtotal + calculatedTax + shippingFee) * 100.0) / 100.0;

        // Make final copies for use in lambda
        final double finalShippingFee = shippingFee;
        final double finalCalculatedSubtotal = calculatedSubtotal;
        final double finalCalculatedTax = calculatedTax;
        final double finalCalculatedTotal = calculatedTotal;

        // Update UI with currency symbol from Firebase
        binding.subtotalTxt.setText(formatPrice(calculatedSubtotal));
        binding.deliveryTxt.setText(formatPrice(shippingFee));
        binding.taxTxt.setText(formatPrice(calculatedTax));
        binding.totalAmountTxt.setText(formatPrice(calculatedTotal));

        // Set checkout button listener
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
        // Format with currency symbol from AppSettings
        String symbol = appSettings != null ? appSettings.getCurrencySymbol() : "$";
        return symbol + String.format(Locale.getDefault(), "%.2f", value);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh cart when fragment resumes (e.g., after checkout)
        initCartList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

