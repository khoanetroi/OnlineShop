package com.example.onlineshop.Respository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlineshop.Model.AppSettingsModel;
import com.example.onlineshop.Model.BannerModel;
import com.example.onlineshop.Model.CategoryModel;
import com.example.onlineshop.Model.ItemsModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainRepository {
    private final FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    public LiveData<ArrayList<CategoryModel>> loadCategory() {
        MutableLiveData<ArrayList<CategoryModel>> listData=new MutableLiveData<>();
        DatabaseReference ref=firebaseDatabase.getReference("Category");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<CategoryModel> list=new ArrayList<>();
                for (DataSnapshot childSnapshot:snapshot.getChildren()){
                    CategoryModel item=childSnapshot.getValue(CategoryModel.class);
                    if (item!=null) list.add(item);
                }
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return listData;
    }

    public LiveData<ArrayList<BannerModel>> loadBanner() {
        MutableLiveData<ArrayList<BannerModel>> listData=new MutableLiveData<>();
        DatabaseReference ref=firebaseDatabase.getReference("Banner");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<BannerModel> list=new ArrayList<>();
                for (DataSnapshot childSnapshot:snapshot.getChildren()){
                    BannerModel item=childSnapshot.getValue(BannerModel.class);
                    if (item!=null) list.add(item);
                }
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return listData;
    }

    public LiveData<ArrayList<ItemsModel>> loadPopular() {
        MutableLiveData<ArrayList<ItemsModel>> listData=new MutableLiveData<>();
        DatabaseReference ref=firebaseDatabase.getReference("Items");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<ItemsModel> list=new ArrayList<>();
                for (DataSnapshot childSnapshot:snapshot.getChildren()){
                    ItemsModel item=childSnapshot.getValue(ItemsModel.class);
                    if (item!=null) list.add(item);
                }
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return listData;
    }

    public LiveData<ArrayList<ItemsModel>> loadNewArrivals() {
        MutableLiveData<ArrayList<ItemsModel>> listData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("Items");
        ref.limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<ItemsModel> list = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ItemsModel item = childSnapshot.getValue(ItemsModel.class);
                    if (item != null) list.add(0, item);
                }
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return listData;
    }

    public LiveData<ArrayList<ItemsModel>> loadRecommended() {
        MutableLiveData<ArrayList<ItemsModel>> listData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("Items");
        ref.orderByChild("rating").startAt(4.0).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<ItemsModel> list = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ItemsModel item = childSnapshot.getValue(ItemsModel.class);
                    if (item != null) list.add(item);
                }
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return listData;
    }

    public LiveData<AppSettingsModel> loadAppSettings() {
        MutableLiveData<AppSettingsModel> settingsData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("AppSettings");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AppSettingsModel settings = snapshot.getValue(AppSettingsModel.class);
                settingsData.setValue(settings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                AppSettingsModel defaultSettings = new AppSettingsModel();
                defaultSettings.setCurrency("USD");
                defaultSettings.setCurrencySymbol("$");
                defaultSettings.setTaxRate(0.1);
                defaultSettings.setShippingFee(10);
                defaultSettings.setFreeShippingThreshold(100);
                defaultSettings.setMaxCartItems(50);
                settingsData.setValue(defaultSettings);
            }
        });
        return settingsData;
    }


}
