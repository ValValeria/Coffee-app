package com.example.coffee.controllers;

import com.example.coffee.errors.NotFoundException;
import com.example.coffee.models.Comment;
import com.example.coffee.models.Dish;
import com.example.coffee.models.FullDish;
import com.example.coffee.repositories.DishCrudRepository;
import com.example.coffee.services.ResponseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;


@RestController
public class ViewDishController {
    private final ResponseService<Dish> responseService;
    private final Logger logger;
    private final DishCrudRepository dishCrudRepository;

    @Autowired
    ViewDishController(ResponseService<Dish> responseService,
                       DishCrudRepository dishCrudRepository
                   ){
        this.responseService = responseService;
        this.logger = LoggerFactory.getLogger(ViewDishController.class);
        this.dishCrudRepository = dishCrudRepository;
    }

    @RequestMapping(value = "/api/product/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    private String product(@PathVariable int id) throws IOException {
        Dish dish = this.dishCrudRepository.getById(id);

        if(dish == null) {
            this.logger.info("The dish is not found");

            throw new NotFoundException();
        }

        try{
            String link = dish.getLink();

            Document productDocument = Jsoup.connect(link)
                    .userAgent("Chrome/4.0.249.0 Safari/532.5")
                    .referrer("http://www.google.com")
                    .get();

            String title = productDocument.select(".product__title").text();
            dish.setTitle(title);

            StringBuilder descr = new StringBuilder();

            for (Element p : productDocument.select(".product-about__description-content > p")) {
                descr.append(p.text());
            }

            dish.setDescription(descr.toString());

            FullDish fullDish = new FullDish(dish);

            for(Element commentElement: productDocument.select(".comment")){
                Comment comment1 = new Comment();
                comment1.setAuthor(commentElement.selectFirst(".comment__author").text());

                Element advantages = commentElement.selectFirst(".comment__essentials-item dd");

                if(advantages != null){
                    comment1.setAdvantages(advantages.text());
                }

                Element disadvantages = commentElement.select(".comment__essentials-item dd").last();

                if(disadvantages != null){
                    comment1.setDisadvantages(disadvantages.text());
                }

                comment1.setRating(5);

                fullDish.getComments().add(comment1);
            }

            for(Element characterictics: productDocument.select(".characteristics-full__item")){
                Element charactericticsKey = characterictics.selectFirst(".characteristics-full__label span");
                Element charactericticsValue = characterictics.selectFirst(".characteristics-full__value li a");

                if(charactericticsValue == null){
                    charactericticsValue = characterictics.selectFirst(".characteristics-full__sub-list li span");
                }

                fullDish.getCharacteristics().put(charactericticsKey.text(), charactericticsValue.text());
            }

            responseService.setData(Map.of("dish", fullDish));
        } catch(Throwable e){
            e.printStackTrace();
        }

        return new ObjectMapper().writeValueAsString(responseService);
    }
}
