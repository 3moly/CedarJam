[instructions for macos & memory consumption benchmark]
# step 1
### run ./gradlew :benchmark:jvmBenchmarkJar
it will generate "benchmark/build/benchmarks/jvm/jars/benchmark-jvm-jmh-JMH.jar"

# step 2
### check current java. use what you need to test
java -version
/usr/libexec/java_home -V

nano ~/.zshrc

export JAVA_HOME=/Users/new07/Library/Java/JavaVirtualMachines/jbr-21.0.10/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

source ~/.zshrc

# step 3
### install async profiler

brew install async-profiler

export DYLD_LIBRARY_PATH=/opt/homebrew/opt/async-profiler/lib

### works
java \
-XX:+UnlockDiagnosticVMOptions \
-XX:+DebugNonSafepoints \
-Djdk.attach.allowAttachSelf=true \
--add-opens java.base/java.lang=ALL-UNNAMED \
-jar benchmark/build/benchmarks/jvm/jars/benchmark-jvm-jmh-JMH.jar \
-prof "async:event=alloc;output=flamegraph"

java \
-XX:+UnlockDiagnosticVMOptions \
-XX:+DebugNonSafepoints \
-Djdk.attach.allowAttachSelf=true \
--add-opens java.base/java.lang=ALL-UNNAMED \
-jar benchmark/build/benchmarks/jvm/jars/benchmark-jvm-jmh-JMH.jar \
-prof "async:event=alloc;output=jfr"


### does nothing
./gradlew :benchmark:jvmBenchmark -Pbenchmark.jvhArgs="-prof async:alloc"