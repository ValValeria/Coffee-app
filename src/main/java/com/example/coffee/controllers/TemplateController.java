package com.example.coffee.controllers;

import com.example.coffee.errors.NotFoundException;
import com.example.coffee.models.Dish;
import com.example.coffee.models.FullDish;
import com.example.coffee.services.ResponseService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class TemplateController {
    private final ResponseService<FullDish> responseService;

    @Autowired
    public TemplateController(ResponseService<FullDish> responseService){
        this.responseService = responseService;
    }

    @GetMapping("/")
    private String home(){
       return "index";
    }

    @GetMapping("/about")
    private String about(){
       return "about";
    }

    @GetMapping("/store")
    private String store(){
        return "store";
    }

    @GetMapping("/product/{id}")
    private String product(@PathVariable int id,
                           HttpServletRequest httpServletRequest,
                           Model model){
        String url = getFullUrl(httpServletRequest, "/api/product/" + id);

        LoggerFactory.getLogger(TemplateController.class).info(url);

        ResponseService<Map<String, Object>> responseService = new RestTemplate().getForObject(url, ResponseService.class);
        
        if(responseService == null){
            throw new NotFoundException();
        }

        Map<String, Map<String, Object>> map = responseService.getData();
        Map<String, Object> fullDish = map.get("dish");

        if(fullDish == null){
            throw new NotFoundException();
        }

        model.addAttribute("dish", fullDish);

        return "dish";
    }

    private String getFullUrl(HttpServletRequest request,
                              String urlPart
                              ){
        String scheme = request.getScheme();             // http
        String serverName = request.getServerName();     // hostname.com
        int port = request.getServerPort();

        StringBuilder requestURL = new StringBuilder(scheme + "://" + serverName + ":" + port);
        String queryString = request.getQueryString();

        if(!urlPart.endsWith("/")) {
            requestURL.append("/");
        }

        requestURL.append(urlPart);

        if (queryString == null) {
            return requestURL.toString();
        }

        return requestURL.append('?').append(queryString).toString();
    }
}
