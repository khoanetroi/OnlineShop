package com.example.onlineshop.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.onlineshop.Model.ItemsModel;
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

    // Allow fragment/activity to pass the logged-in user id so Firebase cart is updated too
    public void setUserId(String userId) {
        if (managmentCart != null) {
            managmentCart.setUserId(userId);
        }
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
        
        if (item.getColor() != null && !item.getColor().isEmpty()) {
            String colorText = "Màu: " + item.getColor().get(0);
            holder.binding.colorTxt.setText(colorText);
        } else {
            holder.binding.colorTxt.setText("Màu: Không có");
        }
        
        double totalPrice = item.getPrice() * item.getNumberinCart();
        holder.binding.priceTxt.setText(String.format("%.0f", totalPrice) + "đ");
        
        Glide.with(holder.itemView.getContext())
                .load(item.getPicUrl().get(0))
                .into(holder.binding.pic);

        holder.binding.checkBox.setOnCheckedChangeListener(null);
        holder.binding.checkBox.setChecked(selectedPositions.contains(position));

        holder.binding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPositions.add(position);
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

        holder.binding.deleteBtn.setOnClickListener(v -> {
            managmentCart.removeItem(listItemSelected, position);
            selectedPositions.remove(position);
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
