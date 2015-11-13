# Android-Plugin-Framework


此项目是Android插件开发框架完整源码及示例。用来通过动态加载的方式在宿主程序中运行插件APK。


# 已支持的功能：
  1、插件apk无需安装，由宿主程序动态加载运行。
  
  2、支持fragment、activity、service、receiver、contentprovider、jni so、application、组件。
  
  3、支持插件自定义控件、宿主自定控件。
  
  4、开发插件apk和开发普通apk时代码编写方式无区别。对插件apk和宿主程序来说，插件框架完全透明，开发插件apk时无约定、无规范约束。无复杂打包脚本。
  
  5、插件中的组件拥有真正生命周期，完全交由系统管理、非反射无代理
  
  6、支持插件引用宿主程序的依赖库、插件资源、宿主资源。
  
  7、支持插件使用宿主程序主题（部分系统暂不支持，如MX5）、系统主题、插件自身主题以及style（插件主题不支持透明）
  
  8、支持非独立插件和独立插件（非独立插件指自己编译的需要依赖宿主中的公共类和资源的插件，不可独立安装运行。独立插件又分为两种：一种是自己编译的不需要依赖宿主中的类和资源的插件，可独立安装运行；一种是第三方发布的apk，如从应用市场下载的apk，可独立安装运行，这种只做了简单支持。）
  
  9、支持插件Activity的4个LaunchMode

  10、支持插件资源文件中直接引用共享依赖库中的资源(目前粗略测试结果只支持5.x及以上)

  11、支持插件发送notification时在RemoteViews使用插件自定义的布局资源（只支持5.x及以上）

# 暂不支持的功能：

  1、插件Activity切换动画不支持使用插件自己的资源。
  
  2、不支持插件申请权限，权限必须预埋到宿主中。
  
  3、不支持第三方app试图唤起插件中的组件时直接使用插件app的Intent。即插件app不能认为自己是一个正常安装的app。第三方app要唤起插件中的静态组件时必须由宿主程序进行桥接。

# 开发注意事项

    1、若仅需支持独立插件，无任何要求，与常规开发无异。

    2、若需要支持非独立插件，必须解决资源id冲突的问题，解决冲突的方式有如下两种：

        a）通过public.xml文件来解决资源id冲突，优点是纯天然，缺点是不支持gradle编译（因此也不支持Android Studio），且编译脚本稍显复杂（只能用Ant或maven编译），代码在for-eclispe-ide分支上，不再更新。

        b）通过定制过的aapt编译，优点是支持gradle编译，因此支持Android Studio，对主题资源id分组支持较好，缺点是非纯天然，需要替换sdk原生的aapt，且要区分多平台，buildTools版本更新后需同步升级aapt。
            定制的aapt由 openAtlasExtention@github 项目提供，目前的版本是基于22.0.1，将项目中的BuildTools替换到本地Android Sdk中相应版本的BuildTools中，并指定gradle的buildTools version为对应版本即可。

        另：非独立插件中的class不能同时存在于宿主程序中，因此其引用的公共库仅参与编译，不参与打包，具体可关注下相应脚本，参看demo。
    
    
目录结构说明：

  1、PluginCore工程是插件库核心工程，用于提供对插件功能的支持。

  2、PluginMain是用来测试的宿主程序Demo工程。

  3、PluginShareLib是用来测试非独立插件的公共依赖库Demo工程。
  
  4、PluginTest是用来测试的非独立插件Demo工程。
  
  5、PluginHelloWorld是用来测试的独立插件Demo工程。

  
