FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

COPY src ./src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
	&& apt-get install -y --no-install-recommends ffmpeg python3 python3-pip \
	&& python3 -m pip install --no-cache-dir --index-url https://download.pytorch.org/whl/cpu torch==2.5.1+cpu \
	&& python3 -m pip install --no-cache-dir openai-whisper==20240930 \
	&& apt-get clean \
	&& rm -rf /var/lib/apt/lists/*

RUN useradd --create-home --shell /bin/bash voys

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

RUN mkdir -p /app/storage /home/voys/.cache/whisper \
	&& chown -R voys:voys /app /home/voys/.cache

USER voys

ENV HOME=/home/voys
ENV VOYS_STORAGE_ROOT=/app/storage/audio
ENV VOYS_WHISPER_OUTPUT_ROOT=/app/storage/transcripts

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
