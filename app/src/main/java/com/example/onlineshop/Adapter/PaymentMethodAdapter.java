package com.example.onlineshop.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ViewholderPaymentMethodBinding;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder> {
    private List<PaymentMethod> paymentMethods;
    private int selectedPosition = -1;
    private OnPaymentMethodSelectedListener listener;

    public interface OnPaymentMethodSelectedListener {
        void onPaymentMethodSelected(PaymentMethod method);
    }

    public static class PaymentMethod {
        private String name;
        private int iconResId;

        public PaymentMethod(String name, int iconResId) {
            this.name = name;
            this.iconResId = iconResId;
        }

        public String getName() {
            return name;
        }

        public int getIconResId() {
            return iconResId;
        }
    }

    public PaymentMethodAdapter(List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods != null ? paymentMethods : new ArrayList<>();
    }

    public void setOnPaymentMethodSelectedListener(OnPaymentMethodSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderPaymentMethodBinding binding = ViewholderPaymentMethodBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentMethod method = paymentMethods.get(position);
        holder.binding.paymentNameTxt.setText(method.getName());
        
        // Set payment method icon image
        holder.binding.paymentIcon.setImageResource(method.getIconResId());
        
        // Show check icon if selected
        holder.binding.checkIcon.setVisibility(selectedPosition == position ? View.VISIBLE : View.GONE);
        
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onPaymentMethodSelected(method);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    public PaymentMethod getSelectedMethod() {
        if (selectedPosition >= 0 && selectedPosition < paymentMethods.size()) {
            return paymentMethods.get(selectedPosition);
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ViewholderPaymentMethodBinding binding;

        public ViewHolder(ViewholderPaymentMethodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

