package org.messplacement.messsecond.Dao;

import org.messplacement.messsecond.Entities.MealPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealPriceRepository extends JpaRepository<MealPrice, String> {
}
