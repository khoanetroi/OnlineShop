package com.example.onlineshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.NotificationAdapter;
import com.example.onlineshop.Model.NotificationModel;
import com.example.onlineshop.databinding.ActivityNotificationBinding;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationBinding binding;
    private final ArrayList<NotificationModel> notifications = new ArrayList<>();
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.notificationView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notifications);
        binding.notificationView.setAdapter(adapter);

        binding.backBtn.setOnClickListener(v -> finish());
        binding.settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationActivity.this, MainContainerActivity.class);
            intent.putExtra("select_profile", true);
            startActivity(intent);
            finish();
        });

        binding.progressBar.setVisibility(View.GONE);
        binding.emptyTxt.setVisibility(View.VISIBLE);
    }
}
