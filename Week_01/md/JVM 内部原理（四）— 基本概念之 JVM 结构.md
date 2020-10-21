# JVM 内部原理（四）— 基本概念之 JVM 结构

## 介绍

版本：Java SE 7

每位使用 Java 的程序员都知道 Java 字节码在 Java 运行时（JRE - Java Runtime Environment）里运行。Java 虚拟机（JVM - Java Virtual Machine）是 Java 运行时（JRE）的重要组成部分，它可以分析和执行 Java 字节码。Java 程序员不需要知道 JVM 是如何工作的。有很多应用程序和应用程序库都已开发完成，但是它们并不需要开发者对 JVM 有深入的理解。但是，如果你理解 JVM ，那么就可以对 Java 更有了解，这也使得那些看似简单而又难以解决的问题得以解决。

在本篇文章中，我会解释 JVM 是如何工作的，它的结构如何，字节码是如何执行的及其执行顺序，与一些常见的错误及其解决方案，还有 Java 7 的新特性。

## 目录

- 虚拟机（Virtual Machine）
- Java 字节码
  - 症状
  - 原因
- 类文件格式（Class File Format）
  - 症状
  - 原因
- JVM 结构
  - 类装载器（Class Loader）
  - 运行时数据区
  - 执行引擎
- Java 虚拟机官方规范文档，第 7 版
  - 分支语句中的字符串
- 总结

## 内容

### JVM 结构

用 Java 编写的代码是按照一下流程执行的。

