android知识点总结：

concurrenthashmap、 hashmap、hashtable 的区别？

hashmap是非线程安全的，
hashtable是线程安全的， 效率比较低下，同时只能有一个写操作，
concurrenthashmap是线程安全的， 它采用的是锁分段机制， 而且一个表有16个桶， 同时可以有16个线程同时操作；

hashmap有个扩容因子， 默认是0.75， 当数量达到总数量＊扩容因子的时候，就会触发rehashing（重新扩容）。 hashmap默认大小为8。

要想提高查询效率，就要把扩容因子设置小点；
要想提高空间利用率， 就要把扩容因子设置大点；


object类的常见的方法都有哪些？
tostring（）、hashcode（）、equals（）等方法
finalize（）、clone（）、 wait（）、 notify（）、notifyall（）方法

linkedhashmap是hashmap和linkedlist的组合，是一个有序的hashmap， 每一个entry对象中有三个指针， 一个指向它前面的节点， 一个指向它后面的节点， 一个指向当hash冲突时，链接的下一个节点
应用场景就是LRU(最近最少使用)缓冲算法。


android最常见的设计模式都有哪些，其特点是什么？

1、单例模式
懒汉模式和饿汉模式
2、builder模式
主要是为了提高代码的可读性，
builder.age(32).name(“hack”).build();
3、观察者模式，是一种一对多的关系。 
RecycleView.addOnScrollListener()
Broadcast (register  unregister  sendbroadcast )
EventBus   ()
RxJava  

4、原型模式  通过拷贝对象来构造对象
OkHttpClient实现了Cloneable的clone方法，如果不实现这个的话， 要对某个对象拷贝一份的话， 需要先new一个对象，然后挨个赋值操作

5、策略模式
属性动画设置差值器
Volley源码中有一个RetryPolicy重试策略，就是用的策略模式。

6、工厂模式
策略模式和工厂模式类似， 一个关注行为（策略模式）， 一个关注对象（工厂模式）

7、代理模式（静态、动态）
mediapler源码中mediaplayerproxy就是对AwsenPlayer的应用

8、状态模式
蓝牙源码中用到的设计模式

9、ioc模式（控制反转模式）
di依赖注入
构造函数注入、属性注入和接口注入



二、android使用控件时候有哪些坑？
1、AsyncTask的队列中好像最多可以存储128条数据，超过了就会被废弃掉；
2、fragment中调用getactivity返回值可能会是空
3、包重复依赖的问题，解决方案如下（通过exclude group的方式）：

implementation 'com.github.ifmvo:Matthew_ImageLoader:1.1.3'
implementation ('com.github.CymChad:BaseRecyclerViewAdapterHelper:2.9.43-alpha1'){
    exclude group: 'com.android.support', module: 'support-annotations'
}
4、target升级时，权限问题， 当app打开后， 退到后台， 然后把权限关闭，app再次起来时， app崩溃的问题。
解决方案：在baseactivity的increase方法中判断如果是从后台起来的， 判断有没有权限， 没有，
第三方sdk升级

5、EasyPermission库的问题
集成的时候，默认没有权限的情况下，有个默认的弹框出现

6、列表页加载动画时候，内存不够崩溃的问题
解决方案：通过okhttp的setskipmemorycache(true).override(width,height)。
SimpleTarget来监听图片下载进度， 下完了就保存下来。
SparseArray来保存获取的drawable图片
什么时候用sparsearray，什么时候用hashmap？
数据量在千位以下时，用sparsarray， 上万后就要用hashmap了。

7、listview的setonitemclicklistener没有触发回调的问题
解决方案： 通过传入callback， 设置button的setonclicklistener方法， 然后调用callback的回传回来

8、fragment欠套3个fragment时，嵌套的fragment有时会莫名其妙不显示的问题


9、recycleview嵌套listview，listview显示不全的问题
需要重写listview的onmeasure方法

10、h5页面滑动卡顿的问题
解决方案：把硬件加速打开，属性如下：android:hardwareAccelerated="true"

10、service在app处于后台的情况下，无法启动app的问题
解决方案：把service换成job-service
jobservice和jobScheduler


11、alarmmanager最重要的特性就是在手机休眠的情况下可以唤醒apu继续工作

12、handler相关的消息：
https://www.cnblogs.com/xgjblog/p/5258947.html
looper中的messagequeue是用时间来排序的。 时间为当前时间＋延时


13、binder机理
涉及server、client、servicemanager  、binder驱动
servicemanager相当于是dns， binder驱动相当于是路由器， server为client提供服务， 但是相对servicemanager来说是客户端。 
servicemanager的binder通过0号引用获取，这样就导致所有的server都能访问到servicemanger。 server首先向servicemanager注册（key－value键值对，名称和binder对象）， 这样servicemanger中就有了该server的binder引用。 client通过名称在servicemanager 中获取到server的binder应用， 然后就可以获取server的服务了。 


binder相较传统的ipc机制来说，有哪些优势？
性能更好、和更安全
调用方式不同、注册方式也不同；
一个是通过getsystemserver
另一个是通过serverconnection传递一个binder代理对象， 而在onConnected中才可以进行的；

在哪里可以获取到view的高度？
a、onwindowfocuschanged（）
b、view。post（new runnable（）），利用idlehandler也可以
c、使用viewtreeobserver中的ongloballayout中可以获取，直接调用view的getwidth和getheight即可获取到view的宽和高

应用级别的server是没有办法注册到servicemanager中的，必须是系统级别的server才可以注册到servicemanager中；

sparseArray特点：
1、更节省内存， 存储的数据已经做了排序；
2、key值只能是int；

在android中，arraymap性能更高；

vivo push埋点点击比到达数据量高
原因：vivo推送后， 首条离线消息，客户端点击没有反应的问题。后来发现是被系统的智慧云给拦截掉了。 后来通过商务谈判添加白名单的方式解决。

三、趣味杂谈
如何让handler发送的消息发送到自己定义的Thread中？
在自定义的线程中new handler，把自己线程的looper对象发送给相应的handler即可。通过调用，非主线程默认是没有启动looper的。
looper.prepare();
new Handler(looper.mylooper());
looper.loop();


