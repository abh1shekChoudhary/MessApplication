package org.messplacement.messsecond.Dao;

import org.messplacement.messsecond.Entities.WeeklyMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyMenuRepository extends JpaRepository<WeeklyMenu, Long> {

    List<WeeklyMenu> findByDayOfWeekIgnoreCase(String dayOfWeek);

    Optional<WeeklyMenu> findByDayOfWeekIgnoreCaseAndMealTypeIgnoreCase(
            String dayOfWeek, String mealType);
}
