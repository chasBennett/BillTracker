package com.example.billstracker.custom_objects;

/**
 * @noinspection unused
 */
public class Category {

    private String categoryName;
    private int categoryPercentage;

    public Category(String categoryName, int categoryPercentage) {

        setCategoryName(categoryName);
        setCategoryPercentage(categoryPercentage);

    }

    /**
     * @noinspection unused
     */
    public Category() {

    }


    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCategoryPercentage() {
        return categoryPercentage;
    }

    public void setCategoryPercentage(int categoryPercentage) {
        this.categoryPercentage = categoryPercentage;
    }
}
