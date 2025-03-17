package com.company.apimanager.app;

import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;

@Component
public class RestClient {
    public String InvokeGet() {
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        RestTemplate restTemplate = new RestTemplate();

        //String rs = restTemplate.exchange("https://jsonplaceholder.typicode.com/todos/1", HttpMethod.GET, entity, String.class).getBody();
        //restTemplate.exchange("https://jsonplaceholder.typicode.com/todos/1", HttpMethod.GET, entity, RestObj.class);

        //RestObj restObj = restTemplate.getForObject("https://jsonplaceholder.typicode.com/todos/1", RestObj.class);
        ResponseEntity<TestObj> response = restTemplate.exchange("https://jsonplaceholder.typicode.com/todos/1", HttpMethod.GET, entity, TestObj.class);

        TestObj restObj = response.getBody();
        String rs = restObj.GetTitle();
        return rs;
    }

    public String post (String url, String body) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
    }

    public String post (String url, String body, HttpHeaders headers) {
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<String> entity = new HttpEntity<String>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
    }


    public String postToken (String url, String body, String type, String token) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (!type.isEmpty()) {
            headers.add(type, token);
        }
        HttpEntity<String> entity = new HttpEntity<String>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
    }

    public String put (String url, String body) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(body, headers);

        return restTemplate.exchange(url, HttpMethod.PUT, entity, String.class).getBody();
    }
}