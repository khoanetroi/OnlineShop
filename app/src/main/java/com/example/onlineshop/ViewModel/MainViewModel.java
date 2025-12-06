package com.example.onlineshop.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.onlineshop.Domain.BannerModel;
import com.example.onlineshop.Domain.CategoryModel;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.Respository.MainRepository;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {
    private final MainRepository repository = new MainRepository();

    public LiveData<ArrayList<CategoryModel>> loadCategory() {
        return repository.loadCategory();
    }

    public  LiveData<ArrayList<BannerModel>> loadBanner() {
        return repository.loadBanner();
    }

    public  LiveData<ArrayList<ItemsModel>> loadPopular() {
        return repository.loadPopular();
    }

    public LiveData<ArrayList<ItemsModel>> loadNewArrivals() {
        return repository.loadNewArrivals();
    }

    public LiveData<ArrayList<ItemsModel>> loadRecommended() {
        return repository.loadRecommended();
    }
}
