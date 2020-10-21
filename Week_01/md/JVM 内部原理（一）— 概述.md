# JVM 内部原理（一）— 概述

## 介绍

版本：Java SE 7

图中显示组件将会从两个方面分别解释。第一部分涵盖线程独有的组件，第二部分涵盖独立于线程的组件（即线程共享组件）。

![img](https://images2015.cnblogs.com/blog/613455/201612/613455-20161207132946616-1995941113.png)

## 目录

- 线程独享（Threads）
  - JVM 系统线程（JVM System Threads）
  - 程序计数器（PC）
  - 栈（Stack）
    - 本地（方法）栈（Native (Method) Stack）
    - 栈约束（Stack Restrictions）
    - 帧（Frame）
    - 本地变量数组（Local Variable Array）
    - 操作数栈（Operand Stack）
  - 动态链接（Dynamic Linking）
- 线程共享（Shared Between Threads）
  - 堆（Heap）
  - 内存管理（Memory Management）
  - 非堆内存（Non-Heap Memory）
  - JIT 编译（Just In Time (JIT) Compilation）
  - 方法区（Method Area）
  - 类文件结构（Class File Structure）
  - 类装载器（Classloader）
  - 快速类加载（Faster Class Loading）
  - 方法区在哪里（Where Is The Method Area）
  - 类装载器引用（Classloader Reference）
  - 运行时常量池（Run Time Constant Pool）
  - 异常表（Exception Table）
  - 标识符表（Symbol Table）
  - String.intern() 字符串表

![img](https://images2015.cnblogs.com/blog/613455/201612/613455-20161207133000616-1877269035.jpg)

## 线程独享

### 线程（Thread）

线程是程序中执行的线程。JVM 允许一个应用有多个线程并发运行。在 Hotspot JVM 中，一个 Java 线程与本地操作系统线程（native operating system）有直接映射。在准备好一个 Java 线程所需的所有状态后（比如，线程本地存储-thread-local storage，分配缓冲-allocation buffers，同步对象-synchronization objects，栈-stacks and 程序计数器-the program counter），本地线程才创建。一旦 Java 线程 中止，本地线程立即回收。操作系统负责调度所有线程并且将它们分发到可用的 CPU 。一旦系统线程初始化成功后，它就会调用 Java 线程里的 run() 方法。当 run() 方法返回时，未捕获的异常会被处理，本地系统线程确认 JVM 是否因为线程中止而需要被中止（例如，当前线程是否为最后一个非控制台线程）。当线程中止后，所有为系统线程和 Java 线程 分配的资源都会被释放。

### JVM 系统线程（JVM System Threads）

如果用 jconsole 或其他的 debugger 工具，就会看到有很多线程在后台运行。这些后台线程与主线程一同运行，以及作为调用 `public static void main(String[])` 而创建的主线程所创建的任何线程。在 Hotspot JVM 中，后台系统主线程有：

|                                      |                                                              |
| ------------------------------------ | ------------------------------------------------------------ |
| VM 线程（VM thread）                 | 此线程等待操作要求 JVM 所达到的安全点                        |
| 周期任务线程（Periodic task thread） | 此线程负责定时事件（例如，中断），用作规划定期执行的操作     |
| GC 线程                              | 这些线程支持 JVM 里各种类型的 GC                             |
| 编译器线程                           | 这些线程在运行时，将字节码编译成本地编码                     |
| 信号分发线程                         | 此线程接收发送给 JVM 进程的信号，并调用 JVM 内部合适的方法对信号进行处理 |

### 线程独有

每个运行的线程都包括一下组件：

#### 程序计数器（PC）

寻址当前指令或操作码如果当前方法不是 native 的。如果当前方法是 native 的，那么程序计数器（PC）的值是 undefined 。所有的 CPU 都有程序计数器，通常程序计数器会在执行指令结束后增加，因此它需要保持下一将要执行指令的地址。JVM 用程序计数器来跟踪指令的执行，程序计数器实际上是会指向方法区（Method Area）的内存地址。

> 

#### 栈（Stack）

每个线程都有自己的栈（stack），栈内以帧（frame）的形式保持着线程内执行的每个方法。栈是一个后进先出（LIFO）的数据结构，所以当前执行的方法在栈顶部。每次方法调用时，都会创建新的帧并且压入栈的顶部。当方法正常返回或抛出未捕获的异常时，帧或从栈顶移除。除了压入和移除帧对象的操作，栈没有其他直接的操作，因此帧对象可以分配在堆中，内存并不要求连续。

![img](https://images2015.cnblogs.com/blog/613455/201612/613455-20161207133022272-2128990993.png)

#### 本地（方法）栈（Native (Method) Stack）

并不是所有的 JVM 都支持 native 方法，而那些支持 native 方法的 JVM 都会以线程创建 native 方法栈。如果 JVM 使用 C 链接模型（C-linkage model）实现 Java Native Invocation（JNI），那么 native 栈是一个 C 语言栈。这种情况下，参数和返回值的顺序都和 C 程序里 native 栈的一致。一个 native 方法（取决于 JVM 的实现）通常也可以回调 JVM 内的 Java 方法。这个从 native 到 Java 的调用会发生在 Java 栈中；线程会将 native 栈放在一边，在 Java 栈中创建新的帧。

#### 栈约束（Stack Restrictions）

栈的大小可以是动态的或固定的。如果线程请求栈的大小超过了限制，就会抛出 StackOverflowError 。如果线程请求创建新的帧，但此时没有足够的内存可供分配，就会抛出 OutOfMemoryError 。

#### 帧（Frame）

每次方法调用时，新的帧都会创建并被压入栈顶。当方法正常返回或抛出未捕获异常时，帧会从做退栈操作。详细的异常处理参加后面 异常表（Exception Table）部分。

每个帧都包括

- 本地变量数组（Local variable array）
- 返回值（Return value）
- 操作数栈（Operand stack）
- 当前方法所在类到运行时常量池的引用

##### 本地变量数组（Local variable array）

本地变量的数组包括方法执行所需要的所有变量，包括 this 的引用，所有方法参数和其他本地定义的变量。对于那些方法（静态方法 static method）参数是以零开始的，对于实例方法，零为 this 保留。

本地变量可以是：

- boolean操作数栈
- byte
- char
- long
- short
- int
- float
- double
- reference
- returnAddress

所有的类型都在本地变量数组中占一个槽，而 long 和 double 会占两个连续的槽，因为它们有双倍宽度（64-bit 而不是 32-bit）。

> 对于 64-bit 模型有待进步研究。

##### 操作数栈（Operand stack）

操作数栈在执行字节码指令的时候使用，它和通用寄存器在 native CPU 中使用的方式类似。大多数 JVM 字节码通过 pushing，popping，duplicating，swapping，或生产消费值的操作使用操作数栈。因此，将值从本地变量数组和操作栈之间移动的指令通常是字节码。例如，一个简单的变量初始化会生成两个字节的编码与操作数栈交互。

```C
int i;
```

编译后生成：

```C
 0:	iconst_0	// Push 0 to top of the operand stack
 1:	istore_1	// Pop value from top of operand stack and store as local variable 1
```

本地变量数组、操作数栈和运行时常量池是如何交互的参见后面 类文件结构（Class File Structure）部分。

#### 动态链接（Dynamic Linking）

每个帧都有一个对运行时常量池的引用。引用指向帧内正在执行方法所在类使用的常量池。这个引用可以支持动态链接。

C/C++ 编码通常是首先编译一个对象文件，然后多个文件会被链接到一起生成一个可执行文件或 dll 文件。在链接阶段，标识引用（symbolic reference）会被真实的内存地址所替换，从而关联到最终的可执行文件。在 Java 中，这个链接的过程是在运行时动态完成的。

当 Java 类编译后，所有变量和方法的引用都作为标识引用存于类的常量池中。标识引用只是一个逻辑引用，并非真实物理内存的地址指向。JVM 实现厂商可以自行决定何时解析替换标识引用，可以发生在类文件被验证及装载后，这种模式被成为早解析；它也可以发生在第一次使用这个标识引用时，这种模式被成为懒解析或晚解析。但在晚解析模式下，如果解析出错，JVM 任何时候都需要表现的和第一次解析出错时一样。绑定是字段、方法或类在标识引用被识别后替换成直接引用的过程。它只在标识引用被完全替换后才发生。如果类的标识引用没有完全被解析，然后这个类被装载了，每个直接引用都会以偏移量的方式存储而不是运行时变量或方法的位置。

## 线程共享（Shared Between Threads）

### 堆（Heap）

堆是运行时分配类实例和数组内存的地方。数组和对象是不能存在栈里的，因为栈帧（frame）不是被设计用作此目的，一旦栈帧创建了，它的大小不可更改。帧只用来存储指向对中对象或数组的引用。与帧内本地变量数组里基本变量和引用不同，对象总是存储在堆内的，所以在方法结束前，它们不会被移除。而且，对象只能被垃圾回收器移除。

为了支持垃圾回收的机制，堆通常被分为三部分：

- 新生代（Young Generation）
  - 通常分为 新生者（Eden）和 幸存者（Survivor）
- 老年代（Old Generation/Tenured Generation）
- 永久代（Permanent Generation）

### 内存管理（Memory Management）

对象和数组不会被显式的移除，而是会被 GC 自动回收。

通常的顺序是这样：

1. 新的对象和数组被创建在新生代区
2. 小的 GC 会发生在新生代，存活的对象会从 新生区（Eden）移到 幸存区（Survivor）
3. 大的 GC ，通常会导致应用程序线程暂停，对象移动会发生在不同代之间。仍然存活的对象会从新生代被移动到老年代。
4. 永久代的收集时刻都会在对老年代收集时发生。任何一代内存使用满了，会在两代同时发生收集。

### 非堆内存（Non-Heap Memory）

那些逻辑上被认为是 JVM 机器一部分的对象不会创建与堆上。

非堆内存包括：

- 永久代，包括
  - 方法区
  - interned 字符串
- 编码缓存 用来编译和存储那些已经被 JIT 编译器编译成 native 码的方法

### JIT 编译（Just In Time (JIT) Compilation）

Java 字节码解释的速度没有直接在 JVM 主机 CPU 上运行的 native 码运行那么快。为了提升性能，Oracle Hotspot VM 查看那些定期执行 “热” 的字节码区域，并将它们编译成 native 码。native 码被存在非堆内存的编码缓存中。通过这种方式，Hotspot VM 尝试在额外编译时间以及运行时额外解释的时间中做平衡，以获取更好的性能

### 方法区（Method Area）

方法区按类存放类相关的信息：

- Classloader 引用（Classloader Reference）
- 运行时常量池（Run Time Constant Pool）
  - 数字常量（Numeric Constants）
  - 字段引用（Field References）
  - 方法引用（Method Reference）
  - 属性（Attribute）
- 字段数据（Field Data）
  - 按字段（Per Field）
    - 名称（Name）
    - 类型（Type）
    - 修饰符（Modifiers）
    - 属性（Attributes）
- 方法数据
  - 按方法（Per Method）
    - 名称（Name）
    - 返回类型（Return Type）
    - 参数类型#有序（Parameter Types in order）
    - 修饰符（Modifiers）
    - 属性（Attributes）
- 方法代码
  - 按方法（Per Method）
    - 字节码（Bytecodes）
    - 操作数栈大小（Operand Stack Size）
    - 本地变量大小（Local Variable Size）
    - 本地变量表（Local Variable Table）
    - 异常表（Exception Table）
      - 按异常来处理（Per Exception Handling）
        - 开始点（Start Point）
        - 终结点（End Point）
        - 处理代码的程序计数器偏移（PC Offset for Handler Code）
        - 被捕获的异常类的常量池的索引（Constant Pool Index for Exception Class Being Caught）

所有的线程都共享相同的方法区，所以在访问方法区数据和处理动态链接时必须保证线程安全。如果两个线程同时尝试访问一个未加载但只加载一次的字段或方法，两个线程都必须等到完全加载后才能继续执行。

### 类文件结构（Class File Structure）

一个编译好的类文件包括以下的结构

```
ClassFile {
    u4			magic;
    u2			minor_version;
    u2			major_version;
    u2			constant_pool_count;
    cp_info		contant_pool[constant_pool_count – 1];
    u2			access_flags;
    u2			this_class;
    u2			super_class;
    u2			interfaces_count;
    u2			interfaces[interfaces_count];
    u2			fields_count;
    field_info		fields[fields_count];
    u2			methods_count;
    method_info		methods[methods_count];
    u2			attributes_count;
    attribute_info	attributes[attributes_count];
}
```

|                                   |                                                              |
| --------------------------------- | ------------------------------------------------------------ |
| magic,minor_version,major_version | 指定关于类版本以及编译的 JDK 版本的信息                      |
| constant_pool                     | 与符号表类似，但是它包含更多信息                             |
| access_flags                      | 提供类修饰符列表                                             |
| this_class                        | 索引到 constant_pool 提供了完整的类名，例如，org/jamesdbloom/foo/Bar |
| super_class                       | 索引到 constant_pool 提供标识符引用到父类，例如，java/lang/Object |
| interfaces                        | 索引列表到 constant_pool 提供标识符引用到所有实现的接口      |
| fields                            | 索引列表到 constant_pool 为每个字段提供完整的描述            |
| methods                           | 索引列表到 constant_pool 为每个方法签名提供完整的描述，如果方法不是抽象的或 native 的，也会呈现字节码 |
| attributes                        | 不同值列表，提供类的额外信息，包括注解 RetentionPolicy.CLASS 或 RetentionPolicy.RUNTIME |

可以通过 javap 命令查看被编译的 Java 类的字节码。

如果编译以下这个简单的类：

```
package org.jvminternals;

public class SimpleClass {

    public void sayHello() {
        System.out.println("Hello");
    }

}
```

那么执行

javap -v -p -s -sysinfo -constants classes/org/jvminternals/SimpleClass.class

会得到字节码

```
public class org.jvminternals.SimpleClass
  SourceFile: "SimpleClass.java"
  minor version: 0
  major version: 51
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Methodref          #6.#17         //  java/lang/Object."<init>":()V
   #2 = Fieldref           #18.#19        //  java/lang/System.out:Ljava/io/PrintStream;
   #3 = String             #20            //  "Hello"
   #4 = Methodref          #21.#22        //  java/io/PrintStream.println:(Ljava/lang/String;)V
   #5 = Class              #23            //  org/jvminternals/SimpleClass
   #6 = Class              #24            //  java/lang/Object
   #7 = Utf8               <init>
   #8 = Utf8               ()V
   #9 = Utf8               Code
  #10 = Utf8               LineNumberTable
  #11 = Utf8               LocalVariableTable
  #12 = Utf8               this
  #13 = Utf8               Lorg/jvminternals/SimpleClass;
  #14 = Utf8               sayHello
  #15 = Utf8               SourceFile
  #16 = Utf8               SimpleClass.java
  #17 = NameAndType        #7:#8          //  "<init>":()V
  #18 = Class              #25            //  java/lang/System
  #19 = NameAndType        #26:#27        //  out:Ljava/io/PrintStream;
  #20 = Utf8               Hello
  #21 = Class              #28            //  java/io/PrintStream
  #22 = NameAndType        #29:#30        //  println:(Ljava/lang/String;)V
  #23 = Utf8               org/jvminternals/SimpleClass
  #24 = Utf8               java/lang/Object
  #25 = Utf8               java/lang/System
  #26 = Utf8               out
  #27 = Utf8               Ljava/io/PrintStream;
  #28 = Utf8               java/io/PrintStream
  #29 = Utf8               println
  #30 = Utf8               (Ljava/lang/String;)V
{
  public org.jvminternals.SimpleClass();
    Signature: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
        0: aload_0
        1: invokespecial #1    // Method java/lang/Object."<init>":()V
        4: return
      LineNumberTable:
        line 3: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
          0      5      0    this   Lorg/jvminternals/SimpleClass;

  public void sayHello();
    Signature: ()V
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=1, args_size=1
        0: getstatic      #2    // Field java/lang/System.out:Ljava/io/PrintStream;
        3: ldc            #3    // String "Hello"
        5: invokevirtual  #4    // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        8: return
      LineNumberTable:
        line 6: 0
        line 7: 8
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
          0      9      0    this   Lorg/jvminternals/SimpleClass;
}
```

这个类文件展示了三个主要部分，常量池、构造器和 sayHello 方法。

- 常量池 - 提供了字符表相同的信息
- 方法 - 每个方法包括四个方面
  - 签名 和 访问标志位（access flags）
  - 字节码
  - 行号表（LineNumberTable）- 为 debugger 工具提供信息，为字节码指令保存行号，例如，第 6 行在 sayHello 方法中的字节码是 0 ，第 7 行对应的字节码是 8 。
  - 本地变量表 - 列出了帧内所有的本地变量，在两个示例中，只有一个本地变量就是 this 。

以下的字节码操作数会在类文件中被用到。

|                             |                                                              |
| --------------------------- | ------------------------------------------------------------ |
| aload_0                     | 这个操作码是一组以 aload_<n> 为格式的操作码中的一个。它们都会装载一个对象的引用到操作数栈里。<n> 指的是本地变量列表的访问位置，只能通过 0、1、2 或 3 来访问。也有其他类似的操作码用来装载值，但不是用作对象引用的 iload_<n>，lload_<n>，float_<n> 和 dload_<n> 这里 i 是对应 int，l 对应 long，f 对应 float，d 对应 double。本地变量的索引位置大于 3 的可以分别通过 iload、lload、float、dload 以及 aload 来装载。这些所有的操作码都以单个操作数来指定要装载的本地变量的索引位置。 |
| ldc                         | 这个操作码用来将常量从运行时常量池压入到操作数栈中。         |
| getstatic                   | 这个操作码用来将静态值从运行时常量池内的一个静态字段列表中压入到操作数栈内。 |
| invokespecial,invokevirtual | 这两个操作码是一组用来调用方法操作码其中的两个，它们是 invokedynamic、invokeinterface、invokespecial、invokestatic、invokevirtual。在这个类文件中，invokespecial 和 invokevirtual 同时被用到，不同之处在于 invokevirtual 调用对象类上的一个方法，而 invokespecial 指令用来调用实例初始化的方法、私有方法或者当前类父类中的方法。 |
| return                      | 这个操作码是一组操作码中的一个，它们是：ireturn，lreturn，freturn，dreturn，areturn 和 return。每个操作码都是与类型相关的返回语句。i 对应 int，l 对应 long，f 对应 float，d 对应 double 然后 a 是对象引用。不带首字母的 return 返回 void。 |

作为字节码，大多操作数以下面这种方式与本地变量、操作数栈和运行时常量池进行交互。

构造器有两个指令，第一个 this 被压入操作数栈，另一个是其父类的构造器，它在调用时会消费 this 并且对操作数栈进行退栈操作。

![img](https://images2015.cnblogs.com/blog/613455/201612/613455-20161207133109085-716416312.png)

sayHello() 方法要更为复杂，因为它必须解析符号引用获取对运行时常量池的真实引用。第一个操作数 getstatic 用来将对 System 类 out 静态字段的引用压入到操作数栈。第二个操作数 ldc 将字符串 “Hello” 压入到操作数栈顶部。最后一个操作数 invokevirtual 调用 System.out 的 println 方法，对 “Hello” 的操作数进行出栈操作当作参数并且为当前线程创建新的帧。

![img](https://images2015.cnblogs.com/blog/613455/201612/613455-20161207133122663-264684757.png)

### 类装载器（Classloader）

JVM 开始于使用启动装载器（bootstrap classloader）装载一个初始类。类在 public static void main(String[]) 调用前完成链接和初始化。这个方法的执行也会驱动装载、链接和初始化其他所需的类与接口。

**装载（Loading）** 是查找特定名称的类或接口类型对应的类文件并将其读入字节数组的过程。字节符被解析并确定它们所代表的 Class 对象以及是否具备正确的版本（major and minor）。任何直接父类，不论是类还是接口都会被装载。一旦这个过程完成后，就会从二进制的表现形式创建类对象或接口对象。

**链接（Linking）** 是对类或接口进行验证并准备它们的类型、直接父类以及直接父接口的过程。链接包括三步：验证、准备和识别（resolving 可选）。

- **验证（Verifying）** 是确认类或接口的表现形式的结构是否正确，是否遵守 Java 编程语言及 JVM 语法规范的过程。例如：会进行以下检查

  1. 一致且格式正确的符号表
  2. final 方法/类没有没有被重载
  3. 方法符合访问控制的关键字
  4. 方法参数的数量和类型正确
  5. 字节码对栈进行正确的操作
  6. 变量在读取前已被正确的初始化
  7. 变量的类型正确

  在验证过程进行这些检查也就意味着无须在运行时进行检查。在链接时进行验证会降低类装载的速度，但同时也避免了在运行字节码时，进行多次验证。

- **准备（Preparing）** 过程涉及为静态存储以及任何 JVM 使用的数据结构（比如，方法表）分配内存。静态字段用缺省值进行创建和初始化，但是，没有初始方法或编码在这个阶段执行，因为这会发生在初始化阶段。

- **解析（Resolving）** 是一个可选阶段，它涉及到通过装载引用类和接口的方式检查标识引用，并检查引用是否正确。如果没有在此处进行解析，那么标识引用的解析过程可以推迟到字节码指令使用之前执行。

** 初始化（Initialization）** 类或接口的过程包括执行类或接口初始化方法 <clinit> 的过程。

![img](https://images2015.cnblogs.com/blog/613455/201612/613455-20161207133138210-321698865.png)

在 JVM 里，有多个不同角色的类装载器。每个类装载器到代理装载它的父装载器，** bootstrap classloader ** 是顶部的装载器。

***Bootstrap Classloader\*** 通常是用原生代码实现的（native code）因为它在 JVM 装载的早期实例化的。bootstrap classloader 的职责是装载基本的 Java APIs，包括例如 rt.jar 。它只装载那些 classpath 下具有高可信度的类；这样它也会省略很多对普通类需要做的校验。

***Extension Classloader\*** 装载那些从标准 Java 扩展的 API 比如，安全扩展功能。

***System Classloader\*** 默认的应用装载器，用来从 classpath 装载应用程序类。

***User Defined Classloader\*** 也可以用来装载应用程序类。使用用户定义的 classloader 有很多特殊的原因，包括运行时重新装载类或者区分不同装载类的组别（通常在 web 服务器，如 Tomcat 需要用到这点）。

![img](https://images2015.cnblogs.com/blog/613455/201612/613455-20161207133154101-32130987.png)

### 快速类加载（Faster Class Loading）

在 Hotspot JVM 5.0 之后引入了一个被称为 类数据共享（Class Data Sharing-CDS）的新特性。在安装 JVM 的过程中，JVM 安装并加载一组 JVM 类的关键集合到内存映射的共享文件中，如 rt.jar 。CDS 减少了 JVM 启动所需的时间，它使得这些类可以在多个不同 JVM 实例共享，从而减少了 JVM 的内存占用。

### 方法区在哪里（Where Is The Method Area）

在 [The Java Virtual Machine Specification Java SE 7 Edition](https://www.amazon.co.uk/Virtual-Machine-Specification-Edition-Series/dp/0133260445) 中，明确指出：“尽管方法区（Method Area）逻辑上是堆的一部分，简单的实现通常既不会对其进行垃圾回收，也不会对其进行压缩”。相反，Oracle JVM 的 jconsole 显示方法区（以及代码缓存）处于非堆中。OpenJDK 的代码显示代码缓存（CodeCache）在 VM 里是独立于对象堆的区域。

### 类装载器引用（Classloader Reference）

所有被装载的类都保留对它装载器（classloader）的一个引用。反正，装载器（classloader）也保留了它装载的所有类的引用。

### 运行时常量池（Run Time Constant Pool）

JVM 按类型维护常量池和运行时的数据结构，它与标识表类似，只是包含更多的数据。Java 里的字节码需要请求数据，通常这些数据很大，无法直接存储于字节码内，所以它们会被存储在常量池中，字节码里只保留一个对常量池的引用。运行时的常量池是作动态链接的。

在常量池内存储着几种类型的数据：

- 数字（numeric literals）
- 字符串（string literals）
- 类引用（class references）
- 字段引用（field references）
- 方法引用（method references）

例如以下代码：

```
Object foo = new Object();
```

用字节码表示会写成：

```
0: 	new #2 		    // Class java/lang/Object
1:	dup
2:	invokespecial #3    // Method java/ lang/Object "<init>"( ) V		
```

`new` 这个操作数码（operand code）紧接着 #2 这个操作数。这个操作码是常量池内的一个索引，因此引用到常量池内的另一个记录，这个记录是一个类的引用，这个记录进一步引用到常量池里以 UTF8 编码的字符串常量 `// Class java/lang/Object` 。这个标识链接就能用来查找 java.lang.Object 类。`new` 操作数码创建类实例并初始化其变量。然后一个新的类实例被加入到操作数栈内。`dup` 操作码拷贝了操作数栈顶部位置的索引，并将其压入到操作数栈的顶部。最后，实例初始化方法在第 2 行被 invokespecial 调用。这个操作数也包含了对常量池的一个引用。初始化方法进行退栈操作，并将引用作为参数传递给方法。这样一个对新对象的引用就创建并初始化完成了。

如果编译以下这个简单的类：

```
package org.jvminternals;

public class SimpleClass {

    public void sayHello() {
        System.out.println("Hello");
    }

}
```

在生成类文件的常量池会是下面这样：

```
Constant pool:
   #1 = Methodref          #6.#17         //  java/lang/Object."<init>":()V
   #2 = Fieldref           #18.#19        //  java/lang/System.out:Ljava/io/PrintStream;
   #3 = String             #20            //  "Hello"
   #4 = Methodref          #21.#22        //  java/io/PrintStream.println:(Ljava/lang/String;)V
   #5 = Class              #23            //  org/jvminternals/SimpleClass
   #6 = Class              #24            //  java/lang/Object
   #7 = Utf8               <init>
   #8 = Utf8               ()V
   #9 = Utf8               Code
  #10 = Utf8               LineNumberTable
  #11 = Utf8               LocalVariableTable
  #12 = Utf8               this
  #13 = Utf8               Lorg/jvminternals/SimpleClass;
  #14 = Utf8               sayHello
  #15 = Utf8               SourceFile
  #16 = Utf8               SimpleClass.java
  #17 = NameAndType        #7:#8          //  "<init>":()V
  #18 = Class              #25            //  java/lang/System
  #19 = NameAndType        #26:#27        //  out:Ljava/io/PrintStream;
  #20 = Utf8               Hello
  #21 = Class              #28            //  java/io/PrintStream
  #22 = NameAndType        #29:#30        //  println:(Ljava/lang/String;)V
  #23 = Utf8               org/jvminternals/SimpleClass
  #24 = Utf8               java/lang/Object
  #25 = Utf8               java/lang/System
  #26 = Utf8               out
  #27 = Utf8               Ljava/io/PrintStream;
  #28 = Utf8               java/io/PrintStream
  #29 = Utf8               println
  #30 = Utf8               (Ljava/lang/String;)V
```

常量表里有如下类型：

|                                       |                                                              |
| ------------------------------------- | ------------------------------------------------------------ |
| Integer                               | 4 byte int 常量                                              |
| Long                                  | 8 byte long 常量                                             |
| Float                                 | 4 byte float 常量                                            |
| Double                                | 8 byte double 常量                                           |
| String                                | 字符串常量指向常量池中另一个 UTF8 包含真实字节的记录         |
| Utf8                                  | 一个 UTF8 编码的字符串流                                     |
| Class                                 | 一个类常量指向常量池中另一个 UTF8 包含 JVM 格式的完整类名称的记录 |
| NameAndType                           | 以分号分隔的数值对，每个都指向常量池中的一条记录。分号前的数值指向表示方法或类型名称的 UTF8 字符串记录，分号后的数值指向类型。如果是字段，那么对应完整的累名称，如果是方法，那么对应一组包含完整类名的参数列表 |
| Fieldref,Methodref,InterfaceMethodref | 以点为分隔符的数值对，每个数值指向常量池里面的一条记录。点之前的值指向 Class 记录，点之后的值指向 NameAndType 记录 |

### 异常表（Exception Table）

异常表按异常处理类型存储信息：

- 开始点（Start point）
- 结束点（End point）
- 异常处理代码程序计数器的偏移量（PC offset for handler code）
- 捕获异常类在常量池中的索引

如果一个方法定义了 try-catch 或 try-finally 异常处理，那么就会创建一个异常表。它包括了每个异常处理或 finally 块以及异常处理代码应用的范围，包括异常的类型以及异常处理的代码。

当抛出异常时，JVM 会查找与当前方法匹配的异常处理代码，如果没有找到，方法就会被异常中止，然后对当前栈桢退栈，并在被调用的方法内（新的当前桢）重新抛出异常。如果没有找到任何异常处理程序，那么所有的桢都会被退栈，线程被中止。这也可能导致 JVM 本身被中止，如果异常发生在最后一个非后台线程时就会出现这种状况。例如，如果线程是主线程。

`finally` 异常处理匹配所有类型的异常，所以只要有异常抛出就会执行异常处理。当没有异常抛出时，`finally` 块仍然会被执行，这可以通过在 return 语句执行之前，跳入 finally 处理代码来实现。

### 标识符表（Symbol Table）

说到按类型存储的运行时常量池，Hotspot JVM 的标识符表是存储在永久代的。标识符表用一个 Hashtable 在标识指针与标识之间建立映射（例如，Hashtable<Symbol*, Symbol>）以及一个指向所有标识符的指针，包括那些被存储的每个类的运行时常量表。

引用计数用来控制标识符从标识符表内移除。例如，当一个类被卸载时，所有在运行时常量池里保留的标识符引用都会做相应的自减。当标识符表的引用计数变为零时，标识符表知道标识符不再被引用，那么标识符就会从标识符表中卸载。无论是标识符表还是字符串表，所有记录都以都以出现的 canonicalized 形式保持以提高性能，并保证每个记录都只出现一次。

### String.intern() 字符串表

Java 语言规范（The Java Language Specification）要求相同的字符串文字一致，即包含相同顺序的 Unicode 码指针，指针指向相同的字符串实例。如果 String.intern() 在某个字符串的实例引用被调用，那么它的值需要与相同字符串文字引用的返回值相等。即以下语句为真：

```
("j" + "v" + "m").intern() == "jvm"
```

在 Hotspot JVM intern 的字符串是保存在字符串表里的，它用一个 Hashtable 在对象指针与字符之间建立映射（例如，Hashtable<oop, Symbol>），它被保存在永久代里。对于标识符表和字符串表，所有记录都是以 canonicalized 形式保持以提高性能，并保证每个记录都只出现一次。

字符串 literals 在编译时自动被 interned 并在装载类的时候被加载到字符表里。字符串类的实例也可以显式调用 String.intern() 。当 String.intern() 被调用后，如果标识符表已经包含了该字符串，那么将直接返回字符串的引用，如果没有，那么字符串会被加入到字符串表中，然后返回字符串的引用。