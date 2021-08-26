package com.example.coffee.controllers;

import com.example.coffee.errors.NotFoundException;
import com.example.coffee.models.Dish;
import com.example.coffee.services.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Objects;

@Controller
@ControllerAdvice
public class DishController {
    private final DishService dishService;

    @Autowired
    DishController(DishService dishService){
        this.dishService = dishService;
    }

    @GetMapping("products/{id}")
    private String product(@PathVariable int id, ModelMap model){
        Dish dish = this.dishService.findDishById(id);

        if(Objects.isNull(dish)) {
            throw new NotFoundException();
        }

        model.addAttribute("dish", dish);

        return "dish";
    }

    @ModelAttribute
    public void addAttributes(ModelMap model) {
        model.addAttribute("dish", new Dish());
    }
}
