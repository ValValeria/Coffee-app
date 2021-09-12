package com.example.coffee.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FullDish extends Dish{
    private Map<String, String> characteristics = new HashMap<>();
    private List<Comment> comments = new ArrayList<>();

    public FullDish(Dish dish){
        super.setDescription(dish.getDescription());
        super.setId(dish.getId());
        super.setLink(dish.getLink());
        super.setImagePath(dish.getImagePath());
        super.setPrice(dish.getPrice());
        super.setWeight(dish.getWeight());
        super.setTitle(dish.getTitle());
    }

    public Map<String, String> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(Map<String, String> characteristics) {
        this.characteristics = characteristics;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public static Comment createNewComment(){
        return new Comment();
    }
}
