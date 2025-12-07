package com.example.onlineshop.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.databinding.ViewholderOrderDetailProductBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderDetailProductAdapter extends RecyclerView.Adapter<OrderDetailProductAdapter.Viewholder> {
    private ArrayList<ItemsModel> items;

    public OrderDetailProductAdapter(ArrayList<ItemsModel> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderOrderDetailProductBinding binding = ViewholderOrderDetailProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        ItemsModel item = items.get(position);

        // Product image
        if (item.getPicUrl() != null && !item.getPicUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getPicUrl().get(0))
                    .into(holder.binding.productPic);
        }

        // Product title
        holder.binding.productTitleTxt.setText(item.getTitle());

        // Color
        if (item.getColor() != null && !item.getColor().isEmpty()) {
            holder.binding.productColorTxt.setText("Màu: " + item.getColor().get(0));
        } else {
            holder.binding.productColorTxt.setText("Màu: Không có");
        }

        // Size
        if (item.getSize() != null && !item.getSize().isEmpty()) {
            holder.binding.productSizeTxt.setText("Size: " + item.getSize().get(0));
        } else {
            holder.binding.productSizeTxt.setText("");
        }

        // Quantity
        holder.binding.quantityTxt.setText("x" + item.getNumberinCart());

        // Price
        double totalPrice = item.getPrice() * item.getNumberinCart();
        holder.binding.priceTxt.setText(formatPrice(totalPrice));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatPrice(double value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(value);
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        ViewholderOrderDetailProductBinding binding;

        public Viewholder(ViewholderOrderDetailProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
