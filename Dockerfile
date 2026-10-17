# ============================================================
# TimoNews — Render deployment image
# ============================================================
# Stage 1 builds the WAR with Maven; Stage 2 runs it on Tomcat 10.1.
# Tomcat 10 is required: the app uses the jakarta.* namespace and
# will not start on Tomcat 9 or older.
# ============================================================

# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Cache dependencies separately from source so rebuilds are faster
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: run ----------
FROM tomcat:10.1-jdk17-temurin

# Serve at the root path so the app lives at https://<service>.onrender.com/
RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /build/target/timo-news.war /usr/local/tomcat/webapps/ROOT.war

COPY docker/entrypoint.sh /usr/local/bin/entrypoint.sh
# Strip CRLF in case the file was checked out on Windows, then make executable
RUN sed -i 's/\r$//' /usr/local/bin/entrypoint.sh && chmod +x /usr/local/bin/entrypoint.sh

# Render supplies PORT at runtime; 8080 is only a local fallback
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
