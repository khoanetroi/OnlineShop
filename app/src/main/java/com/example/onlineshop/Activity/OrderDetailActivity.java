package com.example.onlineshop.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.OrderDetailProductAdapter;
import com.example.onlineshop.Model.ItemsModel;
import com.example.onlineshop.Model.OrderModel;
import com.example.onlineshop.databinding.ActivityOrderDetailBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {
    private ActivityOrderDetailBinding binding;
    private OrderModel order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getOrderData();
        setupViews();
        setupListeners();
    }

    private void getOrderData() {
        order = (OrderModel) getIntent().getSerializableExtra("order");
        if (order == null) {
            finish();
        }
    }

    private void setupViews() {
        if (order == null) return;

        // Order ID
        binding.orderIdTxt.setText("Đơn Hàng #" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())));

        // Order Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateStr = sdf.format(new Date(order.getOrderDate()));
        binding.orderDateTxt.setText(dateStr);

        // Status
        String status = order.getStatus() != null ? order.getStatus() : "Chờ Xử Lý";
        binding.statusTxt.setText(status);
        
        if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Delivered") || 
            status.equalsIgnoreCase("Hoàn Thành") || status.equalsIgnoreCase("Đã Giao")) {
            binding.statusTxt.setBackgroundResource(com.example.onlineshop.R.drawable.status_completed_bg);
            binding.statusTxt.setTextColor(getResources().getColor(com.example.onlineshop.R.color.green));
        } else {
            binding.statusTxt.setBackgroundResource(com.example.onlineshop.R.drawable.status_on_progress_bg);
            binding.statusTxt.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        }

        // Products
        ArrayList<ItemsModel> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            OrderDetailProductAdapter adapter = new OrderDetailProductAdapter(items);
            binding.productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.productsRecyclerView.setAdapter(adapter);
            
            binding.productCountTxt.setText(items.size() + " Sản Phẩm");
        }

        // Price breakdown
        binding.subtotalTxt.setText(formatPrice(order.getSubtotal()));
        binding.taxTxt.setText(formatPrice(order.getTax()) + "đ");
        binding.deliveryTxt.setText(formatPrice(order.getDelivery()) + "đ");
        binding.totalTxt.setText(formatPrice(order.getTotal()) + "đ");
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private String formatPrice(double value) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(value);
    }
}
