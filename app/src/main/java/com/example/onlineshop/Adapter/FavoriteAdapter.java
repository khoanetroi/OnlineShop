package com.example.onlineshop.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.RequestOptions;
import com.example.onlineshop.Activity.DetailActivity;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.databinding.ViewholderFavoriteBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private final ArrayList<ItemsModel> items;
    private final Context context;
    private final DatabaseReference wishlistRef;

    public FavoriteAdapter(ArrayList<ItemsModel> items, Context context) {
        this.items = items;
        this.context = context;

        UserPreferences userPreferences = new UserPreferences(context);
        String uid = userPreferences.getUserId();
        if (uid != null) {
            wishlistRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("wishlist");
        } else {
            wishlistRef = null;
        }
    }

    @NonNull
    @Override
    public FavoriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderFavoriteBinding binding = ViewholderFavoriteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteAdapter.ViewHolder holder, int position) {
        ItemsModel item = items.get(position);

        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.priceTxt.setText("$" + item.getPrice());

        RequestOptions options = new RequestOptions().transform(new CenterInside());
        if (item.getPicUrl() != null && !item.getPicUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getPicUrl().get(0))
                    .apply(options)
                    .into(holder.binding.pic);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("object", item);
                context.startActivity(intent);
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).overridePendingTransition(com.example.onlineshop.R.anim.slide_in_right, com.example.onlineshop.R.anim.slide_out_left);
                }
            }
        });

        holder.binding.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) return;

                if (wishlistRef == null) {
                    Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
                    return;
                }

                removeFromWishlist(item, adapterPosition);
            }
        });
    }

    private void removeFromWishlist(ItemsModel item, int position) {
        wishlistRef.orderByChild("title").equalTo(item.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    child.getRef().removeValue();
                }

                if (position >= 0 && position < items.size()) {
                    items.remove(position);
                    notifyItemRemoved(position);
                } else {
                    notifyDataSetChanged();
                }

                Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to update favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ViewholderFavoriteBinding binding;

        public ViewHolder(ViewholderFavoriteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
