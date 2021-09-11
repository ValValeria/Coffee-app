package com.example.coffee.controllers;

import com.example.coffee.classes.DataResponse;
import com.example.coffee.models.Dish;
import com.example.coffee.repositories.DishCrudRepository;
import com.example.coffee.services.ResponseService;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.nimbusds.jose.util.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


@Controller()
public class ApiDishesController {
    private final String URL = "https://rozetka.com.ua/section/konfety/";
    private final DishCrudRepository dishRepository;
    private final ResponseService<List<Dish>> responseService;
    private List<Dish> results = new ArrayList<>();

    @Autowired
    ApiDishesController(DishCrudRepository dishCrudRepository,
                        ResponseService<List<Dish>> responseService)
    {
        this.dishRepository = dishCrudRepository;
        this.responseService = responseService;
    }

    @RequestMapping(value = "/api/products", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    private void products(HttpServletResponse httpServletResponse,
                          PrintWriter printWriter) throws Throwable
    {
        Consumer<List<Dish>> listConsumer = dishes -> {
            responseService.setData(Map.of("dishes", dishes));

            try {
                String result = new ObjectMapper().writeValueAsString(responseService);

                printWriter.print(result);
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        };

        if(dishRepository.count() > 0){
            dishRepository.findAllDishes()
                    .thenAccept(listConsumer)
                    .exceptionally(v -> {
                        httpServletResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                        return null;
                    });
        } else {
            HttpRequest request = HttpRequest.newBuilder(new URI(URL)).GET().build();

            HttpClient.newHttpClient()
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(v -> {
                        try {
                            Document productsDocument = Jsoup.connect(URL)
                                    .userAgent("Chrome/4.0.249.0 Safari/532.5")
                                    .referrer("http://www.google.com")
                                    .get();
                            Elements elements = productsDocument.select("div.goods-tile");

                            for (Element element: elements) {
                                this.setUpDish(element);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                        return results;
                    })
                    .thenAccept(listConsumer)
            ;
        }
    }

    private void setUpDish(Element element){
        try{
            String title = element.select("span.goods-tile__title").text();
            String imageRef = element.select("a.goods-tile__picture > img").attr("src");
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

            Pattern pattern = Pattern.compile("\\d{3}\\s{1}");
            Matcher matcher = pattern.matcher(title);

            if(matcher.find()){
                String weight = matcher.group();

                Dish dish = new Dish();
                dish.setWeight(Double.parseDouble(weight));
                dish.setTitle(title);
                dish.setImagePath(imageRef);
                dish.setPrice(price);

                if(descr.length() > 300){
                   dish.setDescription(descr.substring(0, 300));
                } else {
                   dish.setDescription(descr.toString());
                }

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
        } catch(IOException exception){
            exception.printStackTrace();
        }
    }
}
