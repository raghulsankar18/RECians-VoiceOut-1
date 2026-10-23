# ---- Build stage ----
FROM eclipse-temurin:17-jdk as builder

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy Maven descriptor and source code
COPY pom.xml .
COPY src ./src

# Build the WAR (skip tests for speed)
RUN mvn -B package -DskipTests

# ---- Runtime stage ----
FROM tomcat:10.1-jre17

# Clean default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy the generated WAR as ROOT
COPY --from=builder /app/target/voiceout.war /usr/local/tomcat/webapps/ROOT.war

# Vercel will set $PORT; Tomcat listens on 8080 by default – expose
ENV PORT=8080
EXPOSE 8080

CMD ["catalina.sh","run"]
