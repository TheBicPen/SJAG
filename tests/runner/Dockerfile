FROM alpine:3.15
RUN apk add curl
COPY test.sh /
RUN chmod +x /test.sh
ENTRYPOINT [ "/test.sh" ]