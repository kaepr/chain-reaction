# https://github.com/andfadeev/clojure-service-template/blob/master/Dockerfile

FROM alpine:3.21 AS tailwind-builder

WORKDIR /app
COPY . .

RUN apk update
RUN apk upgrade
RUN apk add bash

RUN apk add gcompat build-base
RUN wget https://github.com/tailwindlabs/tailwindcss/releases/latest/download/tailwindcss-linux-x64
RUN chmod +x tailwindcss-linux-x64
RUN mv tailwindcss-linux-x64 /bin/tailwindcss
RUN alias tailwindcss=/bin/tailwindcss

RUN /bin/tailwindcss -c ./resources/tailwind.config.js -i ./resources/tailwind.css -o ./resources/public/css/main.css

# https://practical.li/engineering-playbook/continuous-integration/docker/clojure-multi-stage-dockerfile/

FROM clojure:temurin-21-tools-deps-bookworm-slim AS builder

RUN mkdir -p /build
WORKDIR /build

COPY deps.edn build.clj /build/
RUN clojure -P -X:build

COPY ./ /build
RUN clojure -T:build uber

# Run Time Stage
FROM eclipse-temurin:21-alpine AS final

RUN apk add --no-cache dumb-init~=1.2.5

ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    clojure

RUN addgroup -S chainreactionapp && adduser -S chainreactionapp -G chainreactionapp
RUN mkdir -p /service && chown -R clojure. /service
USER clojure

WORKDIR /service
COPY --from=builder --chown=clojure:clojure build/target/chainreactionapp-1.0-standalone.jar /service/chainreactionapp-1.0-standalone.jar

EXPOSE 8080
ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["java", "-jar", "/service/chainreactionapp-1.0-standalone.jar"]
