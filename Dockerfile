FROM gradle:8-jdk17 as build-base

USER gradle
RUN mkdir /home/gradle/project
WORKDIR /home/gradle/project

ADD ./app/build.gradle .
ADD ./app/src ./src

FROM build-base as development-build

RUN gradle shadowJar

FROM amazoncorretto:17-al2-jdk as base

# VS Code dev containers apparently needs tar & gzip to install the VS Code Server in the container
#RUN yum -y install tar gzip && yum -y clean all  && rm -rf /var/cache

RUN mkdir /opt/app
COPY --from=development-build /home/gradle/project/build/libs/project-all.jar /opt/app
WORKDIR /opt/app

FROM base as development
# Use this if you want to connect to the JVM using VS Code or another debugger
#CMD ["java", "-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=y", "-cp", "project-all.jar", "com.trumedia.data.trackman.App"]
# Use this to run the app without a debugger
CMD ["java", "-cp", "project-all.jar", "com.trumedia.project.App"]

