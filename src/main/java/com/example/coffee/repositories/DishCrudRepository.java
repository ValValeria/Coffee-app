package com.example.coffee.repositories;

import com.example.coffee.models.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DishCrudRepository extends JpaRepository<Dish, Integer> {
}
