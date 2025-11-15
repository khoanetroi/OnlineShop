package com.example.onlineshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.NotificationAdapter;
import com.example.onlineshop.Domain.NotificationModel;
import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.databinding.ActivityNotificationBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        loadNotifications();
    }

    private void loadNotifications() {
        UserPreferences userPreferences = new UserPreferences(this);
        String uid = userPreferences.getUserId();
        if (uid == null) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notifications");
        binding.progressBar.setVisibility(View.VISIBLE);

        ref.orderByChild("userId").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                notifications.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    NotificationModel model = child.getValue(NotificationModel.class);
                    if (model != null) {
                        model.setId(child.getKey());
                        notifications.add(model);
                    }
                }
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
                binding.emptyTxt.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }
}
