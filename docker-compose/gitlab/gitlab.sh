#!/bin/bash

if [ $1 == "up" ]; then
  COMPOSE_PROJECT_NAME=gitlab docker-compose up -d
elif [ $1 == "down" ]; then
  COMPOSE_PROJECT_NAME=gitlab docker-compose down
fi
