FROM anapsix/alpine-java:8_jdk
MAINTAINER github.com/tpopela
WORKDIR /vips

COPY src src
COPY lib lib

RUN javac -classpath "lib/antlr-runtime-3.5.2.jar:lib/cssbox-4.8.jar:lib/jstyleparser-1.20.jar:lib/nekohtml-1.9.21.jar:lib/slf4j-api-1.7.7.jar:lib/unbescape-1.1.0.RELEASE.jar:lib/xercesImpl-2.11.0.jar:lib/xml-apis-1.4.01.jar" -sourcepath "src" src/org/fit/vips/VipsTester.java
# Just to test the whether the compilation was successful
RUN java -cp "src:lib/*" org.fit.vips.VipsTester
RUN echo "#/bin/sh" >> run; echo "java -cp \"src:lib/*\" org.fit.vips.VipsTester \$@" >> run; chmod +x run

CMD bash
