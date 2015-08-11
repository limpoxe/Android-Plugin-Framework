# Android-Plugin-Framework


此项目是Android插件框架完整源码以及实例。用来开发Android插件APK，并通过动态加载的方式在宿主程序中运行。

插件分两类：
    1、独立插件，即插件本身可以独立安装运行。
    
    2、非独立插件，即插件需要依赖宿主程序的class和资源，不能独立安装运行。
      非独立插件中的class不能同时存在于宿主程序中。需要打包时排除掉。
    
    
目录结构说明：

  1、PluginCore工程是插件库核心工程，用于提供对插件功能的支持。

  2、PluginMain是用来测试的插件宿主程序Demo工程。

  3、PluginShareLib是用来测试的插件宿主程序的依赖库Demo工程。
  
  4、PluginTest是用来测试的非独立插件Demo工程。此工程下有用来编译插件的ant脚本。

  
demo安装说明：

  1、宿主程序工程可以通过ant编译或者导入eclipse后直接点击Run菜单进行安装。

  2、插件Demo工程需要通过插件ant脚本编译。编译命令为 “ant clean debug” 
  
     原因是Demo中引用了宿主程序的依赖库。需要在编译时对共享库进行排除，如果是独立插件不需要特定的编译脚本。

  3、插件编译出来以后，可以将插件复制到sdcard，然后在宿主程序中调用PluginLoader.installPlugin("插件apk绝对路径")进行安装

  4、简易安装方式，是使用编译命令为 “ant clean debug install” 直接将插件apk安装到系统中，PluginMain工程会监听系统的应用安装广播，监听到插件apk安装广播后，
再自动调用PluginLoader.installPlugin("/data/app/插件apk文件.apk")进行插件安装。免去复制到sdcard的过程。

  5、需要关注PluginTest工程的ant.properties文件和project.properties文件以及custom_rules.xml文件，
  插件使用宿主程序共享库，以及共享库R引用，和编译时排除的功能，都在这3个配置文件中体现

编译环境：如果编译失败，请升级androidSDK


# 已支持的功能：
  1、插件apk无需安装，由宿主程序动态加载运行。
  
  2、支持fragment、activity、service、receiver、application组件。
  
  3、插件中的activity、service、receiver组件拥有真正生命周期（不是使用反射实现的伪生命周期）、同时也支持Activity反射代理。
     
  4、插件中的组件无需继承特定基类。
  
  5、支持插件中使用自定义控件、使用宿主程序提供的自定义控件
  
  6、支持插件资源和宿主资源混合使用。意即支持如下场景:
  
     插件程序和宿主程序共用依赖库时，插件中的某布局文件使用了宿主程序中的自定义控件，
     而宿主程序中的自定义控件又使用了宿主程序中的布局文件。此时插件布局文件相当于即
     包含了宿主布局，有包含了插件布局。插件中无需做任何特殊处理，通过layoutinflater即可使用这种布局文件。
     
  7、插件中的各种资源、布局、R、以及宿主程序中的各种资源、布局、R等可随意使用。
  
  8、支持插件使用宿主程序主题、系统主题、插件自身主题以及style(控件style暂不支持5.x)。
  
  
# 暂不支持的功能：

  1、插件中定义的控件的style 暂不支持5.x
  
  2、暂不支持插件资源文件中直接使用宿主程序中的资源。但是支持间接使用。
     例如在上述“已支持的功能”6中描述的,实际就是间接使用。
  
