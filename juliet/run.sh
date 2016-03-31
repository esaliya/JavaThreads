#!/bin/bash
cp=/N/u/sekanaya/.m2/repository/com/google/guava/guava/15.0/guava-15.0.jar:/N/u/sekanaya/.m2/repository/habanero-java-lib/habanero-java-lib/0.1.4-SNAPSHOT/habanero-java-lib-0.1.4-SNAPSHOT.jar:/N/u/sekanaya/.m2/repository/net/java/dev/jna/jna/4.1.0/jna-4.1.0.jar:/N/u/sekanaya/.m2/repository/net/java/dev/jna/jna-platform/4.1.0/jna-platform-4.1.0.jar:/N/u/sekanaya/.m2/repository/net/openhft/affinity/3.0/affinity-3.0.jar:/N/u/sekanaya/.m2/repository/ompi/ompijavabinding/1.8.1/ompijavabinding-1.8.1.jar:/N/u/sekanaya/.m2/repository/org/kohsuke/jetbrains/annotations/9.0/annotations-9.0.jar:/N/u/sekanaya/.m2/repository/org/slf4j/slf4j-api/1.7.12/slf4j-api-1.7.12.jar:/N/u/sekanaya/sali/git/github/esaliya/JavaThreads/target/javathreads-1.0-SNAPSHOT.jar

#java -cp $cp org.saliya.javathreads.AffinityThreads $1 $2  true true 
taskset -c 0,24 java -cp $cp org.saliya.javathreads.AffinityThreads $1 $2  true true 
