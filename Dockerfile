FROM tomcat:8.5.81
WORKDIR /usr/local/tomcat/webapps
COPY build/libs/ApiManager-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps
RUN mv ApiManager-0.0.1-SNAPSHOT.war ROOT.war
COPY conf/* /usr/local/tomcat/conf/
CMD ["catalina.sh", "run"]
