package com.example.onlineshop.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.Helper.ChangeNumberItemsListener;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.databinding.ViewholderCartBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.Viewholder> {
    ArrayList<ItemsModel> listItemSelected;
    ChangeNumberItemsListener changeNumberItemsListener;
    private ManagmentCart managmentCart;
    private Set<Integer> selectedPositions = new HashSet<>();
    private OnItemSelectionChangedListener selectionListener;

    public interface OnItemSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
        void onItemChecked(ItemsModel item, int position);
    }

    public CartAdapter(ArrayList<ItemsModel> listItemSelected, Context context, ChangeNumberItemsListener changeNumberItemsListener) {
        this.listItemSelected = listItemSelected;
        this.changeNumberItemsListener = changeNumberItemsListener;
        managmentCart = new ManagmentCart(context);
    }

    public void setSelectionListener(OnItemSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    public ArrayList<ItemsModel> getSelectedItems() {
        ArrayList<ItemsModel> selected = new ArrayList<>();
        for (Integer position : selectedPositions) {
            if (position < listItemSelected.size()) {
                selected.add(listItemSelected.get(position));
            }
        }
        return selected;
    }



    @NonNull
    @Override
    public CartAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderCartBinding binding = ViewholderCartBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.Viewholder holder, int position) {
        ItemsModel item = listItemSelected.get(position);
        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.numberItemTxt.setText(String.valueOf(item.getNumberinCart()));
        
        // Set color information
        if (item.getColor() != null && !item.getColor().isEmpty()) {
            String colorText = "Color: " + item.getColor().get(0);
            holder.binding.colorTxt.setText(colorText);
        } else {
            holder.binding.colorTxt.setText("Color: N/A");
        }
        
        // Set price
        double totalPrice = item.getPrice() * item.getNumberinCart();
        holder.binding.priceTxt.setText("$" + String.format("%.2f", totalPrice));
        
        Glide.with(holder.itemView.getContext())
                .load(item.getPicUrl().get(0))
                .into(holder.binding.pic);

        // Set checkbox state without triggering listener
        holder.binding.checkBox.setOnCheckedChangeListener(null);
        holder.binding.checkBox.setChecked(selectedPositions.contains(position));

        // Checkbox click listener - show checkout modal when item is checked
        holder.binding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPositions.add(position);
                // Show checkout modal when item is checked
                if (selectionListener != null) {
                    selectionListener.onSelectionChanged(selectedPositions.size());
                    selectionListener.onItemChecked(item, position);
                }
            } else {
                selectedPositions.remove(position);
                if (selectionListener != null) {
                    selectionListener.onSelectionChanged(selectedPositions.size());
                }
            }
        });

        // Delete button click listener
        holder.binding.deleteBtn.setOnClickListener(v -> {
            managmentCart.removeItem(listItemSelected, position);
            selectedPositions.remove(position);
            // Adjust positions after removal
            Set<Integer> newSelected = new HashSet<>();
            for (Integer pos : selectedPositions) {
                if (pos > position) {
                    newSelected.add(pos - 1);
                } else if (pos < position) {
                    newSelected.add(pos);
                }
            }
            selectedPositions = newSelected;
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, listItemSelected.size());
            changeNumberItemsListener.changed();
            if (selectionListener != null) {
                selectionListener.onSelectionChanged(selectedPositions.size());
            }
        });

        holder.binding.plusCartBtn.setOnClickListener(v -> {
            managmentCart.plusItem(listItemSelected, position, new ChangeNumberItemsListener() {
                @Override
                public void changed() {
                    notifyDataSetChanged();
                    changeNumberItemsListener.changed();
                }
            });
        });
        
        holder.binding.minusCartBtn.setOnClickListener(v -> {
            managmentCart.minusItem(listItemSelected, position, new ChangeNumberItemsListener() {
                @Override
                public void changed() {
                    notifyDataSetChanged();
                    changeNumberItemsListener.changed();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return listItemSelected.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        ViewholderCartBinding binding;
        public Viewholder(ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
