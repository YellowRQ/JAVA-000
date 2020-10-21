# JVM 内部原理（二）— 基本概念之字节码

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

### 虚拟机（Virtual Machine）

Java 运行时环境包括 Java API 和 JVM 。JVM 负责通过装载器（Class Loader）读取 Java 应用程序并结合 Java API 一起执行。

**虚拟机（VM）** 是机器的软件实现（如，计算机），它可以像物理机一样执行程序。Java 设计的初衷是让运行时基于虚拟机与物理机器隔离，即一次编写随处执行（**WORA** - Write Once Run Anywhere），尽管这个目标几乎已经被人遗忘。因此，JVM 可以在各种硬件上运行，并执行 **Java 字节码（Java Bytecode）** 无须改变 Java 的执行代码。

JVM 有如下特性：

- **基于栈的虚拟机（Stack-based virtual machine）：** 大多数流行的计算机架构如 Intel x86 架构和 ARM 架构都是基于寄存器运行的。但是，*JVM 是基于栈运行的* 。
- **标识符引用（Symbolic reference）：** 所有类型（类和接口）除了基本类型（又称原始类型）都是通过标识符引用的，而不是通过显式的基于内存地址的引用。
- **垃圾收集（Garbage collection）：** 一个类实例是由用户代码显式创建的并通过垃圾收集自动销毁。
- **通过清楚的定义基本数据类型（primitive data type）保证平台的独立：** 传统的语言如 C/C++ 在不同平台下的 int 类型的大小是不一样的。JVM 清楚地定义了原始数据类型以维持兼容性和保证跨平台的能力。
- **网络字节顺序（Network byte order）：** Java 类文件使用网络字节顺序。要在 Intel x86 架构采用的 little endian 与 RISC 系列架构采用的 big endian 之间维持平台独立，就必须保证固定的字节序。因此，JVM 使用网络字节序，它是一种网络传输的顺序。网络字节序是 big endian 的。

Sun 公司（Sun Microsystems）开发了 Java 。不过，任何厂商都可以开发并提供 JVM ，只要遵守 Java 虚拟机官方规范文档即可。因此，JVM 有很多种类，包括 Oracle 公司的 Hotspot JVM 和 IBM 公司的 JVM 。Google 安卓操作系统使用的 Dalvik VM 也是一种 JVM ，尽管它并不遵守 Java 虚拟机规范。与 Java 虚拟机不同（基于栈的虚拟机），Dalvik VM 采用基于寄存器的架构。

### Java 字节码

要想实现 一次编写到处运行（WORA），JVM 使用 Java 字节码，它是一种介于 Java（用户语言）和机器语言之间的中间语言。Java 字节码是 Java 代码部署的最小单元。

在解释 Java 字节码之前，让我们以一个开发过程中出现的真实案例来进行介绍。

#### 症状

应用程序在库更新之后返回如下错误：

```
1 Exception in thread "main" java.lang.NoSuchMethodError:  com.nhn.user.UserAdmin.addUser(Ljava/lang/String;)V
2     at com.nhn.service.UserService.add(UserService.java:14)
3     at com.nhn.service.UserService.main(UserService.java:19)
```

应用程序代码如下，并没有作任何更改：

```
1 // UserService.java
2 …
3 public void add(String userName) {
4     admin.addUser(userName);
5 }	
```

更新的库源码如下：

```
 1 // UserAdmin.java - Updated library source code
 2 …
 3 public User addUser(String userName) {
 4     User user = new User(userName);
 5     User prevUser = userMap.put(userName, user);
 6     return prevUser;
 7 }
 8 // UserAdmin.java - Original library source code
 9 …
10 public void addUser(String userName) {
11     User user = new User(userName);
12     userMap.put(userName, user);
13 }
```

简而言之，addUser() 方法没有返回值变更成为返回 User 类实例的的方法。但是应用程序代码并没有发生任何改变，因为它没有使用 addUser() 方法的返回值

> 乍一看，com.nhn.user.UserAdmin.addUser() 方法看似仍然存在，但如果这样，**为什么还会出现 NoSuchMethodError 呢？**

#### 原因

原因是应用程序代码已经被编译到一个新的库。换句话说，应用程序代码似乎会调用方法而不管返回值。但是，编译的类文件中方法是带有返回值的。

可以看到以下的错误消息：

```
 1 java.lang.NoSuchMethodError: com.nhn.user.UserAdmin.addUser(Ljava/lang/String;)V
```

*NoSuchMethodError* 出现是因为 “com.nhn.user.UserAdmin.addUser(Ljava/lang/String;)V” 方法找不到。观察 “Ljava/lang/String;” 以及最后一个字母 “V” 。在 Java 字节码表达式中，“L;” 是类实例。这代表 addUser() 方法返回了一个 java/lang/String 对象作为参数。在这个例子的类库中，参数并没有发生改变，这是正常的。消息中最后的 “V” 代表方法的返回值。在 Java 字节码表达式中，“V” 表示没有返回值。简而言之，错误消息是说一个 java.lang.String 对象作为参数返回，而没有任何返回值的 com.nhn.user.UserAdmin.addUser 方法没有找到。