四、二手车项目架构升级：
1、刚开始时，用的是xutils架构
dbutuils、 viewutils、 bitmaputils 和 
后来被逐渐抛弃
数据库目前已经升级成room（google 给android量身打造的orm框架数据库）

什么是orm框架数据库，关系型数据库？
就是将对象和表做了关联， 不需要手动创建表，减少开发成本，提升开发效率；


五、城市选择页面处理逻辑
1、首先先判断网络是否畅通， 如果畅通， 先取出网络数据， 回来刷新页面， 并更新本地缓冲（数据库）；
如果不畅通， 则先取出缓冲数据， 如果有缓冲，取出缓冲数据刷新页面， 如果没有， 则取出本地assert中json文件数据；


http返回码总结：
200:成功
206:服务器成功返回了部分内容，请求部分数据，请求头添加range字段， 结合RangeAccessFile来实现
304:自从上次请求过后， 数据在没有更新，http_not_modify
503:服务器超时；
504:网关超时
404 not found



hashmap相关疑点问题总结：
hashmap中hash碰撞产生的根本原因是什么？难道仅仅是大家所说的hash值相同吗？hash值不同就不会产生hash碰撞？
  根据key的hash值算出来的索引相同，位于同一链表上的数据他们的hash值的后n位相同（如果hashmap的长度为2的n次方）
位于同一链表数据的hash值有什么相同点？
  位于同一链表的数据的后n位相同
发生hash碰撞的时候我们放入的新数据是位于链表头部还是尾部？
  是从头部插入的，代码如下：
void addEntry(int hash, K key, V value, int bucketIndex) {
    Entry<K,V> e = table[bucketIndex];
    table[bucketIndex] = new Entry<K,V>(hash, key, value, e);
    if (size++ >= threshold)
        resize(2 * table.length);
}

扩容的时候原数据到底是怎么重新放入新数组中的？难道还要挨个计算一下位置？如果不是，那么是通过什么方式来判断之前位于同一链表的数据是否还在同一链表？
  e.hash & (oldcapacity-1)  等价于 j | highBit

 推理如下：
 j | highBit
  = j | (e.hash & oldCapacity) 第一步
  = (e.hash & (oldCapacity-1)) | (e.hash & oldCapacity) 第二步
  = e.hash & ( (oldCapacity-1) | oldCapacity) 第三步
  = e.hash & (newCapacity- 1) 第四步


单里模式的创建有两种模式，懒汉模式和饿汉模式
懒汉模式：等用到的时候才去常见实力对象；
饿汉模式：类加载的时候就去创建，天生线程安全。已空间换取时间。


sharepreference特点：
跨进程不安全、读取慢、全量写入、卡顿（系统广播、或者onpause时落地到磁盘）

优化：微信开源的MMKV


contentprovider特点：
跨进程安全、适合大数据传输（里面用到了匿名共享内存和binder，binder传输的只是共享内存文件描述符）、 当数据比较小时， 使用共享内存可能就没那么划算了， 它里面有个call函数， 通过binder来传输数据

序列化：
对象序列化
serializable
性能较差（用到了大量的反射和递归调用）
parcelable
只会在内存中进行序列化，不需要通过反射进行序列化和反序列化。并不会存储到磁盘，性能要好， 
版本兼容问题
数据前后兼容性（数据顺序和类型变化）
serial
twitter开源库，结合了serializable和parcelabale两者的优点
序列化时间、反序列化时间、文件大小都要优

数据序列化
json
protocol buffers
flat buffer

数据库
realm、 levelDB、 WCDB
ORM框架(用面向对象的概念把表和对象关联起来)
greenDAO、 Room

优化索引：


数据库损坏的原因：
空间内存不足，数据设置生命周期为多少天， 超过天数后自动进行删除操作，如微信的聊天记录中的图片。 为了防止写操作时候内存不足的问题，可以提前预留足够的空间。每次空间倍数增长
系统突然断电（解决方案是每次写操作都要落地到磁盘）
文件sync同步失败（master表做主从备份， 为了防止主从表都被破坏，可以做双重备份，每次sync时，取老的备份表或则是损坏的表做备份）


内存优化的策略：
设备划分等级
bitmap优化
一般内存不足，是由于java对内存不足导致， 所以我们可以通过申请native的内存空间来达到较高的内存占用比。 还可以通过hprof工具来检测重复的图片和无用的图片资源
防止内存泄漏

sqlite数据库都有哪些锁？
SHARE(共享)  RESERVE（保留） PENDING（未决） EXCLUSIVE（排他）
多个读操作都可以同时拥有共享锁；
需要写操作时首先要获取 保留锁，可以开始数据库写入操作， 但是此时只能在内存中操作，不能入库，与此同时别的线程想要读数据库是可以的， 等需要入库的时候， 把锁升级成未决锁， 此时不能的新的读操作， 原有未读的操作可以继续操作。等所有读操作完成之后， 未决锁就可以升级成排他锁。此时开始入库操作。 别的线程既读、写操作均不可以执行了。

react native优化总结：
1、列表采用Flatlist，效率比listview更高效；
2、在componentshouldmount生命周期函数中判断数据有没有变化，有的话，才进行页面的刷新操作；
3、RN主要的性能瓶颈在javascript的执行，js是比较耗时，
4、js bundle进行拆分，分为框架bundle和业务bundle；
5、react native单实例共享；
6、jsmodule单实例加载后， 不要在重复加载；
7、app启动后，预加载rn实例；
8、框架兼容层，统一规则， 以便后续框架升级后还需要更改业务层方面的东西；

什么是jsx？
jsx是javascript语言的一种扩展，react native利用它来描述用户界面；


rtsp协议：（adobo公司的产品）
它包括OPTIONS、DESCRIBE、SETUP、 TEARDOWN、 PLAY等请求， 来控制视频播放的；

hls协议：
http:主要是负责数据的传输；
m3u8:主要是相关的索引信息， 包括一级m3u8文件和二级m3u8文件。其中一级中包括对应的多路流媒体信息， 二级中包括对应的流中包括多少个ts视频流信息；
ts对应的视频流地址

react－native学习心得

