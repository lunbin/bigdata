FROM 10.19.248.200:29006/tools/dep-centos7-spark-2.1.1-hadoop-2.7:0.1-lbsheng
COPY target/sparkstream-1.0-SNAPSHOT-jar-with-dependencies.jar /opt
COPY submit.sh /opt/submit.sh
RUN chmod +x /opt/submit.sh
CMD ["tail", "-f", "/etc/hosts"]

