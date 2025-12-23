package com.example.onlineshop.Respository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlineshop.Model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseDatabase firebaseDatabase;

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        try {
            this.firebaseDatabase.setPersistenceEnabled(true);
        } catch (Exception e) {
        }
    }

    public LiveData<AuthResult> login(String email, String password) {
        MutableLiveData<AuthResult> result = new MutableLiveData<>();
        
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            loadUserData(user.getUid(), result);
                        } else {
                            result.setValue(new AuthResult(false, "Failed to get user information", null));
                        }
                    } else {
                        String errorMessage = "Login failed";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        result.setValue(new AuthResult(false, errorMessage, null));
                    }
                })
                .addOnFailureListener(e -> {
                    result.setValue(new AuthResult(false, e.getMessage(), null));
                });
        
        return result;
    }

    public LiveData<AuthResult> register(String email, String password, String name) {
        MutableLiveData<AuthResult> result = new MutableLiveData<>();
        
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            UserModel user = new UserModel(firebaseUser.getUid(), email, name);
                            saveUserToDatabase(user, result);
                        } else {
                            result.setValue(new AuthResult(false, "Failed to get user information", null));
                        }
                    } else {
                        String errorMessage = "Registration failed";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        result.setValue(new AuthResult(false, errorMessage, null));
                    }
                })
                .addOnFailureListener(e -> {
                    result.setValue(new AuthResult(false, e.getMessage(), null));
                });
        
        return result;
    }

    private void saveUserToDatabase(UserModel user, MutableLiveData<AuthResult> result) {
        DatabaseReference ref = firebaseDatabase.getReference("Users").child(user.getUid());
        ref.setValue(user)
                .addOnSuccessListener(aVoid -> 
                    result.setValue(new AuthResult(true, "Registration successful", user)))
                .addOnFailureListener(e -> 
                    result.setValue(new AuthResult(false, e.getMessage(), null)));
    }

    private void loadUserData(String uid, MutableLiveData<AuthResult> result) {
        DatabaseReference ref = firebaseDatabase.getReference("Users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                if (user != null) {
                    result.setValue(new AuthResult(true, "Login successful", user));
                } else {
                    result.setValue(new AuthResult(false, "User data not found", null));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.setValue(new AuthResult(false, error.getMessage(), null));
            }
        });
    }

    public LiveData<UserModel> getUserProfile(String uid) {
        MutableLiveData<UserModel> userData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("Users").child(uid);
        
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                userData.setValue(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userData.setValue(null);
            }
        });
        
        return userData;
    }

    public LiveData<Boolean> updateUserProfile(UserModel user) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("Users").child(user.getUid());
        
        ref.setValue(user)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));
        
        return result;
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public static class AuthResult {
        public boolean success;
        public String message;
        public UserModel user;

        public AuthResult(boolean success, String message, UserModel user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
    }
}
