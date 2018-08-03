#!/bin/bash

yum -y install ruby ruby-devel rubygems rpm-build
curl -L get.rvm.io | bash -s stable
gpg2 --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3
curl -L get.rvm.io | bash -s stable
source /usr/local/rvm/scripts/rvm
rvm install 2.3.3
rvm use 2.3.3
rvm remove 2.0.0
gem install redis
