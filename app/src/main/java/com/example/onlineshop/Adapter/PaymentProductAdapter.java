package com.example.onlineshop.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.RequestOptions;
import com.example.onlineshop.Model.ItemsModel;
import com.example.onlineshop.databinding.ViewholderPaymentProductBinding;

import java.util.ArrayList;

public class PaymentProductAdapter extends RecyclerView.Adapter<PaymentProductAdapter.ViewHolder> {

    private final ArrayList<ItemsModel> items;

    public PaymentProductAdapter(ArrayList<ItemsModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderPaymentProductBinding binding = ViewholderPaymentProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemsModel item = items.get(position);
        holder.binding.titleTxt.setText(item.getTitle());

        if (item.getColor() != null && !item.getColor().isEmpty()) {
            holder.binding.colorTxt.setText("Color: " + item.getColor().get(0));
        } else {
            holder.binding.colorTxt.setText("Color: -");
        }

        holder.binding.priceTxt.setText(item.getPrice() + "₫");

        if (item.getPicUrl() != null && !item.getPicUrl().isEmpty()) {
            RequestOptions options = new RequestOptions().transform(new CenterInside());
            Glide.with(holder.itemView.getContext())
                    .load(item.getPicUrl().get(0))
                    .apply(options)
                    .into(holder.binding.pic);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ViewholderPaymentProductBinding binding;

        public ViewHolder(ViewholderPaymentProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