1）、left，right，top，bottom必须在绝对布局中才能使用， 且在项目中尽量少用绝对布局。 在做类似弹框那种的时候可以使用；
2）、如果父布局使用flexDirection:’row’的时候，子布局必须给设置width，否则子布局不会显示出来；
3）、做一个左对齐， 右对齐的布局
    <View flexDirection;’row’>
        <View width;20 height: 20 backgroundColor:’#987654’>
        <View width;20 height: 20 backgroundColor:’#987654’ flex:1>
        <View width;20 height: 20 backgroundColor:’#987654'>
    </View>

4）、flexDirection指定的方向为主轴， 与主轴垂直的方向就是次轴。
     alignItems控制子元素在次轴方向的布局， justifyContent控制子元素在主轴方向上的布局。
5）、如果父布局使用alignItems布局，子元素使用了alignSelf布局， 则父布局的alignItems属性对子元素没有作用；


WMRouter有哪些作用：
1、通过uri方式借助wmrouter进行页面跳转；
2、数据路由通过wmrouter的spi封装通过接口实现分离的方式实现；
3、使用UriInterceptor进行协议拦截，然后实现跳转；

volatile、transient关键字的作用：
transient关键字修饰的字段不会被序列化， 比如敏感字段，如银行卡号和密码不在网络中传输。生命周期只会被写入内存中， 不会被写入磁盘中持久化；

为什么要升级到target26？而不是别的？
应用市场给在某天之前，target必须升级到26，否则可能面临被下架的风险；
通过反射调用sdk的方法可能会被报错；


target升级到26（0版本）对应的一些事？
1）android8.0引入了通知渠道， 要为每种通知类型创建用户自定义的渠道；
2）android8.0强化了权限管理，变得更加安全；
3）android8.0粘性广播收不到的问题，改为动态注册即可解决这个问题；

app签名文件都包含什么内容？
appalias；
apppassword；

ThreadLocal类的作用：
他就是为线程中的某个变量创建一个副本，相当于每个线程中都有这么一个变量，相当于是提高了性能，但是额外占用了空间。如果所有线程共同维护一份的话，需要上锁，多线程操作效率低下。

mvp的缺陷都有哪些？
最明显的创建一个Activity需要配合创建多个接口类和实现类，每个操作都需要通过接口回调的方式进行，虽然逻辑清晰代码，同时也造成了类的增多和代码量的加大。
 
解决方案：
    利用泛型封装一下MVP的base类，通过泛型所提供的类型去实例化View层和Presenter层，在继承封装好的基类中快速的使用MVP模式。注意的是通过泛型约束后，在继承的时候需要填写多个泛型值。


插件化学习总结：
解决的问题如下：
1、加载插件中的类：
app安装后， 会在／data目录下生成安装的apk的信息。 他在内存中保存在了loadapk中。由于插件没有被安装， 所以无法加载插件中的类和资源。 而加载类用到了loadapk中的basedexclassloader， 有dexclassloader和patahclassloader两种。pathclassloader加载系统和主dex包中的类， dexclassloader加载剩余的dex文件。所以我们有两种方案：
一种是生成插件的loadapk，通过反射把他放在系统mpackage对象中，但是loadapk中有很多东西比如applicationinfo，就是通过读取androidmanifest.xml来生成的。但是这样处理逻辑很复杂， 像360的droidplugin就是通过这种策略来实现的。
另一种是通过生成插件中classloader对象，加载插件中dex的dexelement然后把他合并到主app的classloader中。然后就如下：
basedexclassloader->dexpathlist->dexelements [Element]， 获取到插件中的dexelements，然后插入到dexpathlist中。

2、启动插件中activity：
首先我们新建一个占坑的activity，因为启动activity在ams中有个判断，启动的activity必须在android manifest.xml文件中声明过，否则会报activity未申明。 所以实现的思路是在检查前，把他替换成占坑activity， 然后等到真正创建时，再替换成原来的activity。思路如下：
activity启动流程， ActivityManagerNative->gdefault.它实际上是ams的一个本地代理。我们只需要设置它就可以了。 完了之后，它会把消息放到activity什么

handler下发消息的时候，先判断handler的mcallback是否为空， 如果不为空的话， 就会调用mcallback.handlemessage()方法。 mcallback本身是个handler类型。
ActivityThread中有个H（handler） ，然后通过反射重写那个h的mcallback。即可。 在重写的时候

管道pipe＋ epoll 结合， 通过epoll来监听管道的读端， 看看管道的写端有没有数据写入，如果有数据写入， 管道的读端就会被唤醒。

3、加载插件中的资源
contextimpl－》resourcemanager－》 assertmanger－》addassertpath，，然后创建一个用assertmanager来创建resource。 所以我们的方法， 通过反射将我们的插件apk的路径调用addassertmanager方法插入到assertmanger中。

现在主流的网络请求中为什么要用retrofit＋rxjava组合来进行请求的？
因为rxjava可以做到随意切换线程（数据处理在io线程中， 页面刷新在主线程中）；

2、实现一个系统，上报程序的崩溃log？
实现思路： 如何获取程序的崩溃log？
每一个没有捕获的异常数据都能触发setUncaughtExceptionHandler，所以要实现一个这种handler，然后set进去即可。 

3、789123456这么一组数据（已经排好序的一个数组， 然后右移， 把移出来的数据放到最左边，相当于是一个循环数组），怎么实现一种查找算法，实现时间复杂度log(n)?
实际上也是利用了二分查找算法。只是查找规律。采用的是递归思想实现。
二分查找的算法核心思想是每做一次运算能够使得数据量减半。 能够命中哪块区域。仔细分析上述数据，经过折半分拆数据后，都能保证有一边的数据是已经排好序的数据。 这样就能判断所查找的数据落在哪块区域了。

public int search(int []arr, int left, int right, int value) {
  int middle = (left+right)/2;  //这块可能导致数据溢出，最好使用left ＋ （high － left） >> 1; 位操作效率更高
  
  if(value == arr[middle]) return middle;
  if(left >= right) return -1;//没有找到；

  if(arr[middle] > arr[left]) { //左边是升序
     if(value >= arr[left] && value <= arr[middle]) {
       return search(arr, left, middle, value);
     } else {
       return search(arr, middle+1, right, value);
     }
  } else { //右边是升序
    if(value >= arr[middle] && value <= arr[right]) {
      return search(arr, middle, right, value);
    } else {
      return search(arr, left, middle-1, value);
    }
  }
}

