package org.messplacement.messsecond.Service;

import org.messplacement.messsecond.Dao.MealPriceRepository;
import org.messplacement.messsecond.Dao.UserRepository;
import org.messplacement.messsecond.Dao.WeeklyMenuRepository;
import org.messplacement.messsecond.Entities.MealPrice;
import org.messplacement.messsecond.Entities.Role;
import org.messplacement.messsecond.Entities.User;
import org.messplacement.messsecond.Entities.WeeklyMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds default data on first startup:
 *   - Default users  (admin, guest, demo student)
 *   - Default meal prices (75 / 100 / 125)
 *   - Full weekly menu from the original hardcoded menu.service.ts data
 *
 * All operations are idempotent: existing rows are never overwritten.
 *
 * IMPORTANT: Change default passwords immediately after first deployment.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired private UserRepository       userRepository;
    @Autowired private MealPriceRepository  mealPriceRepository;
    @Autowired private WeeklyMenuRepository weeklyMenuRepository;
    @Autowired private PasswordEncoder      passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedUsers();
        seedPrices();
        seedMenu();
    }

    // ── Users ──────────────────────────────────────────────────────────────

    private void seedUsers() {
        seedUser("admin",      "AdminPassword123",  Role.ROLE_ADMIN,   null);
        seedUser("guest",      "GuestDemo123",      Role.ROLE_GUEST,   null);
        seedUser("22BAI10072", "StudentPass123",    Role.ROLE_STUDENT, "22BAI10072");
    }

    private void seedUser(String username, String rawPassword, Role role, String regNo) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setRegNo(regNo);
            userRepository.save(user);
            System.out.printf("[DataInitializer] Seeded user '%s' (%s)%n", username, role);
        }
    }

    // ── Meal Prices ────────────────────────────────────────────────────────

    private void seedPrices() {
        if (mealPriceRepository.count() == 0) {
            mealPriceRepository.saveAll(List.of(
                    new MealPrice("BREAKFAST", 75),
                    new MealPrice("LUNCH",     100),
                    new MealPrice("DINNER",    125)
            ));
            System.out.println("[DataInitializer] Seeded meal prices.");
        }
    }

    // ── Weekly Menu ────────────────────────────────────────────────────────

    private void seedMenu() {
        if (weeklyMenuRepository.count() > 0) return;

        List<WeeklyMenu> items = List.of(
            menu("MONDAY",    "BREAKFAST", "Idly, Vada",              "Sambar, Chutney, Fruit, Bread Butter Jam, Milk, Tea, Coffee"),
            menu("MONDAY",    "LUNCH",     "Plain/Lemon/Coconut Rice","Roti, Rajma, Dal Tadka, White Rice, Seasonal Veg, Sambhar, Rasam, Fryums, Buttermilk"),
            menu("MONDAY",    "DINNER",    "Roti",                    "Aloo Bhindi Dry, White Rice, Dal Fry, Rasam & Pickle"),
            menu("MONDAY",    "SNACKS",    "Kachori/Noodles",         "Sauce, Tea / Coffee / Milk"),

            menu("TUESDAY",   "BREAKFAST", "Poori, Vermicelli",       "Bhal Chutney, Fruit, Bread Butter Jam, Milk, Tea, Coffee"),
            menu("TUESDAY",   "LUNCH",     "Ghee Rice",               "Roti, Dal Tadka, White Rice, Sambhar, Greens Kottu, Jeera Rasam, Salad, Curd"),
            menu("TUESDAY",   "DINNER",    "Roti",                    "Dal Fry, White Rice, Aloo Gobi Fry, Rasam & Pickle"),
            menu("TUESDAY",   "SNACKS",    "Pani Poori/Chana Sundal", "Tea / Coffee / Milk"),

            menu("WEDNESDAY", "BREAKFAST", "Onion/Veg Uttapam",       "Sambhar, Tomato Chutney, Fruits, Bread Butter Jam, Milk, Tea, Coffee, Boiled Egg, Sprouts"),
            menu("WEDNESDAY", "LUNCH",     "Puli Kulambu",            "Roti, Dal Makhani, White Rice, Avial, Garlic Rasam & Pickle, Salad, Boondi"),
            menu("WEDNESDAY", "DINNER",    "Roti",                    "Plain Dal, White Rice, Kadai Chicken / Paneer Masala, Rasam & Pickle"),
            menu("WEDNESDAY", "SNACKS",    "Samosa",                  "Sauce, Tea / Coffee / Milk"),

            menu("THURSDAY",  "BREAKFAST", "Poha, Jalebi",            "Pongal, Sambar, Chutney, Fruits, Bread Butter Jam, Milk, Tea, Coffee"),
            menu("THURSDAY",  "LUNCH",     "Veg Kofta",               "Roti, Jeera Aloo, White Rice, Tomato Dal, Sambhar, Beetroot Poriyal, Rasam, Pickle & Appalam"),
            menu("THURSDAY",  "DINNER",    "Aloo Paneer",             "Roti, White Rice, Egg Masala, Dal Fry, Rasam & Pickle, Tomato/Veg Soup"),
            menu("THURSDAY",  "SNACKS",    "Sweet Corn Salad/Burger", "Tea / Coffee / Milk"),

            menu("FRIDAY",    "BREAKFAST", "Bhatura",                 "Chole Masala, Fruits, Bread Butter Jam, Milk, Tea, Coffee"),
            menu("FRIDAY",    "LUNCH",     "Veg Biryani",             "Roti, Brinjal Masala, Masoor Dal, Plain Rice, Sambhar Rice, Rasam, Boondi Raitha"),
            menu("FRIDAY",    "DINNER",    "Roti",                    "Dal Tadka, White Rice, Butter Chicken, Chili Paneer, Rasam & Pickle"),
            menu("FRIDAY",    "SNACKS",    "Vada Pav",                "Green Chutney, Tea / Coffee / Milk"),

            menu("SATURDAY",  "BREAKFAST", "Rava Upma & Pav Bhaji",  "Chutney, Fruits, Bread Butter Jam, Milk, Tea, Coffee, Boiled Egg / Sprouts"),
            menu("SATURDAY",  "LUNCH",     "Poori",                   "Roti, Chole Masala, Jeera Rice, Seasonal Veg, White Rice, Rasam & Pickle, Butter Milk"),
            menu("SATURDAY",  "DINNER",    "Fried Rice",              "Roti, Masala Dal, White Rice, Veg Manchurian, Rasam & Pickle"),
            menu("SATURDAY",  "SNACKS",    "Cutlet/Dabeli",           "Tea / Coffee / Milk"),

            menu("SUNDAY",    "BREAKFAST", "Masala Dosa",             "Sambar, Chutney, Fruits, Bread Butter Jam, Milk, Tea, Coffee"),
            menu("SUNDAY",    "LUNCH",     "Masala Dal",              "Roti, Chicken Dum Biryani (Limited), Veg Biryani, Paneer Masala, White Rice, Rasam, Onion Cucumber Raitha"),
            menu("SUNDAY",    "DINNER",    "Roti",                    "Masoor Dal, Mix Veg Dry, White Rice, Rasam & Pickle, Gulab Jamun"),
            menu("SUNDAY",    "SNACKS",    "Dhokla / Pasta",          "Green Chutney / Sweet Chutney, Tea / Coffee / Milk")
        );

        weeklyMenuRepository.saveAll(items);
        System.out.printf("[DataInitializer] Seeded weekly menu (%d entries).%n", items.size());
    }

    private WeeklyMenu menu(String day, String mealType, String name, String description) {
        WeeklyMenu m = new WeeklyMenu();
        m.setDayOfWeek(day);
        m.setMealType(mealType);
        m.setName(name);
        m.setDescription(description);
        return m;
    }
}
