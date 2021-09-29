#
# @code-generation-comment@
#

FROM openjdk:17

WORKDIR /@project.artifactId@/

RUN useradd user
RUN chown -R user /@project.artifactId@/
USER user
RUN mkdir -p @documents@
RUN chmod 700 @documents@

COPY maven/@project.build.finalName@.jar ./application.jar
COPY entrypoint.sh ./entrypoint.sh

HEALTHCHECK CMD exit $(echo $(echo $(wget http://localhost:@server.port@@server.servlet.context-path@/actuator/health -q -O -) | grep -cv UP))
ENV JAVA_OPTS="-XX:MaxRAMPercentage=50.0"
ENTRYPOINT ["/bin/sh", "./entrypoint.sh"]
EXPOSE @server.port@