demo安装说明：

  1、宿主程序demo工程的assets目录下已包含了编译好的独立插件demo apk和非独立插件demo apk。

  2、宿主程序demo工程可直接编译安装运行。
  
  3、插件demo工程：
    将openAtlasExtention@github项目提供的BuildTools替换自己的Sdk中相应版本的BuildTools。剩下的步骤照常即可。
    
    待插件编译完成后，插件的编译脚本会自动将插件demo的apk复制到PlugiMain/assets目录下（参看插件工程的build.gradle）,然后重新打包安装PluginMain即可。
    
    或者也可将插件复制到sdcard，然后在宿主程序中调用PluginLoader.installPlugin("插件apk绝对路径")
  进行安装。

  4、简易安装方式
        直接将插件demo安装到系统中，PluginMain工程中PluginDebugHelper会监听系
        统的应用安装广播，监听到插件demo安装广播后，再自动调用PluginLoader.installPlugin("/data/app/[插件demo].apk")
        进行插件安装。免去复制到sdcard的过程。

  5、如果使用eclipse ＋ ant ＋ public.xml需要关注PluginTest工程的ant.properties文件和project.properties文件以及custom_rules.xml,若编译失败，请升级androidSDK。


# 实现原理简介：
  1、插件apk的class
  
     通过构造插件apk的Dexclassloader来加载插件apk中的类。
     DexClassLoader的parent设置为宿主程序的classloader，即可将主程序和插件程序的class贯通。
     若是独立插件，将parent设置为宿主程序的classloader的parent，可隔离宿主class和插件class，此时宿主和插件可包含同名的class。
  
  2、插件apk的Resource
  
     直接构造插件apk的AssetManager和Resouce对象即可，需要注意的是，
     通过addAssetsPath方法添加资源的时候，需要同时添加插件程序的资源文件和宿主程序的资源，
     以及其依赖的资源。这样可以将Resource合并到一个Context里面去，解决资源访问时需要切换上下文的问题。
  
  3、插件apk中的资源id冲突
  
    完成上述第二点以后，宿主程序资源id和插件程序id可能有重复而参数冲突。
    我们知道，资源id是在编译时生成的，其生成的规则是0xPPTTNNNN
    PP段，是用来标记apk的，默认情况下系统资源PP是01，应用程序的PP是07
    TT段，是用来标记资源类型的，比如图标、布局等，相同的类型TT值相同，但是同一个TT值
    不代表同一种资源，例如这次编译的时候可能使用03作为layout的TT，那下次编译的时候可能
    会使用06作为TT的值，具体使用那个值，实际上和当前APP使用的资源类型的个数是相关联的。
    NNNN则是某种资源类型的资源id，默认从1开始，依次累加。
    
    那么我们要解决资源id问题，就可从TT的值开始入手，只要将每次编译时的TT值固定，即可是资
    源id达到分组的效果，从而避免重复。例如将宿主程序的layout资源的TT固定为03，将插件程序
    资源的layout的TT值固定为23,即可解决资源id重复的问题了。
    
    固定资源id的TT值的办法也非常简单，提供一份public.xml，在public.xml中指定什么资源类型以
    什么TT值开头即可。具体public.xml如何编写，可参考PluginMain/res/values/public.xml 以及
    PluginTest/res/values/public.xml俩个文件，它们是分别用来固定宿主程序和插件程序资源id的范围的。


    更新：openAtlasExtention@github项目提供了重写过的aapt指定PP段来实现id分组，不再使用public.xml来实现分组
        原因是android gradle插件1.3.0以上版本不支持public.xml文件，也无法识别public-padding节点
    

  4、插件apk的Context和LayoutInfalter
  
    构造一个Context对象即可，具体的Context实现请参考PluginCore/src/com/plugin/core/PluginContextTheme.java
    关键是要重写几个获取资源、主题的方法，以及重写getClassLoader方法，再从构造粗来的context中获取LayoutInfalter

  6、插件代码无约定无规范约束。
  
    要做到这一点，主要有几点：
    1、上诉第4步骤，
    2、在classloader树中插入自己的Classloader，在loadclass时进行映射
    3、替换ActivityThread的的Instrumentation对象和Handle CallBack对象，用来拦截组件的创建过程。
    4、利用反射修改成员变量，注入Context。利用反射调用隐藏方法。
    
  7、插件中Activity等不在宿主manifest中注册即拥有完整生命周期的方法。
    
    由于Activity等是系统组件，必须在manifest中注册才能被系统唤起并拥有完整生命周期。
    通过反射代理方式实现的实际是伪生命周期，并非完整生命周期。要实现插件组件免注册有2个方法。
    
    前提：宿主中预注册几个组件。预注册的组件可实际存在也可不存在。
    
    a、替换classloader。适用于所有组件。
     App安装时，系统会扫描app的Manifest并缓存到一个xml中，activity启动时，系统会现在查找缓存的xml，
     如果查到了，再通过classLoad去load这个class，并构造一个activity实例。那么我们只需要将classload
     加载这个class的时候做一个简单的映射，让系统以为加载的是A class，而实际上加载的是B class，达到挂羊头买狗肉的效果，
     即可将预注册的A组件替换为未注册的插件中的B组件，从而实现插件中的组件
     完全被系统接管，而拥有完整生命周期。其他组件同理。

    
    b、替换Instrumention。
     这种方式仅适用于Activity。通过修改Instrumentation进行拦截，可以利用Intent传递参数。
     如果是Receiver，利用Handler Callback进行拦截，也能很好的利用Intent传递参数。
     如果是Service，其在创建实例时是没有Intent的，只能通过Classloader在loadclass时进行拦截
     
    
  8、通过activity代理方式实现加载插件中的activity是如何实现的
  
     要实现这一点，同样是基于上述第4点，构造出插件的Context后，通过attachBaseContext的方式，
     替换代理Activiyt的context即可。
     另外还需要在获得插件Activity对象后，通过反射给Activity的attach()方法中attach的成员变量赋值。
     
     更新：activity代理方式已放弃，不再支持，要了解实现可以查看历史版本
  
  9、插件编译问题。
  
     如果插件和宿主共享依赖库，常见的如supportv4，那么编译插件的时候不可将共享库编译到插件当中，
     包括共享库的代码以及R文件，只需在编译时添加到classpath中，且插件中如果要使用共享依赖库中的资源，
     需要使用共享库的R文件来进行引用。这几点在PluginTest示例工程中有体现。

     更新：已接入gradle，通过provided方式即可，具体可参考PluginShareLib和PluginTest的build.gradle文件
  
  10、插件Fragment
    插件UI可通过fragment或者activity来实现
    
    如果是fragment实现的插件，又分为3种：
    1、fragment运行在宿主中的普通Activity中
    2、fragment运行在宿主中的特定Activity中
    3、fragment运行在插件中的Activity中

    对第2种和第3种，fragmet的开发方式和正常开发方式没有任何区别
    
    对第1种，fragmeng中凡是要使用context的地方，都需要使用通过PluginLoader.getPluginContext()获取的context，
    那么这种fragment对其运行容器没有特殊要求
    
    第1种Activity和第2种Activity，两者在代码上没有任何区别。主要是插件框架在运行时需要区分注入的Context的类型。


  11、插件主题
    重要实现原理仍然基于上述第2、3点。
    
  12、插件Activity的LaunchMode
    要实现插件Activity的LaunchMode，需要在宿主程序中预埋若干个相应launchMode的Activity（预注册的组件可实际存在也可不存在），在运行时进行动态映射选择
  
# 需要注意的问题

   1、项目插件化后，特别需要注意的是宿主程序混淆问题。公共库混淆后，可能会导致非独立插件程序运行时出现classnotfound，原因很好理解。
   所以公共库一定要排除混淆。

   2、android sdk中的build tools版本较低时也无法编译public.xml文件，因此如果采用Ant + public.xml的方式编译，应采用较新版本的buildtools。

  更新：已迁移至android studio ＋gradle + aapt，eclispe ＋ ant ＋public.xml的方式不再更新
  
联系作者：
  Q：15871365851， 添加时请注明插件开发。
  Q群：207397154
