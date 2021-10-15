#!/bin/sh
./gradlew clean
if (($? != 0)); then
  echo "BUILD FAILED!"
  exit 0
fi

# 编译主包
./gradlew :Samples:PluginMain:assembleF1Debug
if (($? != 0)); then
  echo "BUILD FAILED!"
  exit 0
fi

# 编译基础插件
./gradlew :Samples:PluginTesBase:assembleF1Debug
if (($? != 0)); then
  echo "BUILD FAILED!"
  exit 0
fi

# 编译所有插件
./gradlew assembleF1Debug
if (($? != 0)); then
  echo "BUILD FAILED!"
  exit 0
fi

# 重新编译主包
./gradlew :Samples:PluginMain:assembleF1Debug
if (($? != 0)); then
  echo "BUILD FAILED!"
  exit 0
fi

./gradlew :Samples:PluginMain:installF1Debug
if (($? != 0)); then
  echo "BUILD FAILED!"
  exit 0
else
  echo "ALL BUILD SUCCESSFUL!"
fi
