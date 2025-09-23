# Dockerfile

# 1. 베이스 이미지 선택 (Java 17 JRE가 포함된 가벼운 이미지)
FROM openjdk:17-jdk-slim

# 2. 빌드된 JAR 파일의 위치를 변수로 지정
#    build/libs/ 디렉토리의 *-SNAPSHOT.jar 파일을 의미합니다.
ARG JAR_FILE=build/libs/*-SNAPSHOT.jar

# 3. JAR 파일을 컨테이너 내부로 복사하고 이름을 app.jar로 변경
COPY ${JAR_FILE} app.jar

# 4. 컨테이너가 시작될 때 실행할 명령어
#    --spring.profiles.active=cloud-prod 프로파일로 실행합니다.
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=cloud-prod"]