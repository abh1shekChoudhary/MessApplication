package org.messplacement.messsecond.Entities;

import jakarta.persistence.*;

/**
 * Stores one meal entry per (day, mealType) combination.
 * Admin can edit name and description via PUT /menu/{day}/{mealType}.
 */
@Entity
@Table(name = "weekly_menu", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"day_of_week", "meal_type"})
})
public class WeeklyMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_of_week", nullable = false, length = 10)
    private String dayOfWeek;   // "MONDAY" … "SUNDAY"

    @Column(name = "meal_type", nullable = false, length = 10)
    private String mealType;    // "BREAKFAST", "LUNCH", "DINNER", "SNACKS"

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    public WeeklyMenu() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
