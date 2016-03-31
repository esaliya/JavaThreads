#!/bin/bash
cp=/N/u/sekanaya/.m2/repository/com/google/guava/guava/15.0/guava-15.0.jar:/N/u/sekanaya/sali/software/jdk1.8.0/jre/../lib/tools.jar:/N/u/sekanaya/.m2/repository/habanero-java-lib/habanero-java-lib/0.1.4-SNAPSHOT/habanero-java-lib-0.1.4-SNAPSHOT.jar:/N/u/sekanaya/.m2/repository/net/java/dev/jna/jna/4.1.0/jna-4.1.0.jar:/N/u/sekanaya/.m2/repository/net/java/dev/jna/jna-platform/4.1.0/jna-platform-4.1.0.jar:/N/u/sekanaya/.m2/repository/net/openhft/affinity/3.0/affinity-3.0.jar:/N/u/sekanaya/.m2/repository/net/openhft/compiler/2.2.0/compiler-2.2.0.jar:/N/u/sekanaya/.m2/repository/net/openhft/lang/6.7.2/lang-6.7.2.jar:/N/u/sekanaya/.m2/repository/ompi/ompijavabinding/1.10.1/ompijavabinding-1.10.1.jar:/N/u/sekanaya/.m2/repository/org/kohsuke/jetbrains/annotations/9.0/annotations-9.0.jar:/N/u/sekanaya/.m2/repository/org/ow2/asm/asm/5.0.3/asm-5.0.3.jar:/N/u/sekanaya/.m2/repository/org/slf4j/slf4j-api/1.7.12/slf4j-api-1.7.12.jar:/N/u/sekanaya/.m2/repository/org/xerial/snappy/snappy-java/1.1.1.6/snappy-java-1.1.1.6.jar:/N/u/sekanaya/sali/git/github/esaliya/JavaThreads/target/javathreads-1.0-SNAPSHOT.jar

iterations=2
rows=100
cols=100000
dim=3

nodes=1

ppn=$2
tpp=$1
bindto=$3

$BUILD/bin/mpirun --hostfile nodes.txt -np $(($ppn*$nodes)) --map-by ppr:$ppn:node:SPAN --bind-to $bindto --rank-by core java -cp $cp org.saliya.javathreads.array.ProgramGlobalArray $tpp $iterations $rows $cols $dim


