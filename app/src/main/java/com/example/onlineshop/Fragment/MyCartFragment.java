package com.example.onlineshop.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Activity.PaymentActivity;
import com.example.onlineshop.Adapter.CartAdapter;
import com.example.onlineshop.Model.ItemsModel;
import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.databinding.FragmentMyCartBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyCartFragment extends Fragment {
    private FragmentMyCartBinding binding;
    private ArrayList<ItemsModel> cartItems = new ArrayList<>();
    private CartAdapter adapter;
    private UserPreferences userPreferences;
    private DatabaseReference cartRef;
    private ValueEventListener cartListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        userPreferences = new UserPreferences(requireContext());
        
        setupRecyclerView();
        loadCartItems();
        setupButtons();
    }

    private void setupRecyclerView() {
        binding.cartView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartAdapter(cartItems, requireContext(), this::updateTotals);

        String userId = null;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
        } else if (userPreferences != null) {
            userId = userPreferences.getUserId();
        }
        if (userId != null && !userId.isEmpty()) {
            adapter.setUserId(userId);
        }
        adapter.setSelectionListener(new CartAdapter.OnItemSelectionChangedListener() {
            @Override
            public void onSelectionChanged(int selectedCount) {
                updateTotals();
            }

            @Override
            public void onItemChecked(ItemsModel item, int position) {
                updateTotals();
            }
        });
        binding.cartView.setAdapter(adapter);
    }

    private void loadCartItems() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyTxt.setVisibility(View.GONE);
        
        cartRef = getCartRef();
        if (cartRef == null) {
            binding.progressBar.setVisibility(View.GONE);
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.emptyTxt.setText("Vui lòng đăng nhập để xem giỏ hàng");
            return;
        }

        cartListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        ItemsModel item = new ItemsModel();
                        item.setTitle(child.child("title").getValue(String.class));
                        item.setPrice(child.child("price").getValue(Double.class));

                        ArrayList<String> picUrl = new ArrayList<>();
                        for (DataSnapshot pic : child.child("picUrl").getChildren()) {
                            String url = pic.getValue(String.class);
                            if (url != null) {
                                picUrl.add(url);
                            }
                        }
                        item.setPicUrl(picUrl);

                        ArrayList<String> colors = new ArrayList<>();
                        for (DataSnapshot colorSnap : child.child("color").getChildren()) {
                            String c = colorSnap.getValue(String.class);
                            if (c != null && !c.isEmpty()) {
                                colors.add(c);
                            }
                        }
                        if (colors.isEmpty()) {
                            String legacyColor = child.child("selectedColor").getValue(String.class);
                            if (legacyColor != null && !legacyColor.isEmpty()) {
                                colors.add(legacyColor);
                            }
                        }
                        item.setColor(colors);

                        ArrayList<String> sizes = new ArrayList<>();
                        for (DataSnapshot sizeSnap : child.child("size").getChildren()) {
                            String s = sizeSnap.getValue(String.class);
                            if (s != null && !s.isEmpty()) {
                                sizes.add(s);
                            }
                        }
                        if (sizes.isEmpty()) {
                            String legacySize = child.child("selectedSize").getValue(String.class);
                            if (legacySize != null && !legacySize.isEmpty()) {
                                sizes.add(legacySize);
                            }
                        }
                        item.setSize(sizes);

                        Long qty = child.child("quantity").getValue(Long.class);
                        item.setNumberinCart(qty != null ? qty.intValue() : 1);

                        cartItems.add(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                binding.progressBar.setVisibility(View.GONE);
                
                if (cartItems.isEmpty()) {
                    binding.emptyTxt.setVisibility(View.VISIBLE);
                    binding.cartView.setVisibility(View.GONE);
                    binding.checkoutBtn.setEnabled(false);
                } else {
                    binding.emptyTxt.setVisibility(View.GONE);
                    binding.cartView.setVisibility(View.VISIBLE);
                    binding.checkoutBtn.setEnabled(true);
                    adapter.notifyDataSetChanged();
                    updateTotals();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Lỗi khi tải giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        };
        
        cartRef.addValueEventListener(cartListener);
    }

    private void updateTotals() {
        double subtotal = 0;
        
        if (adapter != null) {
            ArrayList<ItemsModel> selectedItems = adapter.getSelectedItems();
            for (ItemsModel item : selectedItems) {
                subtotal += item.getPrice() * item.getNumberinCart();
            }
        }
        
        double tax = subtotal * 0.1;
        double delivery = subtotal > 100 ? 0 : 10;
        double total = subtotal + tax + delivery;
        
        binding.subtotalTxt.setText(String.format("%.2f₫", subtotal));
        binding.taxTxt.setText(String.format("%.2f₫", tax));
        binding.deliveryTxt.setText(String.format("%.2f₫", delivery));
        binding.totalTxt.setText(String.format("%.2f₫", total));
    }

    private void setupButtons() {
        binding.backBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.checkoutBtn.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            
            ArrayList<ItemsModel> selectedItems = adapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Intent intent = new Intent(requireContext(), PaymentActivity.class);
            intent.putExtra("selectedItems", selectedItems);
            intent.putExtra("subtotal", binding.subtotalTxt.getText().toString());
            intent.putExtra("tax", binding.taxTxt.getText().toString());
            intent.putExtra("delivery", binding.deliveryTxt.getText().toString());
            intent.putExtra("total", binding.totalTxt.getText().toString());
            startActivity(intent);
        });
    }

    private DatabaseReference getCartRef() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            return FirebaseDatabase.getInstance().getReference("Users")
                    .child(firebaseUser.getUid()).child("cart");
        }
        
        String uid = userPreferences.getUserId();
        if (uid != null && !uid.isEmpty()) {
            return FirebaseDatabase.getInstance().getReference("Users")
                    .child(uid).child("cart");
        }
        
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cartRef != null && cartListener != null) {
            cartRef.removeEventListener(cartListener);
        }
        binding = null;
    }
}
