FROM centos:7 

ENV MYSQL_USER mysql
ENV MYSQL_GROUP mysql
ENV MYSQL_CLUSTER_VERSION 7.5
ENV MYSQL_CLUSTER_MINOR_VERSION 10
ENV MYSQL_CLUSTER_HOME /usr/local/mysql
ENV MYSQL_DATADIR /app/ndb/data

RUN yum install -y libaio \
    && yum install -y libnuma.so.1 \
    && yum install -y numactl.x86_64 \
    && yum install -y net-tools

RUN groupadd ${MYSQL_GROUP} && useradd -g ${MYSQL_GROUP} ${MYSQL_USER} \
	&& curl -sL https://dev.mysql.com/get/Downloads/MySQL-Cluster-${MYSQL_CLUSTER_VERSION}/mysql-cluster-gpl-${MYSQL_CLUSTER_VERSION}.${MYSQL_CLUSTER_MINOR_VERSION}-linux-glibc2.12-x86_64.tar.gz | tar xz -C /usr/local \
	&& mv /usr/local/mysql-cluster-gpl-${MYSQL_CLUSTER_VERSION}.${MYSQL_CLUSTER_MINOR_VERSION}-linux-glibc2.12-x86_64/ /usr/local/mysql \ 
	&& mkdir -p ${MYSQL_DATADIR}/backup \
#配置过程均由官网配置过程改动而来
	&& cd /usr/local \
	&& chown -R ${MYSQL_USER}:${MYSQL_GROUP} ${MYSQL_DATADIR}/backup \
	&& chgrp -R ${MYSQL_GROUP} ${MYSQL_CLUSTER_HOME} \
        && ln -s ${MYSQL_CLUSTER_HOME}/bin/ndb_mgm* /usr/local/bin/ \
        && ln -s ${MYSQL_CLUSTER_HOME}/bin/ndbd /usr/local/bin/ \
        && ln -s ${MYSQL_CLUSTER_HOME}/bin/ndbmtd /usr/local/bin/ \
        && ln -s ${MYSQL_CLUSTER_HOME}/bin/mysql /usr/local/bin/ \
        && ln -s ${MYSQL_CLUSTER_HOME}/bin/mysqld /usr/local/bin/ \
	&& cd /usr/local/bin \
        && chmod +x ndb_mgm* \
        && chmod +x ndb* \
        && chmod +x mysql* \
        && cd ${MYSQL_CLUSTER_HOME} \
        && chown -R root . \
        && chown -R mysql ${MYSQL_DATADIR} \
        && chown -R mysql ${MYSQL_DATADIR}/backup/ \
        && chgrp -R mysql . \ 
	&& cp support-files/mysql.server /etc/rc.d/init.d/ \
        && chmod +x /etc/rc.d/init.d/mysql.server \
        && chkconfig --add mysql.server \
	&& cd ${MYSQL_CLUSTER_HOME}/bin \
	&& rm -f mysql_client_test && rm -f mysql_client_test_embedded && rm -f mysqltest && rm -f mysqltest_embedded && rm -f mysqlxtest \
	&& rm -r ${MYSQL_CLUSTER_HOME}/mysql-test

EXPOSE 1186 3306 2202

