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
                try {
                    AppSettingsModel settings = snapshot.getValue(AppSettingsModel.class);
                    if (settings != null) {
                        settingsData.setValue(settings);
                    } else {
                        settingsData.setValue(createDefaultSettings());
                    }
                } catch (Exception e) {
                    // Handle type conversion errors from Firebase
                    android.util.Log.e("MainRepository", "Error parsing AppSettings", e);

                    // Try to manually parse the data
                    try {
                        AppSettingsModel settings = new AppSettingsModel();

                        if (snapshot.hasChild("currency")) {
                            settings.setCurrency(snapshot.child("currency").getValue(String.class));
                        }
                        if (snapshot.hasChild("currencySymbol")) {
                            settings.setCurrencySymbol(snapshot.child("currencySymbol").getValue(String.class));
                        }
                        if (snapshot.hasChild("taxRate")) {
                            Object taxObj = snapshot.child("taxRate").getValue();
                            if (taxObj instanceof Number) {
                                settings.setTaxRate(((Number) taxObj).doubleValue());
                            }
                        }
                        if (snapshot.hasChild("shippingFee")) {
                            Object shippingObj = snapshot.child("shippingFee").getValue();
                            if (shippingObj instanceof Number) {
                                settings.setShippingFee(((Number) shippingObj).doubleValue());
                            }
                        }
                        if (snapshot.hasChild("freeShippingThreshold")) {
                            Object thresholdObj = snapshot.child("freeShippingThreshold").getValue();
                            if (thresholdObj instanceof Number) {
                                settings.setFreeShippingThreshold(((Number) thresholdObj).doubleValue());
                            }
                        }
                        if (snapshot.hasChild("maxCartItems")) {
                            Object maxObj = snapshot.child("maxCartItems").getValue();
                            if (maxObj instanceof Number) {
                                settings.setMaxCartItems(((Number) maxObj).intValue());
                            }
                        }
                        if (snapshot.hasChild("maintenanceMode")) {
                            settings.setMaintenanceMode(snapshot.child("maintenanceMode").getValue(Boolean.class));
                        }
                        if (snapshot.hasChild("minAppVersion")) {
                            settings.setMinAppVersion(snapshot.child("minAppVersion").getValue(String.class));
                        }
                        if (snapshot.hasChild("returnPolicyDays")) {
                            Object returnObj = snapshot.child("returnPolicyDays").getValue();
                            if (returnObj instanceof Number) {
                                settings.setReturnPolicyDays(((Number) returnObj).intValue());
                            }
                        }
                        if (snapshot.hasChild("supportEmail")) {
                            settings.setSupportEmail(snapshot.child("supportEmail").getValue(String.class));
                        }
                        if (snapshot.hasChild("supportPhone")) {
                            settings.setSupportPhone(snapshot.child("supportPhone").getValue(String.class));
                        }

                        settingsData.setValue(settings);
                    } catch (Exception e2) {
                        android.util.Log.e("MainRepository", "Manual parsing also failed", e2);
                        settingsData.setValue(createDefaultSettings());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                settingsData.setValue(createDefaultSettings());
            }
        });
        return settingsData;
    }

    private AppSettingsModel createDefaultSettings() {
        AppSettingsModel defaultSettings = new AppSettingsModel();
        defaultSettings.setCurrency("VND");
        defaultSettings.setCurrencySymbol("đ");
        defaultSettings.setTaxRate(0.1);
        defaultSettings.setShippingFee(10);
        defaultSettings.setFreeShippingThreshold(100);
        defaultSettings.setMaxCartItems(50);
        return defaultSettings;
    }


}
