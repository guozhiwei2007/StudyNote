Binder机制
Android中进程通信Binder机制
主题	Android中进程通信Binder机制
作者	2018/06/01 赵永涛
附件	ppt、官方demo代码
将从以下几个方面,讲解:

IPC进程间通信(简介);
Binder机制优势;
Android AIDL DEMO;
Binder机制分析;
Android中Binder的应用
IPC进程间通信(简介)
IPC是Inter Process Communication的缩写，其意思就是进程间的通信，也就是两个进程之间的通信过程。我们都知道在Android系统中，每个应用都运行在一个进程上，具有自己的DVM实例，而且进程之间是相互隔离的，也就是说各个进程之间的数据是互相独立，互不影响的，而如果一个进程崩溃了，也不会影响到另一个进程。 采取这样的设计是有一定道理的，例如这样的前提下将互相不影响的系统功能分拆到不同的进程里面去，有助于提升系统的稳定性，毕竟我们都不想自己的应用进程崩溃会导致整个手机系统的崩溃。 进程之间隔离是不错的选择，可是如果进程之间想要互相通信，进行数据交互的时候那该怎么办呢？例如我们在自己的应用中想要访问手机通讯录中的联系人，很显然这是两个不同的进程，如果Android没有提供一种进程之间交流的机制，那么这种功能将无法实现。 不过由于Android系统使用的是Linux内核，而在Linux系统中进程之间的交互是有一套机制的，所以Android也借鉴了其中的一些机制，从而形成了Android的IPC机制

主要有以下几种方式:
管道(Pipe)
管道可用于具有亲缘关系进程间的通信，有名管道克服了管道没有名字的限制，因此，除具有管道所具有的功能外，它还允许无亲缘关系进程间的通信

信号(Signal)
信号是比较复杂的通信方式，用于通知接受进程有某种事件发生

Message队列(消息队列)
消息队列是消息的链接表 ,消息队列克服了信号承载信息量少，管道只能承载无格式字节流以及缓冲区大小受限等缺点

共享内存
使得多个进程可以访问同一块内存空间，是最快的可用IPC形式。是针对其他通信机制运行效率较低而设计的。往往与其它通信机制，如信号量结合使用，来达到进程间的同步及互斥

套接字(Soket)
更为一般的进程间通信机制，主要用在跨网络的进程间通信
有一个例外是SystemService进程与Zygote进程之间是通过Socket的方式进行通讯的

Binder
安卓中大量使用了Binder机制

Binder机制优势
其他IPC方式弊端
管道(Pipe)
在创建时分配一个page大小的内存，缓存区大小比较有限

信号(Signal)
不适用于信息交换，更适用于进程中断控制，比如非法内存访问，杀死某个进程等

Message队列(消息队列)
信息复制两次，额外的CPU消耗；不合适频繁或信息量大的通信

共享内存
无须复制，共享缓冲区直接付附加到进程虚拟地址空间，速度快；但进程间的同步问题操作系统无法实现，必须各进程利用同步工具解决

套接字(Soket)
作为更通用的接口，传输效率低，主要用于不通机器或跨网络的通信

Binder机制优势
性能方面
在移动设备上,跨进程通信对通信机制的性能有严格的要求 Binder基于 Client-Server通信模式，传输过程只需一次拷贝 ,而管道、消息队列、Socket都需要2次，共享内存方式一次内存拷贝都不需要，但实现方式又比较复杂

安全方面
传统的进程通信方式对于通信双方的身份并没有做出严格的验证，比如Socket通信ip地址是客户端手动填入，很容易进行伪造，而Binder机制从协议本身就支持对通信双方做身份校检，因而大大提升了安全性

Android AIDL demo
生成一个aidl文件
里边有一个getPeopleList()方法

interface IPeopleManager {
    //...
    List<People> getPeopleList();
}
菜单-Build-Make Project 后会自动生成一个 interface

