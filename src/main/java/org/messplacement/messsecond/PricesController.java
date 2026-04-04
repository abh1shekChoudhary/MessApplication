package org.messplacement.messsecond;

import org.messplacement.messsecond.Dao.MealPriceRepository;
import org.messplacement.messsecond.Entities.MealPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for meal price configuration.
 *
 * GET  /prices         — all roles (guests see prices too)
 * PUT  /prices/{type}  — ADMIN only
 */
@RestController
@RequestMapping("/prices")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "http://localhost",
        "https://mess-application-front-end-angular.vercel.app"
})
public class PricesController {

    @Autowired
    private MealPriceRepository mealPriceRepository;

    @GetMapping
    public List<MealPrice> getAllPrices() {
        return mealPriceRepository.findAll();
    }

    @PutMapping("/{mealType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MealPrice> updatePrice(
            @PathVariable String mealType,
            @RequestBody MealPrice updated) {

        return mealPriceRepository.findById(mealType.toUpperCase())
                .map(existing -> {
                    existing.setPriceInr(updated.getPriceInr());
                    return ResponseEntity.ok(mealPriceRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
