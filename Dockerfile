# ---- Build stage: compile Java servlets ----
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy source code and web content
COPY src/ ./src/
COPY WebContent/ ./WebContent/

# Download servlet-api.jar (Tomcat 9 compatible)
RUN mkdir -p /app/lib && \
    curl -sL "https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar" \
    -o /app/lib/servlet-api.jar

# Compile all Java source files
RUN mkdir -p WebContent/WEB-INF/classes && \
    find src -name "*.java" > /tmp/sources.txt && \
    javac -encoding UTF-8 \
          -cp "/app/lib/servlet-api.jar:WebContent/WEB-INF/lib/*" \
          -d WebContent/WEB-INF/classes \
          @/tmp/sources.txt

# Copy properties files to classes directory
RUN find src -name "*.properties" -exec cp {} WebContent/WEB-INF/classes/ \;

# ---- Runtime stage: Tomcat 9 ----
FROM tomcat:9.0-jre17

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the compiled web application
COPY --from=builder /app/WebContent/ /usr/local/tomcat/webapps/ROOT/

# Expose port (Render sets $PORT; Tomcat default 8080)
ENV PORT=8080
EXPOSE 8080

CMD ["catalina.sh", "run"]
