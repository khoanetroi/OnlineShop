package com.example.onlineshop.Activity;

import android.graphics.Paint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.onlineshop.Adapter.ColorAdapter;
import com.example.onlineshop.Adapter.PicListAdapter;
import com.example.onlineshop.Adapter.SizeAdapter;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ActivityDetailBinding;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {
private ActivityDetailBinding binding;
private ItemsModel object;
private int numberOrder=1;
private ManagmentCart managmentCart;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart = new ManagmentCart(this);
        getBundle();
        initPicList();
        initSize();
        initColor();
    }

    private void initColor() {
        binding.recyclerColor.setAdapter(new ColorAdapter(object.getColor()));
        binding.recyclerColor.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true));
    }

    private void initSize() {
        binding.recyclerSize.setAdapter(new SizeAdapter(object.getSize()));
        binding.recyclerSize.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true));
    }

    private void initPicList() {
        ArrayList<String> picList=new ArrayList<>(object.getPicUrl());
        Glide.with(this)
                .load(picList.get(0))
                .into(binding.pic);
        binding.picList.setAdapter(new PicListAdapter(picList,binding.pic));
        binding.picList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
    }

    private void getBundle() {
        object = (ItemsModel) getIntent().getSerializableExtra("object");
        binding.titleTxt.setText(object.getTitle());
        binding.priceTxt.setText("$" + object.getPrice());
        binding.oldPriceTxt.setText("$" + object.getOldPrice());
        binding.oldPriceTxt.setPaintFlags(binding.oldPriceTxt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        binding.descriptionTxt.setText(object.getDescription());

        binding.addToCartBtn.setOnClickListener(v -> {
            object.setNumberinCart(numberOrder);
            managmentCart.insertItem(object);
        });
        binding.backBtn.setOnClickListener(v -> finish());
    }
}

