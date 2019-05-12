package com.example.demo.mapper;

import java.util.List;
import com.example.demo.model.UserEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface UserMapper {
  @Select("select * from user")
  @Results({
      @Result(property = "id",column = "id"),
      @Result(property = "name",column = "name"),
      @Result(property = "password", column = "password")
  })
  List<UserEntity> getUserAll();


  @Insert("insert into user(id,name,password) values(#{id},#{name},#{password})")
  void insert(UserEntity user);

}
