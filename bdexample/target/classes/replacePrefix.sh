#!/bin/bash
sed -i "s/${1}/${2}/g" core-site.xml
sed -i "s/${1}/${2}/g" hdfs-site.xml