public interface IPeopleManager extends android.os.IInterface {
    public static abstract class Stub extends android.os.Binder implements zyt.example.com.aldldemo.IPeopleManager {
        private static final java.lang.String DESCRIPTOR = "zyt.example.com.aldldemo.IPeopleManager";
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        public static zyt.example.com.aldldemo.IPeopleManager asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof zyt.example.com.aldldemo.IPeopleManager))) {
                return ((zyt.example.com.aldldemo.IPeopleManager) iin);
            }
            return new zyt.example.com.aldldemo.IPeopleManager.Stub.Proxy(obj);
        }

        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            switch (code) {
                //...
                case TRANSACTION_getPeopleList: {
                    data.enforceInterface(DESCRIPTOR);
                    java.util.List<zyt.example.com.aldldemo.People> _result = this.getPeopleList();
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements zyt.example.com.aldldemo.IPeopleManager {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder() {
                return mRemote;
            }

            public java.lang.String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            //...

            @Override
            public java.util.List<zyt.example.com.aldldemo.People> getPeopleList() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.util.List<zyt.example.com.aldldemo.People> _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getPeopleList, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createTypedArrayList(zyt.example.com.aldldemo.People.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
        }
        //...
        static final int TRANSACTION_getPeopleList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    }

    //...
    public java.util.List<zyt.example.com.aldldemo.People> getPeopleList() throws android.os.RemoteException;
}
增加一个Service
并在Manifest中制定一个新的进程remotew <service android:name=".server.MyService" android:process=":remotew">
重写Obind()方法,返回自动生成中的Interface中的Stub

public class MyService extends Service {
    List<People> peopleList = new ArrayList<>(10);
    IPeopleManager.Stub stub = new IPeopleManager.Stub() {
        //...
        @Override
        public List<People> getPeopleList() throws RemoteException {
            return peopleList;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {//可以控制是否返回IBinder给某个Client
        return stub;
    }
}
增加一个Activity
public class MainActivity extends Activity {

    private ServiceConnection connection;
    private IPeopleManager iPeopleManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent();
        intent.setAction("zyt.example.com.aldldemo.server.MyService");
        intent.setPackage(getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {//android.os.BinderProxy iBinder
                iPeopleManager = IPeopleManager.Stub.asInterface(iBinder);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                iPeopleManager = null;
            }
        };
        bindService(intent, connection, BIND_AUTO_CREATE);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExecutorUtile.runInSubThred(new Runnable() {//调用服务端方法时，应开启子线程，防止UI线程堵塞，导致ANR。此为线程池
                    @Override
                    public void run() {
                        try {
                            List<People> peopleList = iPeopleManager.getPeopleList();
                            Looper.prepare();
                            Toast.makeText(getApplicationContext(), String.format("查询到了%d个人", peopleList.size()), Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
Binder机制分析
Android系统的Binder机制中, 由一系列统组件组成:Server，Client，ServiceManager以及Binder驱动。
其中Server，Client， ServiceManager运行于用户空间， Binder驱动运行于内核空间。
这四个角色的关系和上网类似：Server是服务器，Client是客户终端， ServiceManager是域名服务器（DNS），驱动是路由器。



Client-Server的通信方式
面向对象的思想: Binder及其在Client中的入口;Binder是一个实体位于Server中的对象, Client通过Binder的引用访问Server,将进程间通信转化为通过对某个Binder对象的引用调用该对象的方法
Server
一个进程作为Server提供例如 拨打电话，视频/音频解码，视频捕获，通讯录查询，网络连接等等服务

Client
Client通过获得一个server的代理接口，对server进行调用
其他多个进程可以作为Client向Server发起服务请求，获得所需要的服务

ServiceManager
类似DNS解析， ServiceManager的作用是Server中的Binder转化成Client中对该Binder的引用，使Client能够通过Binder引用代理与Server中的Binder真身进行通信。

Binder对象是一个可以跨进程引用的对象, 它的实体位于一个进程中，而它的引用却遍布于系统的各个进程之中。

Client-Server是相对的
比如ActivityManagerService对于应用层来说是Server 但其相对于ServiceManager来说属于Client
(ServiceManager属于系统的守护线程,它的主要作用，就是帮助系统去维护众多的Service列表,在程序启动的时候创建)

Binder 驱动
Binder驱动虽然默默无闻，却是整个通信的核心。尽管名叫‘驱动’，实际上和硬件设备没有任何关系，只是实现方式和设备驱动程序是一样的：它工作于内核态，提供open()，mmap()，poll()，ioctl()等标准文件操作，用户通过设备目录/dev/binder访问该它。驱动负责进程之间Binder通信的建立，Binder在进程之间的传递，Binder引用计数管理，数据包在进程之间的传递和交互等一系列底层支持。驱动和应用程序之间定义了一套接口协议，主要功能由ioctl()接口实现

##Binder在Android中的运用

######说起Binder在Android的使用场景，可以说是无处不在：
四大组件的生命周期都是使用Binder机制进行管理的
View的工作原理也使用了Binder
WindowManager的工作机制同样使用了Binder
以上三个方面只是最常见的场景，但是却几乎包括了我们开发的整个流程。我们开发的应用都离不开四大组件，而四大组件也正是依靠Binder机制运行的；对于我们最常见的View，他是如何显示的，View又是如何响应我们的动作的，这其中也用到了Binder

那么系统的这些Server都是在什么时候启动的?
SystemServer进程是android中一个很重要的进程由Zygote进程启动
startBootstrapServices() 主要用于启动系统Boot级服务  startCoreServices() 主要用于启动系统核心的服务  startOtherServices() 主要用于启动一些非紧要或者是非需要及时启动的服务

从Activity的启动理解Binder通信
从Activity中调用startActivity会最终调用到startActivityForResult方法,startActivityForResult中是这样的

 public void startActivityForResult(Intent intent, int requestCode,
            @Nullable Bundle options) {
        if (mParent == null) {
            Instrumentation.ActivityResult ar =
                mInstrumentation.execStartActivity(
                    this, mMainThread.getApplicationThread(), mToken, this, intent, requestCode, options);    
                    //...        }
        }
    }
会调用Instrumentation的execStartActivity方法

public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options) {
        //...
        try {
            //...
            int result = ActivityManagerNative.getDefault().startActivity(...); 
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
        return null;
    }
而通过ActivityManagerNative.getDefault()去调用的就是ActivityManagerService中的对应startActivity方法

static public IActivityManager getDefault() {
        return gDefault.get();
    }
private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
        protected IActivityManager create() {
            IBinder b = ServiceManager.getService("activity");
            if (false) {
                Log.v("ActivityManager", "default service binder = " + b);
            }
            IActivityManager am = asInterface(b);
            if (false) {
                Log.v("ActivityManager", "default service = " + am);
            }
            return am;
        }
    };