# 实现原理简介：
  1、插件apk的class
  
     通过构造插件apk的Dexclassloader来加载插件apk中的类。
     DexClassLoader的parent设置为宿主程序的classloader，即可将主程序和插件程序的class贯通。
     若是独立插件，将parent设置为宿主程序的classloader的parent，可隔离宿主class和插件class。
  
  2、插件apk的Resource
  
     直接构造插件apk的AssetManager和Resouce对象即可，需要注意的是，
     通过addAssetsPath方法添加资源的时候，需要同时添加插件程序的资源文件和宿主程序的资源，
     以及其依赖的资源。这样可以讲Resource合并到一个Context里面去，很多资源问题都迎刃而解。
  
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
    
    固定资源idTT值的版本也非常简单，提供一份public.xml，在public.xml中指定什么资源类型以
    什么TT值开头即可。具体public.xml如何编写，可参考PluginMain/res/values/public.xml 以及 PluginTest/res/values/public.xml俩个文件，它们是分别用来固定宿主程序和插件程序资源id的范围的。
  
  4、插件apk的Context和LayoutInfalter
  
    构造一个Context对象即可，具体的Context实现请参考PluginCore/src/com/plugin/core/PluginContextTheme.java
    关键是要重写几个获取资源、主题的方法，以及重写getClassLoader方法，再从构造粗来的context中获取LayoutInfalter

  6、插件代码不依赖特殊代码，如继承特定的基类、接口等。
  
    要做到这一点，主要有几点：
    1、上诉第4步骤，
    2、在classloader树中插入自己的Classloader
    3、替换ActivityThread的的Instrumentation对象和Handle CallBack对象，用来拦截组件的创建过程。
    4、利用反射修改成员变量。
    实际上如果提供特定父类给插件中的组建来继承的话，整个实现过程将会愉快很多。。。
    
    
  7、插件中Activity、service、receiver不在宿主manifest中注册即拥有完整生命周期的方法。
    
    由于Activity、service、receiver是系统组件，必须在manifest中注册才能被系统唤起并拥有完整生命周期。
    通过反射代理方式实现的实际是伪生命周期，并非完整生命周期。要实现面注册有2个方法。
    
    a、替换classloader。适用于所有�组件。
     App安装时，系统会扫描app的Manifest并缓存到一个xml中，activity启动时，系统会现在查找缓存的xml，
     如果查到了，再通过classLoad去load这个class，并构造一个activity实例。那么我们只需要将classload
     加载这个class的时候做一个简单的映射，让系统以为加载的是A class，而实际上加载的是B class，达到挂羊头买狗肉的效果，即可将预注册的Aclass替换为未注册的activity，从而实现插件中的Activity
     完全被系统接管，而拥有完整生命周期。其他组件同理。
    
     在PluginMain和PluginTest已经添加了这种实现方式的测试实例。
    
    b、替换Instrumention。
     这种方式仅适用于Activity。通过修改Instrumentation进行拦截，可以很好的利益Intent传递参数。
     如果是Receiver，利用Handler Callback进行拦截，也能很好的利用Intent传递参数。
     如果是Service，service在创建时是没有Intent的，只能通过Classloader在loadclass时进行拦截
     
    
  8、通过activity代理方式实现加载插件中的activity是如何实现的
  
     要实现这一点，同样是基于上述第4点，构造出插件的Context后，通过attachBaseContext的方式，
     替换代理Activiyt的context即可。
     另外还需要在获得插件Activity对象后，通过反射给Activity的attach()方法中attach的成员变量赋值。
     
     activity代理方式不建议使用，完全可以用上述第7点替代。
  
  9、插件编译问题。
  
     如果插件和宿主共享依赖库，常见的如supportv4，那么编译插件的时候不可将共享库编译到插件当中，
     包括共享库的代码以及R文件，只需在编译时添加到classpath中，且插件中如果要使用共享依赖库中的资源，
     需要使用共享库的R文件来进行引用。这几点在PluginTest示例工程中有体现。
     
  
  10、插件Fragment
    插件UI可通过fragment或者activity来实现
    
    如果是fragment实现的插件，又分为两种：
    1种是fragment运行在任意支持fragment的activity中，这种方式，在开发fragment的时候
    ，fragmeng中凡是要使用context的地方，都需要使用通过PluginLoader.getPluginContext()获取的context，
    那么这种fragment对其运行容器没有特殊要求
    
    另1种是，fragment运行在PluginCore提供的PluginSpecDisplayer中，这种方式，
    由于其运行容器PluginSpecDisplayer的Context已经被PluginLoader.getPluginContext获取的context替换，
    因此这种fragment的代码和普通非插件开发时开发的fragment的代码没有任何区别。
    
    
  11、插件主题
    轻松支持插件主题，重要实现原理仍然基于上述第2点。
  
# 需要注意的问题
  1、项目插件开发后，特别需要注意的是宿主程序混淆问题。宿主程序混淆后，可能会导致插件程序运行时出现classnotfound
  2、android gradle插件不支持public.xml中使用padding，在android Studio可能无法编译。可以使用eclipse。
  3、android sdk中的build tools版本较低时也午饭编译public.xml 文件。
  
联系作者：
  QQ：15871365851， 添加时请注明插件开发。
  Q群：207397154。
