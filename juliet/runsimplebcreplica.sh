#!/bin/bash
cp=/N/u/sekanaya/.m2/repository/com/google/guava/guava/15.0/guava-15.0.jar:/N/u/sekanaya/.m2/repository/habanero-java-lib/habanero-java-lib/0.1.4-SNAPSHOT/habanero-java-lib-0.1.4-SNAPSHOT.jar:/N/u/sekanaya/.m2/repository/net/java/dev/jna/jna/4.1.0/jna-4.1.0.jar:/N/u/sekanaya/.m2/repository/net/java/dev/jna/jna-platform/4.1.0/jna-platform-4.1.0.jar:/N/u/sekanaya/.m2/repository/net/openhft/affinity/3.0/affinity-3.0.jar:/N/u/sekanaya/.m2/repository/ompi/ompijavabinding/1.10.1/ompijavabinding-1.10.1.jar:/N/u/sekanaya/.m2/repository/org/kohsuke/jetbrains/annotations/9.0/annotations-9.0.jar:/N/u/sekanaya/.m2/repository/org/slf4j/slf4j-api/1.7.12/slf4j-api-1.7.12.jar::/N/u/sekanaya/sali/git/github/esaliya/JavaThreads/target/javathreads-1.0-SNAPSHOT.jar

threadCount=1
iterations=1
globalColCount=200448
rowCountPerUnit=174

#opts="-XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation"

java $opts -cp $cp org.saliya.javathreads.BCReplicaSimple $threadCount $iterations $globalColCount $rowCountPerUnit


