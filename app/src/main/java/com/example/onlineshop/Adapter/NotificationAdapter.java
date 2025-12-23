package com.example.onlineshop.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshop.Model.NotificationModel;
import com.example.onlineshop.databinding.ViewholderNotificationBinding;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final ArrayList<NotificationModel> items;

    public NotificationAdapter(ArrayList<NotificationModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderNotificationBinding binding = ViewholderNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel item = items.get(position);
        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.messageTxt.setText(item.getMessage());
        holder.binding.timeTxt.setText(item.getTime());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ViewholderNotificationBinding binding;

        public ViewHolder(ViewholderNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