数据类加密？比如密码的加密
做双层加密， 先做一层md5不可逆的加密， 然后做一次可逆的加密。 传输的过程中就是传递的二次加密后的数据， 服务端拿到二次加密后的数据， 再做一次解密，然后把解密后的数据保存到数据库。 这样在传输过程中即使数据被破解，也拿不到用户的密码。因为他是经过不可逆加密后的密文。
一般使用的可逆加密算法有对称加密和非对称加密。

对称密钥，既可以进行加密， 也可以进行解密；

数据加密，一般情况下， 对数据和密文一起做加密算法， 服务端拿到后，通过相同密钥的逆序排序算法进行解密，才能拿到明文。

常见的异常都有哪些？
NullPointException, ClassNotFoundException, ClassCaseException,  ArrayOutOfIndexException,  UnkownException, SecurityException,  算术异常（0作为被除数）等。


android7.0开启照相功能崩溃的问题？
原来Android7.0系统开始，直接使用本地真实路径的uri被认为是不安全的，会抛出FileUriExposedException异常，在郭霖老师的第一行代码中提到了用FileProvider来解决这个问题。 


target从23升级到26需要处理的事情？
1、广播接收不到的问题；
     需要动态注册，不能采用在androidmanifest中注册的方式；
     主要涉及三个广播， 网络状态切换广播、拍照广播和录像广播
2、通知显示不出来的问题；
     需要添加渠道；
3、8.0手机上安装失败的问题；
     需要添加权限 
     <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES"/>
4、android7.0获取以content开头的文件拿不到正确路径的问题；
5、sharepreference中不能使用MODE_WORLD_READABLE\  MODE_WORLD_WRITEABLE,被认为是不安全的；

share preference中apply是把数据写到内存中，commit是把内存中的数据写到磁盘中；


java反射小结
class里面有属性filed域，方法method域；
反射可以修改类里面的属性和方法，但是不可以基本类型的常量值。因为编译器在生成class文件的时候做过优化， 已经变后面使用常量的地方优化成了常量。所以即使你修改了需改了常量本身， 但是在使用的时候， 但是在代码使用中还是原来的常量。
你也可以这么想，反射肯定可以修改常量的值，但是修改后的值是否会有意义？？？不见得。

android系统进程都有哪些？
init进程 init。rc中配置
zygote进程、systemserver进程、mediaserver进程

悬浮窗口SYSTEM_ALERT_WINDOW 权限没有办法使用代码来申请， android6.0的时候，需要你先判断是不是已经开启这个权限， 如果没有开启， 则发送action挑战到settting页面打开该权限。 在onactivityresult中接收结果。

javaVM,  JniEnv作为实现多类的结合体；

快速排序算法如下：
void quickSort(int a[], int low, int high) {
  int start = low;
  int end = high;
  int key = a[start];

  if(low >= high) return;

  while(start != end) {
    while(start < end && a[end] >= key) {
      end--;
    }

    if(start < end) {
      a[start] = a[end];
    }

    while(start < end && a[start] <= key ) {
      start++;
    }

    if(start < end) {
      a[end] = a[start];
    }
  }

  a[start] = key;
  quickSort(a, low, start-1); //前半部分进行排序
  quickSort(a, start+1, high); //后半部分进行排序
}

int main(int argc, char **argv) {
  int a[] = {4,3,6,8,1,2,9,6,5,2};
  quickSort(a,0,9);

  for(int i = 0; i < 9; i++) {
    printf("%d\n", a[i]);
  }
  return 0;
}


性能监控小程序：Emmagee
地址：https://blog.csdn.net/u011159607/article/details/78143808

懒汉式实现单例类

public final Singleton {
    private Singleton {}
    private static class SingletonHolder {
        public final Singleton = new Singleton();
    }
    public static Singleton getInstance() {
        return SingletonHolder.singleton;
    }
}

public final Singleton{ ／／final为了让这个类不能被继承，防止集成类修改singleton
    private volatile static singleton = null;
    private Singleton { ／／构造函数设置成private类型， 让外部不能调用构造函数产生实体类，只能通过调用getInstance方法来实现一个构造函数；；

    }
    public static Singleton getIns() {
        if(null == singleton) {
            Synchronized(Singleton.class) {
                if(null == singleton) {
                    singleton = new Singleton(); ／／构造函数本身是需要耗时的，
                }
            }
        }
        return singleton;
    }
}
对于上面的来说，setAccessible（）值为true，表示取消访问权限限制。也就是可以访问它的一些私有方法。实例如下：
         Class clazz = SingletonTest.class;
         /*以下调用无参的、私有构造函数*/   
         Constructor c0=  clazz.getDeclaredConstructor(); 
         c0.setAccessible(true); 
         SingletonTest po=(SingletonTest)c0.newInstance();
         System.out.println("无参构造函数\t"+po); 如下：

volatile关键字能保证操作的操作的有序性，但不能保证操作的原子性，所以适合的场景是一个线程写，多个线程读。
缓冲一致性协议就是表示当一个线程修改一个变量的时候，如果发现这个变量是共享变量，就通知其他cpu，这个缓冲变量实效。 所以当其他线程操作该变量的时候，需要从主存中读取的。


涉及到主内存和工作内存中的数据同步，如果不添加valatile关键字修饰的话，没有办法保证数据及时同步
//加入volatile就是为了防止重排序，一个对象的创建过程包括如下几个步骤：
1.分配对象的内存空间
2.初始化对象
3.设置instance指向刚分配的内存地址
当instance指向分配地址时，instance不为空
但是，2、3步之间，可能会被重排序，造成创建对象顺序变为1-3-2.所以需要加入volatile关键字防止重排序

编译器为了提高效率，做了指令重排操作。所以不保证cpu执行的顺序和代码本来的顺序是一致的。 但。是他会保证执行结果是一致的。
要想并发程序正确的执行，必须保证原子性、可见性和有序性。
synchronized和lock保证了原子性， volatile保证了有序性和可见性。volatile禁止指令重排，保证有序性。


饿汉式实现，天生线程安全
public final Singleton {
    private static final Singleton mIns = new Singleton();
    private Singleton() {}
    public static Singleton getIns() {
        return mIns;
    }
}

volatile关键字的作用：
保证数据的有序性和可见性；

synchronized关键字的作用：
保证数据的线程互斥

