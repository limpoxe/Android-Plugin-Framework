
目录结构

| 文件夹        |     说明     |
| :----------- | :-----------|
| baidumapsdk | 独立插件工程 测试百度地图|
| PluginBase | 非独立插件工程 |
| PluginHelloWorld | 独立插件工程 |
| PluginMain | 宿主工程 |
| PluginShareLib | 宿主的依赖 |
| PluginTest | 非独立插件工程 |
| PluginTest2 | 非独立插件工程 |
| plugintest3 | 非独立插件工程依赖 |
| wxsdklibrary | 非独立插件工程 测试微信分享|
| admobdemo | 独立插件 测试google广告Sdk|     
     
编译宿主会产生宿主apk

编译插件会产生插件apk

PluginBase作为公共插件，被PluginTest、wxsdklibrary两个插件依赖。

因此编译PluginTest、wxsdklibrary这两个插件前前需要先编译PluginBase插件

重要：如果是非独立插件, 需要先编译宿主, 再编译插件
