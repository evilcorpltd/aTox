#!/bin/bash

rm -rf app/jnilibs
mkdir -p app/jnilibs

platforms=("armeabi-v7a" "armeabi" "x86" "arm64-v8a" "x86_64")

for p in "${platforms[@]}"; do
    mkdir -p "app/src/jnilibs/$p"
done

echo "Downloading native libraries..."

wget https://build.tox.chat/job/tox4j_build_android_armel_release/lastSuccessfulBuild/artifact/artifacts/libtox4j-c.so -O app/jnilibs/armeabi-v7a/libtox4j-c.so
wget https://build.tox.chat/job/tox4j_build_android_armel_release/lastSuccessfulBuild/artifact/artifacts/libtox4j-c.so -O app/jnilibs/armeabi/libtox4j-c.so
wget https://build.tox.chat/job/tox4j_build_android_x86_release/lastSuccessfulBuild/artifact/artifacts/libtox4j-c.so -O app/jnilibs/x86/libtox4j-c.so
wget https://build.tox.chat/job/tox4j_build_android_arm64_release/lastSuccessfulBuild/artifact/artifacts/libtox4j-c.so -O app/jnilibs/arm64-v8a/libtox4j-c.so
wget https://build.tox.chat/job/tox4j_build_android_x86-64_release/lastSuccessfulBuild/artifact/artifacts/libtox4j-c.so -O app/jnilibs/x86_64/libtox4j-c.so

echo "Downloading tox4j..."

mkdir -p app/libs
rm -f app/libs/*.jar

wget https://build.tox.chat/job/tox4j-api_build_android_multiarch_release/lastSuccessfulBuild/artifact/tox4j-api/target/scala-2.11/tox4j-api_2.11-0.1.2.jar -O app/libs/tox4j-api-c.jar
wget https://build.tox.chat/job/tox4j_build_android_arm64_release/lastSuccessfulBuild/artifact/artifacts/tox4j-c_2.11-0.1.2-SNAPSHOT.jar -O app/libs/tox4j-c.jar
