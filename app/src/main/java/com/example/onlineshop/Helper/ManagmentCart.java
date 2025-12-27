package com.example.onlineshop.Helper;

import android.content.Context;
import android.widget.Toast;
import com.example.onlineshop.Model.ItemsModel;
import com.example.onlineshop.Respository.CartRepository;

import java.util.ArrayList;

public class ManagmentCart {

    private Context context;
    private TinyDB tinyDB;
    private CartRepository cartRepository;

    public ManagmentCart(Context context) {
        this.context = context;
        this.tinyDB = new TinyDB(context);
        this.cartRepository = new CartRepository();
    }

    public void insertItem(ItemsModel item) {
        ArrayList<ItemsModel> listItem = getListCart();
        boolean existAlready = false;
        int n = 0;
        for (int y = 0; y < listItem.size(); y++) {
            if (listItem.get(y).getTitle().equals(item.getTitle())) {
                existAlready = true;
                n = y;
                break;
            }
        }
        if (existAlready) {
            listItem.get(n).setNumberinCart(item.getNumberinCart());
        } else {
            listItem.add(item);
        }
        tinyDB.putListObject("CartList", listItem);
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show();
    }

    public ArrayList<ItemsModel> getListCart() {
        return tinyDB.getListObject("CartList");
    }

    public void minusItem(ArrayList<ItemsModel> listItem, int position, ChangeNumberItemsListener changeNumberItemsListener) {
        if (position < 0 || position >= listItem.size()) return;

        ItemsModel item = listItem.get(position);
        int currentQty = item.getNumberinCart();

        if (currentQty <= 1) {
            listItem.remove(position);
            tinyDB.putListObject("CartList", listItem);

            if (cartRepository != null && cartRepository.isUserLoggedIn()) {
                cartRepository.removeFromCart(item, null);
            }
        } else {
            int newQty = currentQty - 1;
            item.setNumberinCart(newQty);
            tinyDB.putListObject("CartList", listItem);

            if (cartRepository != null && cartRepository.isUserLoggedIn()) {
                cartRepository.updateQuantity(item, newQty, null);
            }
        }

        if (changeNumberItemsListener != null) {
            changeNumberItemsListener.changed();
        }
    }

    public void plusItem(ArrayList<ItemsModel> listItem, int position, ChangeNumberItemsListener changeNumberItemsListener) {
        if (position < 0 || position >= listItem.size()) return;

        ItemsModel item = listItem.get(position);
        int newQty = item.getNumberinCart() + 1;
        item.setNumberinCart(newQty);
        tinyDB.putListObject("CartList", listItem);

        if (cartRepository != null && cartRepository.isUserLoggedIn()) {
            cartRepository.updateQuantity(item, newQty, null);
        }

        if (changeNumberItemsListener != null) {
            changeNumberItemsListener.changed();
        }
    }

    public void removeItem(ArrayList<ItemsModel> listItem, int position) {
        if (position >= 0 && position < listItem.size()) {
            ItemsModel item = listItem.get(position);

            listItem.remove(position);
            tinyDB.putListObject("CartList", listItem);

            if (cartRepository != null && cartRepository.isUserLoggedIn()) {
                cartRepository.removeFromCart(item, null);
            }
        }
    }

    public Double getTotalFee() {
        ArrayList<ItemsModel> listItem2 = getListCart();
        double fee = 0;
        for (int i = 0; i < listItem2.size(); i++) {
            fee = fee + (listItem2.get(i).getPrice() * listItem2.get(i).getNumberinCart());
        }
        return fee;
    }

    public void clearCart() {
        ArrayList<ItemsModel> emptyList = new ArrayList<>();
        tinyDB.putListObject("CartList", emptyList);
    }

    public void setUserId(String userId) {
        if (cartRepository != null) {
            cartRepository.setUserId(userId);
        }
    }

    public void syncLocalCartToFirebase(CartRepository.OnCartOperationListener listener) {
        ArrayList<ItemsModel> localCart = getListCart();
        if (cartRepository != null) {
            cartRepository.syncLocalCartToFirebase(localCart, new CartRepository.OnCartOperationListener() {
                @Override
                public void onSuccess() {
                    clearCart();
                    if (listener != null) {
                        listener.onSuccess();
                    }
                }

                @Override
                public void onFailure(String error) {
                    if (listener != null) {
                        listener.onFailure(error);
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onFailure("Cart repository not initialized");
            }
        }
    }

    public CartRepository getCartRepository() {
        return cartRepository;
    }
}
