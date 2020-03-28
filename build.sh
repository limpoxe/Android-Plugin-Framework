#!/bin/sh
./gradlew clean

# 编译主包
./gradlew :Samples:PluginMain:assembleF1Debug

# 编译基础插件
./gradlew :Samples:PluginTesBase:assembleF1Debug

# 编译所有插件
./gradlew assembleF1Debug

# 重新编译主包
./gradlew :Samples:PluginMain:assembleF1Debug

./gradlew :Samples:PluginMain:installF1Debug