线程的同步一般情况下要如何实现： 一般linux下用信号量来实现， signal
android上用wait和notify来实现。

子线程拥有主线程的资源， 除此之外， 子线程还有自己的栈区域， 在子线程内部创建的一些全局变量是私有的， 主线程是没有办法访问到的。 

对排序设计的步骤：
说明：堆是一个完全二叉树
1、建堆和排序，建堆完毕后， 整个数成这样的结构， 父节点均大于等于或小于等于子节点；

主要涉及的步骤有：插入一个元素， 删除堆顶元素；
插入一个元素：先插入堆的尾部，然后和父节点比较， 比父节点大， 则和父节点交换。 否则结束。
删除堆顶元素：删除后，把堆的最后一个元素放入堆顶， 然后和堆顶的自节点比较， 找出插入点，然后交换。

快速排序和堆排序的事件复杂度都是O(nLogn)
建堆完毕后， 就是排序了。 步骤如下：
1、堆顶和堆尾的数据进行交换；
2、堆顶元素和子节点比较，然后插入位置，进行交换即可类似上面的删除堆顶元素的操作步骤；

堆排序不用占用额外的内存空间。只是排完序后就会就成有序的了。 

应用场景：
优先级队列、求取top k、 求取中位数
求取topk的实现思路， 求n的数据中取出前k的数据，初始化一个小顶堆， 然后再依次取出后面的n－k个数据，如果取出的数据比堆顶数据大，则删除堆顶数据，然后把数据入堆操作。 依次执行完所有的数据， 此时就是前k个数据；

求取中位数实现思路， 先把数据分成两等份，实例化两个堆，一个大顶堆，一个小顶堆，小顶堆的数据都要大于大顶堆中数据， 此时中位数就是大顶堆或小顶堆中的堆顶数据。 然后当新增数据后， 把新增的数据移动到大顶堆或小顶堆， 然后再把数据做转移， 保证大小堆中的数据相等，或大顶堆中的数据比小顶堆中的数据＋1即可。然后就获取到了中位数。

插件化问题总结：
解决的问题，类的加载，资源的加载
类的加载： 新建一个插件apk的classloader， 取出其中的dexelements， 然后通过hook技术取出宿主程序的classloader中的dexelements文件，然后新建一个新的dexelement， 把插件和宿主程序的dexelements合并进去。 通过反射setfield来来设置pathdexlist。这样宿主程序就可以访问插件中的类文件了。 

如何解决插件中的activity的调用问题呢？ 创建一个站桩activity， 在ams检验之前，  把插件中的activity设置成站桩activity，同时保留插件activity的信息，在ams检查之后，再把插件activity替换出来。 那这么样实现替换工作呢？

在启动activity的时候， 要先向ams作报告， 通过ams的本地代理对象实际上是ActivityManagerNative.gdefault对象， 他是一个代理对象，也是一个单例对象， 通过动态代理技术重新设置这个代理对象。在invocationhandler的invoke中做调包工作。 然后ams会通过ActivityThread创建主线程looper，并启动looper循环。 ActivityThread会向自己的内部类Hhander中发送消息。 我们通过hook，来设置h hander的mcallback对象， 来还原插件activity的请求的。 这样我们的插件actiivty就能够正常启动了。  

如何识别启动的activity是插件activity，通过在intent中插件key值来标识。

如何加载插件中资源呢？ 通过查看源码， 他是在contextimpl类中初始化的， 通过ResourceManager, AssertManager, 通过调用assertmanager中的addassertpath来初始化resource对象。 所以我们需要新建一个assermanger对象， 通过它来获取插件中的resource资源。 这样我们在宿主程序中提供接口， 来获取插件中的resource资源。 插件中就能正常获取插件的resource资源了。 

anr是如何产生的？
service超时、 broadcast广播超时、 contentprovider超时。  ui事件分发消息超时。  因为所有所以都是在主线程中处理的。 而每一条消息，必须在指定时间内处理完毕， 否则酒会出发appnotresponse函数， 弹出anr弹框。
怎么实现的呢？ 每次发送一条消息的时候， 都会通过一个handler发送一条延时消息，消息执行完毕后， 把这条延时消息remove掉，如果在指定时间内没有被执行完毕， 则会出发appnotresponse函数。弹出anr弹框；

ui事件传递：
事件分发、事件拦截和事件处理
事件分发是通过dispachtouchevent处理
事件拦截是通过onintercepttouchevent处理，事件拦截只有viewgrounp类才有， view和activity都没有。 
事件处理是通过ontouchevent处理

其中viewgroup的dispacherevent中会先调用onintercepttouchevent函数判断事件是否被拦截， 如果拦截， 则event不继续往下传， 调用他的ontouchevent来处理消息；
然而view的dispatherevent直接就调用了view的ontouchevent中去了。

先说view没有调用setonclicklistener函数，touch有MotionEvent_down  MotionEvent_move  MotionEvent_up， 如果down时间返回true， 才会执行move和up。 如果down返回了false，则消息继续往上传递。

如果view设置了setonclicklister函数， down返回了true， move和up都会执行。 但是onclick不会被执行。 如果down返回了false， 则move和up和onclick都会被执行。

静态广播和动态广播：
动态广播的优先级要比静态广播的优先级要高， 肯定是动态广播先执行。 

如何优化布局：（主要就是减少view的层级）
采用viewstud加include
采用merge
采用约束布局corresslayout


