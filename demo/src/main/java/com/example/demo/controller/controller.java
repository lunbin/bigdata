package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.UserEntity;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
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


  // for ObjectNode test
  @GetMapping("/generateJson1")
  public String generateJson1() {
    ObjectMapper mapper = new ObjectMapper();
    ArrayNode arrayNode = mapper.createArrayNode();
    ObjectNode node = mapper.createObjectNode();
    node.put("id",1);
    node.put("name","lbsheng");
    arrayNode.add(node);
    return arrayNode.toString();
  }

  @PostMapping("/jsonPropertyTest")
  public String jsonPropertyTest(@RequestBody CreateConnectorRequest createConnectorRequest) {

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("config", createConnectorRequest.config());
    jsonObject.put("name", createConnectorRequest.name());
    return jsonObject.toString();
  }

  // for JSONObject test
  @GetMapping("/generateJson2")
  public String generateJson2() {
    JSONObject js = new JSONObject();
    js.put("id",2);
    js.put("name","slb");
    return js.toString();
  }
  public String formatResquestUrl(String requesturl) {
    return null;
  }
}
