package com.example.coffee.services;

import com.example.coffee.models.Dish;
import com.example.coffee.repositories.DishCrudRepository;
import org.springframework.stereotype.Service;

@Service
public class DishService {
    private final DishCrudRepository dishRepository;

    DishService(DishCrudRepository repository){
        this.dishRepository = repository;
    }

    public Dish findDishById(int id){
        return this.dishRepository.getById(id);
    }
}