项目中都做了哪些优化？
1、 启动页做了优化， 延时加载， 就是通过这个idlehandler来实现的， 通过返回值等于false表示只执行一次， 返回true就表示可以无限次数的执行；
2、 首页数据做了缓冲，本地file和sp缓冲；
3、 详情页做了预加载；
4、 详情页做了接口拆分，领先借口和别的接口；
5、 页面无网和无数据默认图做了统一的sdk来处理；支持actiivty和fragment；
6、 数据埋点做了优化，加了缓冲埋点， 条数到达多少条后才缓冲，减少网络请求次数；
7、 title栏做了统一， 所有页面用同一个view；
8、 push推送做了统一处理， 外层统一封装一层借口， 使得外部调用统一，即使将来添加新的第三方推送的sdk，客户端修改的工作量不会太大；
9、 组件化架构升级， 降低藕合度。 模块间跳转以及数据获取通过接口来实现； 定义一个单例类， 通过hashmap来保存所有的接口和实现类， 而在每个moudle中把这个单例类传进去， 这样所有的模块就都可以访问到这些实现以及他们的接口方法了；
map<string, IPlugin> key值是通过tag和借口类class生成的；
10、通过加入aspectj 面向切面编程技术解决类似登陆相关的问题； 在连接点函数before之前加入函数判断来说明函数的解析；
11、通过自定义本地unCaughtExceptionHandler， 通过调用setUnCaughtExceptionHandler来拦截异常，并保存到本地文件夹，方便后续拿到log日志来定位问题；主要是在测试阶段使用。
12、dplink制定统一的协议， 来解析web请求；
13、sp文件进行拆分， 尽量不要sp文件太大， 慎用sp。commit和sp。applay，commit是写入磁盘， apply是写入内存，多用apply，少用commit；
14、规范线程安全的实例对象写法，埋点sdk在加入rn线程后出现崩溃问题；

如何实现一个线程安全的map类
1、map对象申明成volatile类型；
2、get和put方法添加synchronized关键字进行上锁；

http和https的区别：
http是基于tcp传输的，主要是他传递的数据量比较大， 另外它是一种无状态的连接；
https是http＋ ssl／tls协议， ssl底层是基于tcp／ip的， 做了数据封装、 压缩和解密的功能。 使用https要向ca申请证书。 而且要进行ssl握手操作，  所以耗时比较长。 

ssl协议有两层， 一层使ssl记录协议， 是建立在tcp协议基础上，做数据的压缩和加密和解密工作。 一层时ssl握手协议，主要是在通讯之前， 对双方身份进行认证， 以及协商加密算法和交换加密密钥。

1、https要向ca申请证书
2、网络请求端口不一样， http是80，https是443
3、http是超文本传输协议，传输的是明文， 而https是具有安全性的ssl加密传输协议；
4、https握手阶段比较耗时， 所以加载比较长；


ssl是如何保证数据的传输安全的？
1、客户端向服务器发出ssl连接请求；
2、服务端会把公钥发给客户端， 并保留了唯一的私钥在服务器端， 客户端用公钥对双方通信的对称密钥进行加密， 并发给服务端；
3、服务端用唯一的私钥对客户端发来的对称密钥进行解密；
4、在传输过程中， 服务端和客户端用共有的对称密钥进行加密和解密；
另外https也不是一种绝对安全的传输协议， 安全要依赖于ca证书， 如果ca证书被篡改了（手机被root后，就有可能被篡改）， 这样的话， 就不能保证安全了。 


步骤更新，更全面：
1、首先客户端从服务器发起申请ssl公钥的请求；
2、服务端会返回ssl的公钥，以及让经过ca证书的私钥加密过的公钥密文；
3、客户端收到数据后，用本地ca证书的公钥进行解密， 和传递过来的ssl公钥进行比对，是不是一样（实际比对用的是他的hash值，这样效率会更高），主要做身份认证；
4、身份认证通过后， 客户端在本地生成一个对称密钥， 然后拿这个公钥进行加密， 传递给服务端；
5、服务端接收到加密密文后， 用私钥进行解密， 获得对称密钥；
6、以后服务端和客户端进行数据传输时， 都是传递的对称密钥加密后的数据， 因为彼此都有对称密钥，都可以对其解密操作；

上面第2步骤中的， 公钥密文， 是服务端向ca证书发动请求生成的， 身份证认证信息可以了。 

handler问题相关总结：
1、messagequeue中的消息是基于什么进行排序的？
2、时间是发送消息时间还是处理消息时间？
3、它是如何保证消息是在指定时间被执行的， 当前时间和消息的when时间做比对，如果比当前时间晚， 则把超时时间设置成二者的差值， 然后在差值时间后就会收到next消息就会被返回？
4、message时如何管理的？
5、looper。loop的while（1）死循环， 主线程为什么没有死掉？

二分查找变种的问题：
已经排序的数组中数据是有重复数据的， 比如查找第一个等于value的数据， 最后一个等于value的数据， 第一个大于等于value的数据， 最后一个大于等于value的数据等。

AsyncTask在android23里面， 默认是串行执行的， 就是一个任务执行完了后， 才会执行下一个任务， 若要解决这个问题， 需要重写自己的executor即可。 AsyncTask中有个executor，它是一个THREAD_POOL_EXECUATOR， 我们通过调用task.executeOnExecutor(executor)，就可以实现多任务并发了。 不过最好还是使用默认的， 除非不得已再使用那个了。
一个asynctask任务被创建后， 只能执行一次任务。 如果启动asynctask， 当前task不是处于pending状态， 就会直接抛出异常。 默认只有三种状态， pending、 running和finish状态。
一个asynctask被创建后， 只有等这个任务执行完毕了， 才会被销毁这个asynctask这个对象。 里面有个threadpoolexecutor对象， 管理者所有的asynctask对象。


23种涉及模式：
创建型：
结构型：代理模式（对实体类做一些条件判断）， 桥接模式（比如底层有种三种数据库实现mysql， sqlite等）
适配器模式， 装饰者模式（保留目标的对象， 处理前后持有该对象），  
行为型： 


target26升级出现的问题：
悬浮窗升级后，不显示的问题， 默认类型是：TYPE_SYSTEM_ALERT， 修改成TYPE_APPLICATION_OVERLAY即可。  替换成dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));


any_rtc商业级的实时直播框架


bindservice和startservice的区别：
1、调用的生命周期方法不同；
oncreate  onstartcommend ondestory
oncreata  onbind
2、startservice启动的service， 必须调用stopservice才会退出，  而bindservice启动的service，退出有两种呢方案， 一种使unbindservice，一种是启动service的actiivty退出；

有了thread，为什么还要用service呢？
一般service里面会启动一个thread执行耗时操作， 这样的话， 方便管理这个线程，  在任何地方都可以调用stopservice来结束这个线程操作。

ffplay的线程模型是什么？

measure的四种测量模式都有什么？
unspecified, （无限制）父容器对子容器大小没有限制，要多大给多大
at_most：（最大模式）父容器限制了子容器的大小，最大为多少
exactly：（精准模式）父容器明确知道子容器的大小， 确切值
对于view来说
exactly精准模式就对应于match_parent
at_most最大模式就对应于wrap_content
unspecified无限制表示没有限制模式

