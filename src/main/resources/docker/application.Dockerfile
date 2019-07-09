#
# @code-generation-comment@
#

FROM openjdk:8-jre-alpine

RUN addgroup user && adduser -D -G user user
WORKDIR /@project.artifactId@

RUN mkdir -p @documents@ && chmod 500 @documents@ 

COPY maven/@project.build.finalName@.jar ./application.jar
COPY entrypoint.sh ./entrypoint.sh
RUN touch ./application.properties
RUN touch ./application.yml

RUN chmod 400 ./application.jar \
 && chmod 400 ./application.properties \
 && chmod 400 ./application.yml \
 && chmod 500 ./entrypoint.sh

RUN chown -R user:user ./
USER user:user

HEALTHCHECK CMD exit $(echo $(echo $(wget http://localhost:@server.port@@server.servlet.context-path@/actuator/health -q -O -) | grep -cv UP))
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1"
ENTRYPOINT ["/bin/sh", "./entrypoint.sh"]
EXPOSE @server.port@
