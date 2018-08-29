FROM centos7-jdk8:0.1

RUN yum -y install gcc automake autoconf libtool make && yum install -y wget
RUN cd /opt && \
    wget "http://download.redis.io/releases/redis-3.2.9.tar.gz" && \
    tar -zxf redis-3.2.9.tar.gz && \
    ln -s redis-3.2.9 redis && \
    rm -f redis-3.2.9.tar.gz && \
    cd redis && make && make PREFIX=/usr/local/redis install
COPY redis.conf /opt/redis
COPY entrypoint.sh /opt
COPY rh-ruby23.sh /etc/profile.d
RUN chmod +x /etc/profile.d/rh-ruby23.sh
RUN chmod +x /opt/entrypoint.sh
#RUN gpg --keyserver hkp://keys.gnupg.net --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3 7D2BAF1CF37B13E2069D6956105BD0E739499BDB 
#RUN curl -L get.rvm.io | bash -s stable 
#RUN source /usr/local/rvm/scripts/rvm 
#RUN /usr/local/rvm/bin/rvm install 2.3.3 && /usr/local/rvm/bin/rvm use 2.3.3 && /usr/local/rvm/bin/rvm remove 2.0.0 
#RUN /usr/local/rvm/rubies/ruby-2.3.3/bin/gem install redis 
RUN yum install -y centos-release-scl-rh && yum install rh-ruby23 -y && scl enable rh-ruby23 bash
ENTRYPOINT ["/usr/local/redis/bin/redis-server","/opt/redis/redis.conf"] 
