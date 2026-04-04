package org.messplacement.messsecond;

import org.messplacement.messsecond.Dao.WeeklyMenuRepository;
import org.messplacement.messsecond.Entities.WeeklyMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the editable weekly menu.
 *
 * GET  /menu              — all authenticated roles
 * GET  /menu/{day}        — all authenticated roles
 * PUT  /menu/{day}/{type} — ADMIN only
 */
@RestController
@RequestMapping("/menu")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "http://localhost",
        "https://mess-application-front-end-angular.vercel.app"
})
public class MenuController {

    @Autowired
    private WeeklyMenuRepository weeklyMenuRepository;

    /** Returns the full 7-day menu (all meal types). */
    @GetMapping
    public List<WeeklyMenu> getFullMenu() {
        return weeklyMenuRepository.findAll();
    }

    /** Returns all meal entries for a specific day (e.g. MONDAY). */
    @GetMapping("/{day}")
    public List<WeeklyMenu> getMenuByDay(@PathVariable String day) {
        return weeklyMenuRepository.findByDayOfWeekIgnoreCase(day);
    }

    /** Admin updates the name/description for a specific day+meal combination. */
    @PutMapping("/{day}/{mealType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WeeklyMenu> updateMenuItem(
            @PathVariable String day,
            @PathVariable String mealType,
            @RequestBody WeeklyMenu updated) {

        return weeklyMenuRepository
                .findByDayOfWeekIgnoreCaseAndMealTypeIgnoreCase(day, mealType)
                .map(existing -> {
                    existing.setName(updated.getName());
                    existing.setDescription(updated.getDescription());
                    return ResponseEntity.ok(weeklyMenuRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
