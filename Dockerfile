# --- Этап Сборки ---
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /app

# Кэшируем зависимости: сначала копируем только pom.xml
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем проект
COPY src ./src
RUN mvn clean package -DskipTests

# --- Этап Запуска ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Устанавливаем часовой пояс Благовещенска (Якутское время UTC+9)
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Yakutsk /etc/localtime \
    && echo "Asia/Yakutsk" > /etc/timezone

# Копируем готовый jar-файл из предыдущего этапа
COPY --from=builder /app/target/campus_BGPU-0.0.1-SNAPSHOT.jar app.jar

# Запускаем приложение
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
