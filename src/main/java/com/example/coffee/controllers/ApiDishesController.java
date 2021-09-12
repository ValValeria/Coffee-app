package com.example.coffee.controllers;

import com.example.coffee.classes.DataResponse;
import com.example.coffee.models.Dish;
import com.example.coffee.repositories.DishCrudRepository;
import com.example.coffee.services.ResponseService;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



@Controller()
public class ApiDishesController {
    private final String URL = "https://rozetka.com.ua/section/konfety/";
    private final DishCrudRepository dishRepository;
    private final ResponseService<List<Dish>> responseService;
    private List<Dish> results = new ArrayList<>();
    Logger logger = LoggerFactory.getLogger(ApiDishesController.class);


    @Autowired
    ApiDishesController(DishCrudRepository dishCrudRepository,
                        ResponseService<List<Dish>> responseService)
    {
        this.dishRepository = dishCrudRepository;
        this.responseService = responseService;
    }

    @RequestMapping(value = "/api/products", method = RequestMethod.GET)
    @ResponseBody
    private void products(HttpServletResponse httpServletResponse) throws Throwable
    {
        Consumer<List<Dish>> listConsumer = dishes -> {
            responseService.setData(Map.of("dishes", dishes));

            try {
                ObjectWriter objectMapper = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String result = objectMapper.writeValueAsString(responseService);

                httpServletResponse.setContentType("application/json");
                httpServletResponse.setCharacterEncoding("UTF-8");

                httpServletResponse.getWriter().write(result);
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            } catch (Throwable e) {
                e.printStackTrace();

                httpServletResponse.setStatus(500);
            }
        };

        if(dishRepository.count() > 0){
            results = dishRepository.findAll();
        } else {
            Document productsDocument = Jsoup.connect(URL)
                    .userAgent("Chrome/4.0.249.0 Safari/532.5")
                    .referrer("http://www.google.com")
                    .get();
            Elements elements = productsDocument.select("div.goods-tile");

            for (Element element: elements) {
                this.setUpDish(element);
            }
        }

        listConsumer.accept(results);
    }

    private void setUpDish(Element element){
        try{
            String title = element.select("span.goods-tile__title").text();
            String priceStr = element.select("span.goods-tile__price-value").text();
            Double price = Double.parseDouble(priceStr);
            String url = element.select("a.goods-tile__heading").attr("href");

            Document productDocument = Jsoup.connect(url)
                    .userAgent("Chrome/4.0.249.0 Safari/532.5")
                    .referrer("http://www.google.com")
                    .get();

            Elements descrP = productDocument.select(".product-about__description-content > p");
            StringBuilder descr = new StringBuilder();

            for (Element p : descrP) {
                descr.append(p.text());
            }

            String imageRef = productDocument.select("img.picture-container__picture").attr("src");

            logger.info("Image src is " + imageRef);

            Pattern pattern = Pattern.compile("\\d{3}\\s{1}");
            Matcher matcher = pattern.matcher(title);

            if(matcher.find()){
                String weight = matcher.group();

                Dish dish = new Dish();
                dish.setWeight(Double.parseDouble(weight));
                dish.setTitle(title);
                dish.setImagePath(imageRef);
                dish.setPrice(price);
                dish.setLink(url);

                if(descr.length() > 1900){
                   dish.setDescription(descr.substring(0, 1900) + "....");
                } else {
                   dish.setDescription(descr.toString());
                }

                if(descr.length() > 0){
                    Deque<Dish> list1 = new ArrayDeque<>(dishRepository.findAll(Sort.by("id").descending()));
                    Dish dish1 = list1.peekFirst();
                    int id = 1;

                    if(dish1 != null && dish1.getId() > 0){
                        id = dish1.getId() + 1;
                    }

                    dish.setId(id);
                    results.add(dish);
                    dishRepository.save(dish);
                }
            }
        } catch(IOException exception){
            exception.printStackTrace();
        }
    }
}
