package com.example.onlineshop.Respository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlineshop.Domain.OrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OrderRepository {
    private final FirebaseDatabase firebaseDatabase;

    public OrderRepository() {
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        try {
            this.firebaseDatabase.setPersistenceEnabled(true);
        } catch (Exception e) {
        }
    }

    public LiveData<ArrayList<OrderModel>> loadOrders() {
        MutableLiveData<ArrayList<OrderModel>> listData = new MutableLiveData<>();
        
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            listData.setValue(new ArrayList<>());
            return listData;
        }

        String uid = firebaseUser.getUid();
        DatabaseReference ref = firebaseDatabase.getReference("Orders").child(uid);
        
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<OrderModel> list = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    OrderModel order = childSnapshot.getValue(OrderModel.class);
                    if (order != null) {
                        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                            order.setOrderId(childSnapshot.getKey());
                        }
                        if (order.getOrderDate() != 0 && order.getCreatedAt() == 0) {
                            order.setCreatedAt(order.getOrderDate());
                        }
                        if (order.getCreatedAt() != 0 && order.getOrderDate() == 0) {
                            order.setOrderDate(order.getCreatedAt());
                        }
                        list.add(order);
                    }
                }
                
                Collections.sort(list, (o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt()));
                
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listData.setValue(new ArrayList<>());
            }
        });
        
        return listData;
    }

    public LiveData<ArrayList<OrderModel>> loadInProgressOrders() {
        MutableLiveData<ArrayList<OrderModel>> listData = new MutableLiveData<>();
        
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            listData.setValue(new ArrayList<>());
            return listData;
        }

        String uid = firebaseUser.getUid();
        DatabaseReference ref = firebaseDatabase.getReference("Orders").child(uid);
        
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<OrderModel> list = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    OrderModel order = childSnapshot.getValue(OrderModel.class);
                    if (order != null) {
                        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                            order.setOrderId(childSnapshot.getKey());
                        }
                        String status = order.getStatus() != null ? order.getStatus() : "";
                        if (!status.equalsIgnoreCase("Completed") && !status.equalsIgnoreCase("Delivered")) {
                            list.add(order);
                        }
                    }
                }
                
                Collections.sort(list, (o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt()));
                
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listData.setValue(new ArrayList<>());
            }
        });
        
        return listData;
    }

    public LiveData<ArrayList<OrderModel>> loadCompletedOrders() {
        MutableLiveData<ArrayList<OrderModel>> listData = new MutableLiveData<>();
        
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            listData.setValue(new ArrayList<>());
            return listData;
        }

        String uid = firebaseUser.getUid();
        DatabaseReference ref = firebaseDatabase.getReference("Orders").child(uid);
        
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<OrderModel> list = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    OrderModel order = childSnapshot.getValue(OrderModel.class);
                    if (order != null) {
                        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                            order.setOrderId(childSnapshot.getKey());
                        }
                        String status = order.getStatus() != null ? order.getStatus() : "";
                        if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Delivered")) {
                            list.add(order);
                        }
                    }
                }
                
                Collections.sort(list, (o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt()));
                
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listData.setValue(new ArrayList<>());
            }
        });
        
        return listData;
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return;

        String uid = firebaseUser.getUid();
        DatabaseReference ref = firebaseDatabase.getReference("Orders")
                .child(uid)
                .child(orderId)
                .child("status");
        ref.setValue(newStatus);
    }
}

