package com.example.onlineshop.Model;

public class AppSettingsModel {
    private String currency;
    private String currencySymbol;
    private double taxRate;
    private double shippingFee;
    private double freeShippingThreshold;
    private int maxCartItems;
    private boolean maintenanceMode;
    private String minAppVersion;
    private int returnPolicyDays;
    private String supportEmail;
    private String supportPhone;

    public AppSettingsModel() {
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public double getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public void setFreeShippingThreshold(double freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public int getMaxCartItems() {
        return maxCartItems;
    }

    public void setMaxCartItems(int maxCartItems) {
        this.maxCartItems = maxCartItems;
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    public String getMinAppVersion() {
        return minAppVersion;
    }

    public void setMinAppVersion(String minAppVersion) {
        this.minAppVersion = minAppVersion;
    }

    public int getReturnPolicyDays() {
        return returnPolicyDays;
    }

    public void setReturnPolicyDays(int returnPolicyDays) {
        this.returnPolicyDays = returnPolicyDays;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public String getSupportPhone() {
        return supportPhone;
    }

    public void setSupportPhone(String supportPhone) {
        this.supportPhone = supportPhone;
    }
}

