package com.example.task_23;

public class Expense {

    private String keyID;
    private String description;
    private double amount;
    private String category;
    private String date;

    public Expense() {
    }
    public Expense(String keyID, String description, double amount, String category, String date) {
        this.keyID = keyID;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public String getKeyID() {
        return keyID;
    }
    public void setKeyID(String keyID) {
        this.keyID = keyID;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
}
