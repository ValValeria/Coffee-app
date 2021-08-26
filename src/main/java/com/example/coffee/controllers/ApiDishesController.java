package com.example.coffee.controllers;

import com.example.coffee.classes.DataResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController("/api")
public class ApiDishesController {
    private final String URL = "https://common-api.rozetka.com.ua/v2/fat-menu/full?front-type=xl";

    @GetMapping("/products")
    private void products() throws Throwable {
        HttpRequest request = HttpRequest.newBuilder(new URI(URL)).GET().build();
        HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        new ObjectMapper().readValue(httpResponse.body(), DataResponse.class);
    }
}
