# 1. Java 17 JDK 이미지 사용 (빌드 단계)
FROM eclipse-temurin:17-jdk AS build

# 빌드 단계에서 작업할 디렉터리를 /app으로 설정
WORKDIR /app

# 프로젝트 코드 복사
COPY . .

# 빌드 (테스트 제외)
RUN ./gradlew build -x test

# 2. 실행 단계 (JRE만 사용하여 이미지 경량화)
FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Render가 포트 감지할 수 있도록
EXPOSE 8888

# 컨테이너 시작 시 실행 명령
CMD ["java", "-jar", "app.jar"]