mesurespec是一个int整形32位表示的int数字， 前两位表示mode， 后30位表示大小
int measureSpec=MeasureSpec.makeMeasureSpec(size, mode)，通过size和mode生成新的measureSpec
setMeasuredDimension设置view的长宽

委托模式：有两个对象参与处理同一个请求， 接受请求的对象将请求委托给另一个对象来处理。

http的缓冲涉及到的字段：
expires：缓冲有效期
cache－control：是否要缓冲的字段
last－modified：上一次修改缓冲数据时间
etag：缓冲版本
data：
if－modified－since：后面跟一个时间， 判断这个时间之后服务器端的数据有没有变更过
if－none－match： 判断有没有缓冲被命中

LinearGradient线性渐变的效果类

 普通广播和有序广播
 有序广播：
 有序广播的接收者们将按照事先申明的优先级依次接收，数越大优先级越高（取值范围：-1000~10000），优先级可以声明在<intent-filter android:priority="n".../>，也可以调用IntentFilter对象的setPriority设置。并且接收者可以终止传播（调用abortBroadcast()方法即可终止），一旦终止后面接收者就无法接受广播。另外，接收者可以将处理结果存入数据（可通过setResultExtras(Bundle)方法将数据存入Broadcast），当做Broadcast再传递给下一级接收者（可通过代码Bundle bundle = getResultExtras(true)获取上一级传递过来的数据）。

 setResultExtras
　　短信拦截原理：系统收到短信，发出的Broadcast属于有序广播，程序就可以通过设定优先级先接收到通知，然后终止传递。

startservice和bindservice，多次调用startservice和bindservice，会出现几个service的实例 

broadcastreceiver的onreceiver方法中创建一个线程， 有可能导致线程和广播的内存泄漏。
线程没有退出，导致线程的内存泄漏，
由于线程内部类持有外部类的应用， 导致receiver也内存泄漏，
sendOrderedBroadcast是发送有序广播

线程start之后，必须调用stop才能使线程退出。

[server端返回]last－modify－》 if－modify－since[客户端请求header添加字段]
[server端返回]etag－》 if－none－match[客户端请求header添加字段]


okhttp拦截链
1、重定向连接器， retryandfllowupinterceptor， 创建了streamallocation
2、桥接拦截器：bridgetinterceptor，设置内存长度， 编码方式， 请求头， gzip压缩， cookie
3、缓存拦截器；cacheinterceptor， 添加缓存逻辑处理， 就是让下一次网络请求节省更多时间，加快页面展示速度
4、连接拦截器：connectinterceptor， 创建网络连接
5、callserverinterceptor， 执行真正的网络请求


http返回码：
2xx：成功
3xx：重定向
4xx：客户端错误
5xx：服务器错误

java中的数据结构都有哪些分类？

java中都有哪些数据结构实现了collection这个接口？

java对堆内存分代策略？如何自己写一个垃圾回收算法？

java中工作内存中数据是存放在ram中的吗？
不是的，是存放在寄存器或高速缓冲中的

如何判断一个链表是一个环状链表？

sp.applay和sp.commit的区别？

activity在onstart中执行finish操作， 后续会走哪些生命周期函数， 为什么？

现在你刚接手了一个新项目，项目中存在内存泄漏，如何在短时间内查出在哪块有内存泄漏？
加入leakcanary
3.0以前用android monitor， 3.0以后用android profiler工具

现在接手一个新项目， 如何搭建项目框架？

垃圾回收算法：
首先垃圾回收，90%区域在堆内存，10%区域在方法区。堆区又按照对象的存活生命时长， 分为新生代和老年代区域。 垃圾回收时，优先回收新生代中对象， 所以新生代中的对象都有朝生夕死的特性， 新生代中采用的垃圾回收算法是复制算法。  而老年代中采用的垃圾回收算法是标记－整理算法；
标记－清除算法
效率低下，容易造成大量的垃圾碎片，申明不到足够的内存空间，再次触发gc垃圾回收

复制算法（新生代垃圾回收算法）
空间被分成eden和survivor from和survivor to区域， 按照比例8:1:1来划分的；

分代搜集算法：
根据新生代和老年代， 分别使用不同的垃圾回收算法，新生代采用复制算法， 老年代使用标记整理算法

永生代，指的是操作系统层面的申请的内存空间


标记－整理算法（老年代垃圾回收算法）

垃圾回收算法链接：https://blog.csdn.net/weixin_40739833/article/details/80717638

java内存模型中，有主存和工作内存之分：
主存是放在ram中的
工作内存是放在cpu寄存器和高速缓冲区中的，解决内存读取速度和cpu计算速度性能相差较大的问题

为什么要这么设计， 主要是由于cpu计算速度和从内存中读取速度有差异导致的。 而为了降低两者之间的速度差异， 加入了工作内存的概念。

有一组无序数组， 如何实现一种算法， 提取第k大的数据？
用快速排序，每次折半，查看碰面的数据的下标是不是为k，是就是表明找到了， 不是再折半区间，继续查找，知道找到为止；

java中有哪些集合类接口？
collection、 set、 list和map，其中set和list实现了collection接口， collections算法提供了对集合进行排序和遍历的多种算法实现
Collections.sort(list)，对list进行排序
Collections.max(list)，求取list中对最大元素值
Collections.min(list)，求取list中的最小元素值
Collections.binarySearch(list, "value")，查处list中值为value的元素的下标
Collections.replaceAll(list, "aaa", "bbb")，把list中值为aaa的元素全部替换成bbb
Collections.reserve(list)，把list做反转操作

java中线程安全的集合类都有哪些？
stringbuffer、 vector和hashtable

java中常见的数据结构都有哪些，最好按类来分？
collection  
  list－》arraylist、linkedlist和vector
  set－》 hashset和treeset
map－》hashmap和treemap

最好不要在广播接收中创建创建子线程来执行耗时操作， 因为广播接收者的生命周期比较短暂， 当收到广播后，才会被激活， 当执行完onreceiver后就会结束掉， 此时当activity退出了， receiver结束了， 此时进程就是一个空进程， 就有可能被系统回收掉。

