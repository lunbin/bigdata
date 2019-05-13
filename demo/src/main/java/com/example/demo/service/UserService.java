package com.example.demo.service;

import java.util.List;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private UserMapper userMapper;

  public List<UserEntity> getUserAll() {
    return  userMapper.getUserAll();
  }

  public void insert(UserEntity entity) {
    userMapper.insert(entity);
  }
}
