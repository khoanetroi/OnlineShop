package com.example.onlineshop.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.OrderAdapter;
import com.example.onlineshop.Domain.OrderModel;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.R;
import com.example.onlineshop.Respository.OrderRepository;
import com.example.onlineshop.databinding.ActivityMyOrderBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyOrderFragment extends Fragment {
    private ActivityMyOrderBinding binding;
    private OrderRepository orderRepository;
    private OrderAdapter orderAdapter;
    private UserPreferences userPreferences;
    private boolean isHistoryTab = false;
    private ArrayList<OrderModel> allOrders = new ArrayList<>();
    private ArrayList<OrderModel> inProgressOrders = new ArrayList<>();
    private ArrayList<OrderModel> completedOrders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityMyOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        userPreferences = new UserPreferences(requireContext());
        orderRepository = new OrderRepository();
        
        setupListeners();
        setupRecyclerView();
        setupTabs();
        loadOrders();
        updateCartBadge();
    }

    private void setupListeners() {
        binding.cartBtn.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.onlineshop.Activity.MainContainerActivity) {
                com.example.onlineshop.Activity.MainContainerActivity activity = 
                    (com.example.onlineshop.Activity.MainContainerActivity) getActivity();
                activity.navigateToMyCart();
            }
        });

        binding.bagIcon.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(requireContext(), com.example.onlineshop.Activity.NotificationActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        binding.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        orderAdapter = new OrderAdapter(new ArrayList<>());
        orderAdapter.setActionListener(new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onDetailClick(OrderModel order) {
                android.content.Intent intent = new android.content.Intent(requireContext(), 
                    com.example.onlineshop.Activity.OrderDetailActivity.class);
                intent.putExtra("order", order);
                startActivity(intent);
            }

            @Override
            public void onTrackingClick(OrderModel order) {
                android.widget.Toast.makeText(requireContext(), 
                    "Tính năng theo dõi đang được phát triển", 
                    android.widget.Toast.LENGTH_SHORT).show();
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
        binding.myOrderTab.setOnClickListener(v -> switchToMyOrderTab());
        binding.historyTab.setOnClickListener(v -> switchToHistoryTab());
        
        switchToMyOrderTab();
    }

    private void switchToMyOrderTab() {
        isHistoryTab = false;
        binding.myOrderTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        binding.myOrderTab.setTypeface(null, android.graphics.Typeface.BOLD);
        binding.historyTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey));
        binding.historyTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        binding.tabIndicator.post(() -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.tabIndicator.getLayoutParams();
            params.width = binding.myOrderTab.getWidth();
            binding.tabIndicator.setLayoutParams(params);
            binding.tabIndicator.animate()
                    .translationX(0)
                    .setDuration(200)
                    .start();
        });
        
        updateOrdersDisplay();
    }

    private void switchToHistoryTab() {
        isHistoryTab = true;
        binding.historyTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        binding.historyTab.setTypeface(null, android.graphics.Typeface.BOLD);
        binding.myOrderTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey));
        binding.myOrderTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        binding.tabIndicator.post(() -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.tabIndicator.getLayoutParams();
            params.width = binding.historyTab.getWidth();
            binding.tabIndicator.setLayoutParams(params);
            float translationX = binding.historyTab.getLeft() - binding.myOrderTab.getLeft();
            binding.tabIndicator.animate()
                    .translationX(translationX)
                    .setDuration(200)
                    .start();
        });
        
        updateOrdersDisplay();
    }

    private void loadOrders() {
        orderRepository.loadInProgressOrders().observe(getViewLifecycleOwner(), orders -> {
            inProgressOrders = orders != null ? orders : new ArrayList<>();
            updateOrdersDisplay();
        });

        orderRepository.loadCompletedOrders().observe(getViewLifecycleOwner(), orders -> {
            completedOrders = orders != null ? orders : new ArrayList<>();
            updateOrdersDisplay();
        });
    }

    private void updateCartBadge() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId != null) {
            DatabaseReference cartRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId)
                    .child("cart");

            cartRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int itemCount = (int) snapshot.getChildrenCount();
                    updateBadgeUI(itemCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    updateBadgeFromLocalCart();
                }
            });
        } else {
            updateBadgeFromLocalCart();
        }
    }

    private void updateBadgeFromLocalCart() {
        ManagmentCart managmentCart = new ManagmentCart(requireContext());
        int itemCount = managmentCart.getListCart().size();
        updateBadgeUI(itemCount);
    }

    private void updateBadgeUI(int count) {
        if (binding != null) {
            if (count > 0) {
                binding.cartBadge.setVisibility(View.VISIBLE);
                binding.cartBadge.setText(String.valueOf(count > 99 ? "99+" : count));
            } else {
                binding.cartBadge.setVisibility(View.GONE);
            }
        }
    }

    private void updateOrdersDisplay() {
        ArrayList<OrderModel> ordersToShow = isHistoryTab ? completedOrders : inProgressOrders;
        
        if (ordersToShow.isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.ordersRecyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.ordersRecyclerView.setVisibility(View.VISIBLE);
            orderAdapter.setOrders(ordersToShow);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCartBadge();
    }
}

