package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.UserEntity;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class controller {
  @Autowired
  HttpServletRequest request;

  @Autowired
  UserService userService;


  // --kafka.topic in command will replace kafka.topic in application.properties
  @Value("${kafka.topic}")
  String topic;

  @GetMapping("/gettopic")
  public String getTopic() {
    return topic;
  }

  @RequestMapping(path = "/get/{name}")
  public String getRequestPath(@PathVariable String name) {
    String queryString = "";
    if (request.getQueryString() != null) {
      queryString = request.getQueryString();
    }
    String method = request.getMethod();

    System.out.println("requestUrl:" + request.getRequestURL() + queryString + " " + "method: " + method );

    System.out.println( "thread name: " + Thread.currentThread().getName());
    System.out.println("sleep 50");
    try {
      Thread.sleep(1000 * 50);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return request.getRequestURL() + queryString;

  }

  @PostMapping("/insert")
  public void insert(@RequestBody UserEntity user) {
    userService.insert(user);
  }

  @GetMapping("/getUserAll")
  public List<UserEntity> getUserAll() {
    return userService.getUserAll();
  }

  public String formatResquestUrl(String requesturl) {
    return null;
  }
}
