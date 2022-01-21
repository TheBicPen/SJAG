FROM httpd:alpine3.15
ARG PORT
COPY sample_html /usr/local/apache2/htdocs/
RUN sed -ir "s/^Listen [0-9]\+/Listen ${PORT}/g" /usr/local/apache2/conf/httpd.conf