![image](http://note.youdao.com/yws/api/personal/file/WEBc531ccdb6d6724fe773f600d3de30de3?method=download&shareKey=86fc881bfe66aeb4368f09851a74bee0)

类装载器将编译好的 Java 字节码（Java Bytecode）加载到运行时数据区（Runtime Data Areas），执行引擎运行 Java 字节码。

#### 类装载器（Class Loader）

Java 提供了动态加载特性；它在运行时，而不是编译时第一次使用类时对类进行装载和链接处理。JVM 类装载器执行动态加载。Java 类装载器的特性如下：

- **层次化结构（Hierarchical Structure）**：Java 里的类装载器以父子关系的层次结构来组织。启动类装载器（Bootstrap Class Loader）是所有类装载器的父装载器。
- **代理模式（Delegation Mode）**：基于这个层次结构，装载在不同类装载器之间进行代理。当一个类在装载时，它的父装载器会检查并确定这个类是否在父装载器中。如果上层的装载器里有这个类，那么这个类就会被加载，如果没有，那么类装载器会请求加载这个类。
- **可见受限（Visibility Limit）**：子的类装载器可以在它的父装载器内找到类（即，父装载器中的类对子装载器可见），而子装载器中的类对父装载器是不可见的。
- **严禁卸载（Unload is Not Allowed）**：类装载器可以装载一个类，但是不能卸载它。不过，可以删除当前的类装载器，并创建一个新的。

每个类装载器有它的命名空间，存储着装载的类。当类装载器装载一个类时，它基于存储在命名空间下完整有效的类名称（FQCN - Fully Qualified Class Name）来检查判断一个类是否以及被装载。即使类有完全相同的 FQCN 但命名空间不同，那么它也会被认为是一个不同的类。不同的命名空间表明类是通过另一个类装载器加载的。

下图展示了一个类装载器的代理模型。

![image](http://note.youdao.com/yws/api/personal/file/WEB7a7aefc383757b42fd9d6d894a3a6055?method=download&shareKey=fac525a054a90b0f4611fbf8f74a092c)

当类装载器接受一个类加载的请求的时候，它会按以下顺序检查：这个类是否存在于类装载器的缓存中，是否在父装载器中，是否已被自己加载。简而言之，它会先检查这个类是否已经被加载到装载器的内存中，如果没有，它会检查父类装载器。如果这个类无法在启动类装载器中找到，那么被请求的类装载器会从文件系统查找这个类。

- **启动类装载器（Bootstrap class loader）**：它在 JVM 启动时被创建。它加载 Java API，包括对象类。与其他的装载器不同，它是用原生代码实现的，不是 Java 。
- **扩展类装载器（Extension class loader）**：它用来加载基本 Java API 以外的扩展类。它同时也加载各种安全相关的扩展功能。
- **系统类装载器（System class loader）**：如果启动类装载器和扩展类装载器加载的是 JVM 组件，那么系统类装载器就加载应用程序类。它加载 $CLASSPATH 上用户指定的类。
- **用户定义的类装载器（User-defined class loader）**：这个是由应用用户直接使用代码创建的类装载器。

像 Web 应用程序服务器（WAS - Web application server）这样的框架让 Web 应用和企业应用得以独立运行。换句话说，它可以保证程序可以通过类装载器的代理模式独立运行。WAS 类装载器同样使用层次结构，但不同 WAS 供应商的实现又有些许不同。

如果类装载器发现一个类未加载，这个类会通过以下流程进行加载和链接。

![image](http://note.youdao.com/yws/api/personal/file/WEBbed57cebdb0472bcfee26a36a1bbbd45?method=download&shareKey=ab39edf3a3d9a2a2a035a6ebee9fa5d5)

每个阶段具体工作的描述如下：

- **加载（Loading）**：类是从文件获取的并加载到 JVM 内存中。
- **验证（Verifying）**：检查读取的类是否按照 Java 语言规范和 JVM 规范。这是类加载过程中最复杂的一个测试过程，消耗很长的时间也是最多的。大多数 JVM TCK 测试用例都是来测试在加载一个错误类时，验证过程是否会抛出错误。
- **准备（Preparing）**：准备类及其字段、方法和接口所需指定内存的存储结构。
- **解析（Resolving）**：将类常量池内所有标识符更改成直接引用。
- **初始化（Initializing）**：将类变量以合适的值进行初始化。执行静态初始器，初始化静态字段。

JVM 规范定义了任务。然而，它对执行时间的却要求比较灵活。

#### 运行时数据区（Runtime Data Areas）

![image](http://note.youdao.com/yws/api/personal/file/WEB35088ebfbdffef939bd7b365d8e6014f?method=download&shareKey=6e82d06d7c00f7240ca183e106ce570e)

运行时数据区是 JVM 在 OS 上运行是分配的内存区域。运行时数据区可以分为 6 个部分。其中，程序计数寄存器（PC Register）、JVM 栈（JVM Stack）以及原生方法栈（Native Method Stack）是线程独有的。堆（Heap）、方法区（Method Area）、运行时常量池（Runtime Constant Pool）由所有线程共有。

- **程序计数寄存器（PC register）**：一个程序计数（Program Counter）寄存器存在于线程中，它在线程开始时创建。程序计数（Program Counter）寄存器有当前正在执行 JVM 指令的地址。

- **JVM 栈（JVM Stack）**：JVM 栈存在于线程中，也是在线程开始时创建。这个栈内的存储结构是栈帧（Stack Frame）为单位。JVM 只是对 JVM 栈进行入栈和出栈操作。如果有错误出现，栈跟踪的每行内容都会通过 printStackTrace() 将一个栈桢作为输出。

  ![image](http://note.youdao.com/yws/api/personal/file/WEBaf94bbffb7874c454213706e1a7de0c6?method=download&shareKey=cb1682dd3fd3006af3cc32ee0de7cff5)

  - 栈桢（Stack Frame）：栈桢是在方法执行期间创建的，并加入到线程的 JVM 栈中。当方法结束时，栈桢被移除。每个栈桢都有方法在类里执行时，对的本地变量列表（Local Variable Array）、操作数栈（Operand Stack）和运行时常量池（Runtime Constant Pool）的引用。本地变量列表（Local Variable Array）的大小以及操作数栈都是在编译时决定好的。因此，栈桢的大小根据方法的不同是固定的。
  - 本地变量列表（Local Variable Array）：它的索引从 0 开始。0 位置是方法所属实例的引用。从 1 开始，保存这方法的参数。在方法参数之后，保存的是方法的本地变量。
  - 操作数栈（Operand Stack）：方法真实的工作区。每个方法都在操作数栈与本地变量列表之间进行数据交换，对其他方法调用的结果进行入栈和出栈操作。操作数栈空间的必要大小在编译时决定。因此，操作数栈的大小也可以在编译时决定。

- **原地方法栈（Native method stack）**：一个供 Java 以外的本地语言代码使用的栈。换句话说，这个栈通过 Java 本地接口（JNI - Java Native Interface）执行 C/C++ 代码。会根据语言不同，分别创建 C 语言栈和 C++ 语言栈。

- **方法区（Method area）**：方法区是由所有线程共享的，它在 JVM 启动时创建。它存储了运行时常量池（runtime constant pool），字段（field）和方法（method）信息，静态变量（static variable），每个类与接口的方法字节，供 JVM 读取。方法区可以有多种不同的实现方式。Oracle Hotspot JVM 将其叫做永久区或永久代（PermGen）。方法区的垃圾收集对于 JVM 的提供商来说是可选实现。

- **运行时常量池（Runtime constant pool）**：与类文件格式中常量池表（constant_pool table）对应的区域。因此，JVM 规范特别强调了它的重要性。除了包括每个类与每个接口的常量，它还包括所有方法和字段的引用。简而言之，当方法和字段被引用时，JVM 会通过使用运行时常量池（runtime constant pool）搜索方法或字段在内存中的真实地址。

- **堆（Heap）**：存储实例或对象的空间，垃圾收集的目标区域。在我们讨论 JVM 性能问题时，会经常提及这个区域。JVM 提供商可以自行决定堆的配置方式，是否需要进行垃圾回收。

回头看看之前讨论过的反编译后的字节码

```
public void add(java.lang.String);
  Code:
   0:   aload_0
   1:   getfield        #15; //Field admin:Lcom/nhn/user/UserAdmin;
   4:   aload_1
   5:   invokevirtual   #23; //Method com/nhn/user/UserAdmin.addUser:(Ljava/lang/String;)Lcom/nhn/user/User;
   8:   pop
   9:   return
```

比较反编译之后的代码与 x86 架构下的汇编语言的操作码，两者有相似的格式；但是，Java 字节码不同的是它没有寄存器名，内存寻址器或操作数的偏移量。如之前描述的那样，JVM 使用栈，因此它不使用寄存器，取而代之的是使用如 15 和 23 这样的索引数字代替内存地址。因为它是自行管理内存的。15 和 23 是当前类常量池的索引（这里，UserService.class）。简言之，JVM 为每个类创建常量池，常量池中保存了引用的真实目标。

UserService.class 代码

```
// UserService.java
…
public void add(String userName) {
    admin.addUser(userName);
}
```

逐行解释反编译代码如下：

- **aload_0**：将索引为 #0 的本地变量列表加到操作数栈下。#0 索引的本地变量列表永远是 this ，当前类实例的引用。
- **getfield #15**： 在当前类的常量池下，将 #15 索引加入到操作数栈中。UserAdmin admin 字段被增加。因为 admin 字段是类的实例，因此加入的是它的引用。
- **aload_1**：将 #1 本地变量列表的索引加入到操作数栈中。本地变量列表的 #1 索引是方法的参数。因此，当调用 add() 时，字符串 userName 的引用被加入到栈中。
- **invokevirtual #23**：调用当前类常量池中对应 #23 索引的方法。此时，引用是通过使用 getfield 增加的，参数是通过使用 aload_1 将其送入被调用的方法的。当调用结束时，返回值被加入到操作数栈中。
- **pop**：使用 invokevirtual 对调用方法的返回值进行出栈操作。可以看到通过之前库编译的代码没有返回值。正因为如此，也无须对做出栈操作获取返回值。
- **return**：完成方法。

下图将有助于理解上述过程。

![image](http://note.youdao.com/yws/api/personal/file/WEB94dcec8f4c9723bd64ee7be1c5d5fd93?method=download&shareKey=6aa986212ebdbd7b845a9e918097bad3)

在本例中，本地变量列表并没有发生改变。所以上图只显示了操作数栈发生的改变。但是，在大多数情况下，本地变量列表也同样会发生变化。本地变量列表与操作数栈之间的数据传输通过很多装载命令（aload，iload）以及存储指令（astore，istore）来完成。

在本场景中，我们对运行时常量池和 JVM 栈有了一个简单的描述。当 JVM 运行时，每个类实例都会存储在堆上，而包括 User、UserAdmin、UserService 和 String 的类信息会存储在方法区。

#### 执行引擎（Execution Engine）

通过类装载器加载到 JVM 运行时数据区的字节码是通过执行引擎来运行的。执行引擎以单元指令的形式读取 Java 字节码（Java Bytecode）。这就和 CPU 逐行执行机器命令一样。每条字节码命令都包括 1 字节的操作码以及操作数。执行引擎获取一个操作码，再通过操作数执行任务，然后执行下一条操作码。

Java 字节码是人可以理解的语言，这和机器直接执行的语言还不一样。因此，执行引擎必须在 JVM 中将字节码语言转换成机器可执行的语言。字节码可以通过以下的两种方式将自身转换成合适的语言。

- **解释器（Interpreter）**：逐行读取、解释以及执行字节码。正因为它逐行解释和执行命令，它可以快速的解释字节码，但是执行解释的结果就会比较慢。这是解释型语言的缺点。字节码“语言”基本上是以解释器的方式运行的。
- **JIT（Just-In-Time）编译器**：JIT 编译器的引入是为了弥补解释器的缺陷。执行引擎在合适的时间先执行解释器，JIT 编译器编译整个字节码并将其转换成本地代码（native code）。在此之后，执行引擎不再解释改方法，而是直接执行本地代码。以本地代码的方式来执行要比逐行解释指令要快得多。因为本地代码是存于缓存中的，所以编译的代码得以快速执行。

不过，JIT 编译器编译代码的时间要比解释器逐行解释代码的时间长。因此，如果代码只是执行一次，那么解释是优于编译的。正因为这样，JVM 用 JIT 编译器从内部检查方法执行的频次，只对那些超过一定执行频次的方法才进行编译。

![image](http://note.youdao.com/yws/api/personal/file/WEB11f83f5d923964706e54a6ea1a04fb18?method=download&shareKey=e6bf84ee1334fad36773be6012dc981a)

JVM 规范并没有规定执行引擎具体的运行方式。因此，JVM 厂商会用各种不同的技术来提升执行引擎的性能，同时也引入了各种不同类型的 JIT 编译器。

大多数编译器按下图方式运行：

![image](http://note.youdao.com/yws/api/personal/file/WEBe30dc21d273d48b50bf3c94e973331af?method=download&shareKey=42022a0b7d690f31960b6bce0df2562e)

JIT 编译器将字节码转换成中间层表达式，中间表现形式（IR - Intermediate Representation），执行优化，然后将表达式转换成本地代码（native code）。

Oracle Hotspot VM 使用 JIT 编译器被称为 Hotspot 编译器。之所以被称为 Hotspot 是因为 Hotspot 编译器通过性能分析搜索处于最高优先级的待编译的 “Hotspot”，然后将 Hotspot 编译成为本地代码（native code）。如果被编译字节码方法不再被频繁调用，换句话说也就是如果方法不再是 Hotspot ，Hotspot VM 会将本地代码从缓存中移除并以解释模式运行。Hotspot VM 被分为服务端 VM（Server VM）和客户端 VM（Client VM），这两个 VM 使用不同的 JIT 编译器。

![image](http://note.youdao.com/yws/api/personal/file/WEB031e401f881ed7c949accf333858b833?method=download&shareKey=c3e857e6db5b5213114b77542569533d)

客户端 VM 和服务器 VM 使用着相同的运行时；但是，它们使用着不同的 JIT 编译器，如上图所示。高级动态优化编译器（Advanced Dynamic Optimizing Compiler）是在服务端的 VM 中使用的，它应用了更为复杂和更为多样的性能优化技术。

IBM JVM 从 IBM JDK 6 开始引入了 AOT（Ahead-Of-Time）编译器以及 JIT 编译器。这意味着很多 JVM 会通过共享缓存共享已编译好的本地代码（native code）。简而言之，已经被 AOT 编译器编译过的代码可以直接在另外一个 JVM 中使用不需要重新编译。除此之外，IBM JVM 还用 AOT 编译器将预编译代码编译成 JXE（Java EXecutable）文件格式以提供更快的执行方式。

大多数 Java 性能的提升是通过提升执行引擎的能力获得的。与 JIT 编译器一样，由于引入了各式各样的性能优化技术，JVM 的性能也得以长足的提升。初期 JVM 与最近的 JVM 之间的最大差异之处就在于执行引擎。

Hotspot 编译器是自 Oracle Hotspot VM 的 1.3 版本引入的，Dalvik VM 引入 JIT 编译器是从 Android 2.2 开始的。

> 注意
>
> 引入如字节码这样中间语言的技术后，VM 执行字节码，JIT 编译器提高 JVM 性能的这种方式也通常应用于其他的语言。例如 Microsoft 的 .Net ，CLR（Common Language Runtime）也是一种虚拟机，执行某种称为 CIL（Common Intermediate Language）的字节码。CLR 同时提供 AOT 编译器和 JIT 编译器。因此，如果源代码是用 C# 或 VB.NET 编译的，编译器创建 CIL 并通过 JIT 编译器在 CLR 执行创建的 CIL 。CLR 也会使用垃圾回收策略，同时与 JVM 一样，它也是以堆栈结构机器的模式运行。

## 参考

参考来源:

[JVM Specification SE 7 - Run-Time Data Areas](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.5)

[2011.01 Java Bytecode Fundamentals](http://arhipov.blogspot.jp/2011/01/java-bytecode-fundamentals.html)

[2012.02 Understanding JVM Internals](http://www.cubrid.org/blog/dev-platform/understanding-jvm-internals/)

[2013.04 JVM Run-Time Data Areas](http://www.programcreek.com/2013/04/jvm-run-time-data-areas/)

[Chapter 5 of Inside the Java Virtual Machine](http://www.artima.com/insidejvm/ed2/jvm2.html)

[2012.10 Understanding JVM Internals, from Basic Structure to Java SE 7 Features](https://dzone.com/articles/understanding-jvm-internals)

[2016.05 深入理解java虚拟机](http://www.cnblogs.com/prayers/p/5515245.html)

## 结束