因为应用程序代码被编译到了之前的类库，所以类文件定义调用方法的返回值应该是 “V” 。然而，在已经改变的类库中，返回 “V” 的方法并不存在，而是一个返回 “Lcom/nhn/user/User” 的方法。因此，会出现错误NoSuchMethodError 。

> 注意
>
> 错误出现是因为开发者并没有重新编译新的类。但在这个例子中，类库提供者有很大的责任。这个公有方法没有返回值，但被修改成了返回用户类实例的方法。这显然是因为方法签名改变所导致的，这也意味着类的向后兼容被破坏了。因此类库提供方必须告知用户方法已经发生了改变。

让我们回到 Java 字节码。**Java 字节码** 是 JVM 的重要元素。JVM 是一个模拟器，它运行 Java 字节码。Java 编译器并不像 C/C++ 那样直接将高级语言转换成为机器语言（直接的 CPU 指令）；它将 Java 语言转换成为 JVM 可以理解的 Java 字节码。因为 Java 字节码没有任何依赖于平台的代码，它可以在任意安装好 JVM （准确的说是 JRE）的硬件上运行，即使当 CPU 或 OS 不同时也是如此（在 Windows PC 上开发和编译出的类文件也可以在 Linux 机器上运行，无须任何改变）。编译好的文件的大小与源码的大小几乎一样，这让通过网络来传输和运行编译的代码变得简单。

类文件本身是一个二进制文件，人们无法理解。为了管理类文件，JVM 提供商提供了 **javap** ，反编译工具。javap 生成的结果称为 Java 汇编语言。在上面的例子中，通过 javap -c 命令反编译应用程序代码中的 UserService.add() 方法得到的 Java 汇编语言如下：

```
1 public void add(java.lang.String);
2   Code:
3    0:   aload_0
4    1:   getfield        #15; //Field admin:Lcom/nhn/user/UserAdmin;
5    4:   aload_1
6    5:   invokevirtual   #23; //Method com/nhn/user/UserAdmin.addUser:(Ljava/lang/String;)V
7 	 8:   return
```

在 Java 汇编语言中，addUser() 方法在第四行被调用，"5: invokevirtual #23;" 。这表示索引 23 对应的方法会被调用。** invokevirtual ** 是操作码，它是 Java 字节码中调用方法的的最基本命令。参考 Java 字节码中的以下四种操作码： *invokeinterface* 、 *invokespecial* 、 *invokestatic* 和 *invokevirtual* 。每个操作码的意义如下：

- **invokeinterface** ：调用接口方法
- **invokespecial** ：调用初始化方法，私有方法，或父类中的方法
- **invokestatic** ：调用静态方法
- **invokevirtual** ：调用实例方法

Java 字节码指令集包括操作码和操作数。如 invokevirtual 这种操作码需要 2 字节的操作数。

可以先编译应用程序代码然后反编译它，得到以下结果：

```
1 public void add(java.lang.String);
2   Code:
3    0:   aload_0
4    1:   getfield        #15; //Field admin:Lcom/nhn/user/UserAdmin;
5    4:   aload_1
6    5:   invokevirtual   #23; //Method com/nhn/user/UserAdmin.addUser:(Ljava/lang/String;)Lcom/nhn/user/User;
7    8:   pop
8    9:   return
```

可以看到 #23 对应方法的返回值是 “Lcom/nhn/user/User;”。

> **在以上反编译的结果中，在代码前面的数字表示什么呢？**

它是字节数。或许这也是在 JVM 里执行的代码被称为“字节”码的原因。简而言之，操作码的字节码指令如 *aload_0* 、 *getfield* 和 *invokevirtual* 用 1 个字节的字节数来表示（aload_0 = 0x2a、getfield = 0xb4、invokevirtual = 0xb6）。因此，Java 字节码指令的操作码的最大值是 256 。

操作码如 aload_0 和 aload_1 不需要任何操作数。因此，aload_0 的下一个字节是操作码的下一个指令。但是，getfield 和 invokevirtual 需要 2 个字节的操作数。因此，getfield 的下一指令的第一个字节通过跳过 2 个字节写到第四个字节。字节码通过十六进制的编辑器显示如下：

```
1 2a b4 00 0f 2b b6 00 17 57 b1
```

在 Java 字节码中，类实例用 “L” 表示；void 用 “V” 来表示。以这种方式，其他的类型也都有他们自己的表达式。下面列表总结了这些表达式：

##### 表 1：Java 字节码类型表达式

| Java 字节码 | 类型      | 描述           |
| ----------- | --------- | -------------- |
| B           | byte      | 有符号字节     |
| C           | char      | Unicode 字符   |
| D           | double    | 双精度浮点数值 |
| F           | float     | 单精度浮点数值 |
| I           | int       | 整型           |
| J           | long      | 长整型         |
| L           | reference | 类的实例       |
| S           | short     | 有符号的短型   |
| Z           | boolean   | 真或假         |
| [           | reference | 一维数组       |

##### 表 2：Java 字节码表达式示例

| Java 代码                                  | Java 字节码表达式                        |
| ------------------------------------------ | ---------------------------------------- |
| double d[][][];                            | [[[D                                     |
| Object mymethod(int I, double d, Thread t) | (IDLjava/lang/Thread;)Ljava/lang/Object; |

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