一个activity按home键处于后台任务的情况下， 会不会被系统回收掉？
有可能会被回收掉的，因为按home键后，进程的优先级会从前台进程变成后台进程，在系统内存不足时，触发lmk机制，app被杀死，进而内存被回收；

一个service如何实现保活？

面向对象要遵循的设计原则：
单一原则
开闭原则， 对修改关闭， 对扩展开放
接口隔离原则， 尽量把接口做拆分， 不能搞一个大的接口
依赖倒置原则， 抽象不依赖于具体， 具体依赖于抽象
合成复用原则， 尽量使用组合， 而不要通过继承来复用 
迪卡特原则， 一个实体类尽量不要和其他实体类相互依赖
里氏替换原则， 一个基类替换成子类后不会有任何错误


进程保活都有哪些措施？
1、手机启动接受广播，启动service；
2、双进程实现保护机制，其中一个是守护进程；
3、通过startcommand函数中，返回进程被杀死时，再次启动的问题；
4、可能用到alarmmanager；

ii：把service设置成前台进程；
ii：启动高度为1像素的notification；
ii：循环播放一个没有声音的音频数据；


lmk的low memory killer机制？
主要是根据进程的oom_adj的值来判断进程的优先级， 值越小， 越重要， 被杀死的优先级越低；

如何实现进程的保活呢？
调用startForegound， 让你service所在的进程升级为前台进程；
在service的onstop方法中重启改service；
将service设置成，被杀死后，重启改service；
双进程相互监听保护，
进程杀死后，如何复原？没有什么好的办法，通过数据库保存现场数据。 每次启动后，判断应用程序是不是被意外杀死，如若被意外杀死，则从数据库读取复原现场；

app被杀死后，如何保留原油数据？


热修复的其他实现方案：
因为应用进程是被zygote进程启动起来的，可以通过hook技术hook app_process这个进程。 通过aop面向切面编程技术来实现的。 
通过hook技术修改basedexclassloader中的dexpathlist这个属性。 把修复后的dex包插入到dexpathlist数组的最前面。
通过addassertpath得到插件apk的assertmanager，通过getresource得到其资源。

一些棘手问题？
1、埋点sdk多进程数据错乱的问题；
2、target权限升级，用户允许权限后，切换到后台，然后用户手动关闭权限后再次打开app后崩溃的问题；
3、列表页添加vr动画后，内存oom的问题；
4、动画相关的问题；

jni一些实战：
jclass jc ＝ env->findClass("com/demo/Demo");
jobject jo = env->allocObject(jc);
jmethodid mid = env->GetMethodId(jc, "append", "(LJava/lang/String;I)LJava/lang/String")";
JString str = env->callObjectMethod(jo, jmethodid, "", 12);

系统新特点：
6.0的动态权限功能
7.0的分屏功能
8.0的画中画、后台服务受限、广播限制
9.0的刘海设计、material design、全部使用https、使用黑白模式切换、加入护眼模式

gc root节点是如何选择的？
方法区中的静态变量、常量；
栈中的本地方法列表，局部变量生命周期随着函数的结束就结束了；

如何判断两个链表是否相交？
两个链表同时入栈，判断栈顶的元素是否一样，一样则说明相交；
同时遍历两个链表到尾部，判断尾部是否一样，一样则说明相交；
最好就是遍历尾部节点，如果一样则相交

如何判断一个链表是否有环？
穷举法：依次遍历，每取出一个，就和前面的元素做判断，是否存在？存在说明有环。否则无环？
快慢指针法：
set集合：每次遍历数据后，插入集合，判断集合大小，如果不加1了，就说明有环；

android中intent传递数据超过1m的时候，就会报异常

powermanager的wakelock（PARTIAL_WAKE_LOCK类型），获取锁之后，在释放锁之前，cpu会一直处于唤醒状态；
alarmmanager做一些定时任务；
WakefulBroadcastReceiver结合intentservice，会在启动intentservice的时候持有锁，执行完毕后，释放锁，所以保证了在程序执行完前；
jobschedule来做一些延时任务。在指定的条件下

查看当前正在运行的activity的命令？
adb shell dumpsys activity activities | grep "Run"

onpause和onstop的区别？
当a调起b时，会调用a的onpause方法，onstop表示只有某一个activity为不可见的时候，才会被调用到。当一个actiivty的theme被设置成dialog时候，onstop不会被调用到，因为activity不会变成不可见状态。 再者，app被lmk杀死的情况下，onstop也有可能不会被调用到。。。

android属性动画小结？（valueanimator和objectanimator）
为什么要使用属性动画：
传统帧动画和补间动画不能满足复杂动画的需求；
只是想通过改变属性来达到动画的效果；

补间动画会改变view的属性值
原理：
1、设置初始状态和结束状态；
2、给定变化趋势（插值器和估值器）；
3、每次变化后通过调用invaluate（）来重新绘制view，达到刷新的目的；

插值器（实现interpolator接口）和估值器（实现typeevaluate接口）
插值器使用系统的一般就可以了；
估值器需要自定义实现；比如抛物线动画；

如何获取view的宽高？
1、onWindowFocusChanged；
2、监听viewtree是否绘制完成；
3、通过idlehandler来监听；

热修复方案：
1、qqzone的dex插桩，存在的问题是插桩带来的性能问题，以及类被打上了isprefied标签导致运行报错；
2、美团rebost热插拔方案，在编译阶段，为每个类打入了changequickredirect字段， 而后又为这个类的每个方法入口添加代码，以便做热修复操作；
3、腾讯tinker，服务器做dex插值， 自己写dexdiff和dexmerge算法， 把原有包中的有问题的类的定义删除，这样就可以只能加载到热修复的包中的方法了；
4、dexposed和andfix方案

为什么内部类使用的外部变量都必须使用final修饰符？
主要是因为内部类的生命周期和外部变量的生命周期不一致导致的；

recycleview内存优化，需要考虑哪些问题？
要充分利用recycleview的缓冲机制

包体积优化？
lint unuseresource 无用资源优化
使用一套布局文件，资源文件xhdpi
tinypng对图片资源进行压缩
webp图片资源优化

冷启动耗时统计
adb shell am start -S -W 包名/启动类的全限定名
例如：adb shell am start -S -W com.example.moneyqian.demo/com.example.moneyqian.demo.MainActivity

将Bugly，x5内核初始化，SP的读写，友盟等

















