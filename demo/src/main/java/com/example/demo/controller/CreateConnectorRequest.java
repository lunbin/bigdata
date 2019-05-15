package com.example.demo.controller;

import java.util.Map;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateConnectorRequest {
  private final String name;
  private final Map<String, String> config;

  @JsonCreator
  public CreateConnectorRequest(
      @JsonProperty("name") String name, @JsonProperty("config") Map<String, String> config) {
    this.name = name;
    this.config = config;
  }

  @JsonProperty
  public String name() {
    return name;
  }

  @JsonProperty
  public Map<String, String> config() {
    return config;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CreateConnectorRequest that = (CreateConnectorRequest) o;
    return Objects.equals(name, that.name) && Objects.equals(config, that.config);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, config);
  }
}
