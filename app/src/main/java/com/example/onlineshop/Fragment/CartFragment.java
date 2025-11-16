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
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.Helper.ChangeNumberItemsListener;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ActivityCartBinding;

import java.util.ArrayList;
import java.util.Locale;

public class CartFragment extends Fragment {
    private ActivityCartBinding binding;
    private ManagmentCart managmentCart;
    private CartAdapter cartAdapter;

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
        setVariable();
        initCartList();
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
            }
        });
        cartAdapter.setSelectionListener(new CartAdapter.OnItemSelectionChangedListener() {
            @Override
            public void onSelectionChanged(int selectedCount) {
                if (selectedCount > 0) {
                    // Show or update the checkout modal
                    updateCheckoutModal();
                    binding.checkoutModalContainer.setVisibility(View.VISIBLE);
                } else {
                    // Auto-hide when all items are unchecked
                    binding.checkoutModalContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onItemChecked(ItemsModel item, int position) {
                // Show or update the checkout modal when an item is checked
                updateCheckoutModal();
                binding.checkoutModalContainer.setVisibility(View.VISIBLE);
            }
        });
        binding.cartView.setAdapter(cartAdapter);
    }

    private void setVariable() {
        binding.notificationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.onlineshop.Activity.NotificationActivity.class);
            startActivity(intent);
        });
    }

    private void updateCheckoutModal() {
        ArrayList<ItemsModel> selectedItems = cartAdapter.getSelectedItems();
        
        // Hide modal if there are no selected items
        if (selectedItems.isEmpty()) {
            binding.checkoutModalContainer.setVisibility(View.GONE);
            return;
        }
        
        // Calculate prices
        double rawSubtotal = 0;
        for (ItemsModel item : selectedItems) {
            rawSubtotal += item.getPrice() * item.getNumberinCart();
        }

        double percentTax = 0.02;
        double delivery = 10.0;
        double calculatedTax = Math.round((rawSubtotal * percentTax) * 100.0) / 100.0;
        double calculatedSubtotal = Math.round(rawSubtotal * 100.0) / 100.0;
        double calculatedTotal = Math.round((calculatedSubtotal + calculatedTax + delivery) * 100.0) / 100.0;

        // Update UI
        binding.subtotalTxt.setText(formatPrice(calculatedSubtotal));
        binding.deliveryTxt.setText(formatPrice(delivery));
        binding.taxTxt.setText(formatPrice(calculatedTax));
        binding.totalAmountTxt.setText(formatPrice(calculatedTotal));

        // Set checkout button listener
        binding.checkoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.onlineshop.Activity.PaymentActivity.class);
            intent.putExtra("cart_items", selectedItems);
            intent.putExtra("subtotal", calculatedSubtotal);
            intent.putExtra("tax", calculatedTax);
            intent.putExtra("delivery", delivery);
            intent.putExtra("total", calculatedTotal);
            startActivity(intent);
        });
    }

    private String formatPrice(double value) {
        return "$" + String.format(Locale.US, "%.2f", value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

