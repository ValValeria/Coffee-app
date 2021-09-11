package com.example.coffee.repositories;

import com.example.coffee.models.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DishCrudRepository extends JpaRepository<Dish, Integer> {
    @Async
    CompletableFuture<List<Dish>> findAllByTitle(String title);

    @Async
    @Query("select d from Dish d")
    CompletableFuture<List<Dish>> findAllDishes();

    @Async
    CompletableFuture<List<Dish>> findFirstById(int id);
}
