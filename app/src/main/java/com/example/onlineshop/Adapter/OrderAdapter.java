package com.example.onlineshop.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.onlineshop.Domain.OrderModel;
import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ViewholderOrderBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.Viewholder> {
    private ArrayList<OrderModel> orders;
    private OnOrderActionListener actionListener;

    public interface OnOrderActionListener {
        void onDetailClick(OrderModel order);
        void onTrackingClick(OrderModel order);
        void onReceiveOrderClick(OrderModel order);
    }

    public OrderAdapter(ArrayList<OrderModel> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
    }

    public void setOrders(ArrayList<OrderModel> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setActionListener(OnOrderActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderOrderBinding binding = ViewholderOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        OrderModel order = orders.get(position);
        
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }

        com.example.onlineshop.Domain.ItemsModel firstItem = order.getItems().get(0);
        
        if (firstItem.getPicUrl() != null && !firstItem.getPicUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(firstItem.getPicUrl().get(0))
                    .into(holder.binding.productPic);
        }

        holder.binding.productTitleTxt.setText(firstItem.getTitle());

        if (firstItem.getColor() != null && !firstItem.getColor().isEmpty()) {
            String colorText = "Màu: " + firstItem.getColor().get(0);
            holder.binding.productColorTxt.setText(colorText);
        } else {
            holder.binding.productColorTxt.setText("Màu: Không có");
        }

        int totalQty = 0;
        for (com.example.onlineshop.Domain.ItemsModel item : order.getItems()) {
            totalQty += item.getNumberinCart();
        }
        holder.binding.productQtyTxt.setText("SL: " + totalQty);

        String priceText = formatPrice(order.getTotal());
        holder.binding.priceTxt.setText(priceText);

        String status = order.getStatus() != null ? order.getStatus() : "Chờ Xử Lý";
        holder.binding.statusBadge.setText(status);
        
        if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Delivered")) {
            holder.binding.statusBadge.setBackgroundResource(R.drawable.status_completed_bg);
            holder.binding.statusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
        } else {
            holder.binding.statusBadge.setBackgroundResource(R.drawable.status_on_progress_bg);
            holder.binding.statusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_blue_dark));
        }

        boolean isCompleted = status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Delivered") || status.equalsIgnoreCase("Hoàn Thành") || status.equalsIgnoreCase("Đã Giao");
        if (isCompleted) {
            holder.binding.actionBtn.setText("Đã Nhận Hàng");
            holder.binding.actionBtn.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onReceiveOrderClick(order);
                }
            });
        } else {
            holder.binding.actionBtn.setText("Theo Dõi");
            holder.binding.actionBtn.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onTrackingClick(order);
                }
            });
        }

        holder.binding.detailBtn.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDetailClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private String formatPrice(double value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(value);
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        ViewholderOrderBinding binding;

        public Viewholder(ViewholderOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

