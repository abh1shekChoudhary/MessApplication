package org.messplacement.messsecond.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Stores configurable meal prices (BREAKFAST, LUNCH, DINNER).
 * Admin can update prices via PUT /prices/{mealType}.
 * MessService reads these on each calculation instead of using hardcoded values.
 */
@Entity
@Table(name = "meal_prices")
public class MealPrice {

    @Id
    @Column(name = "meal_type", length = 20)
    private String mealType;   // "BREAKFAST", "LUNCH", "DINNER"

    @Column(name = "price_inr", nullable = false)
    private int priceInr;

    public MealPrice() {}

    public MealPrice(String mealType, int priceInr) {
        this.mealType = mealType;
        this.priceInr = priceInr;
    }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public int getPriceInr() { return priceInr; }
    public void setPriceInr(int priceInr) { this.priceInr = priceInr; }
}
