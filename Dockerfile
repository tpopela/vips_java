FROM anapsix/alpine-java:8_jdk
MAINTAINER github.com/tpopela
WORKDIR /vips

COPY src src
COPY lib lib

RUN javac -classpath "lib/cssbox-4.14.jar" -sourcepath "src" src/org/fit/vips/VipsTester.java
# Just to test the whether the compilation was successful
RUN java -cp "src:lib/*" org.fit.vips.VipsTester
RUN echo "#/bin/sh" >> run; echo "java -cp \"src:lib/*\" org.fit.vips.VipsTester \$@" >> run; chmod +x run

CMD bash
