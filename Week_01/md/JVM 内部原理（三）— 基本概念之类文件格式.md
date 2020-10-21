# JVM 内部原理（三）— 基本概念之类文件格式

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

### 类文件格式（Class File Format）

在解释 Java 类文件格式之前，让我们先查看一个 Java Web 应用程序经常出现的状况。

#### 症状

当在 Tomcat 上运行 JSP 时，JSP 并没有运行，而是出现一下错误。

```
1 Servlet.service() for servlet jsp threw exception org.apache.jasper.JasperException: Unable to compile class for JSP Generated servlet error:
2 The code of method _jspService(HttpServletRequest, HttpServletResponse) is exceeding the 65535 bytes limit"
```

#### 原因

以上的错误消息提示会因为 Web 应用服务器不同而有些许差异，但有有样事情是一样的：那就是 65535 字节的限制。65535 字节的限制是由于 JVM 的限制规定，那就是 **一个方法的大小不可以超过 65535 字节**。

我会解释 65535 字节的限制以及为什么会有这样的限制。

Java 字节码使用的 branch/jump 指令是 “goto” 和 “jsr” 。

```
1 goto [branchbyte1] [branchbyte2]
2 jsr [branchbyte1] [branchbyte2]
```

两者都接收一个 2 字节有符号的 branch 偏移量作为他们的操作数，这样它可以扩展到最大 65535 索引。然而，为了支持足够的 branch ，Java 字节码提供了 “goto_w” 和 “jsr_w” 接收 4 字节有符号的 branch 偏移量。

```
1 goto_w [branchbyte1] [branchbyte2] [branchbyte3] [branchbyte4]
2 jsr_w [branchbyte1] [branchbyte2] [branchbyte3] [branchbyte4]
```

有了这两条指令，branch 可以提供超过 65535 的地址索引，这样就可以解决对于 Java 方法 65535 字节的大小的限制。然而，由于 Java 类文件格式其他诸多方面的限制，Java 方法仍然无法超过 65535 字节。为了解释其他的这些限制，我会通过类文件格式来进行简单的说明。

一个 Java 类文件结构如下：

```
 1 ClassFile {
 2     u4 magic; 
 3     u2 minor_version;
 4     u2 major_version;
 5     u2 constant_pool_count;
 6     cp_info constant_pool[constant_pool_count-1];
 7     u2 access_flags;
 8     u2 this_class;
 9     u2 super_class;
10     u2 interfaces_count;
11     u2 interfaces[interfaces_count];
12     u2 fields_count;
13     field_info fields[fields_count];
14     u2 methods_count;
15     method_info methods[methods_count];
16     u2 attributes_count;
17     attribute_info attributes[attributes_count];}
```

首 16 字节 UserService.class 文件反编译后的十六进制显示如下：

```
ca fe ba be 00 00 00 32 00 28 07 00 02 01 00 1b
```

将这个值与类文件格式一起查看。

- **magic**：类文件的前 4 字节的内容是魔数（magic number）。它的值已经预先指定好，用来区分 Java 类文件。在以上十六进制中，它的值始终是 0xCAFEBABE 。简而言之，当文件的前 4 字节是 0xCAFEBABE 时，那么它就是这个 Java 类文件。
- **minor_version,major_version**：后面接着的 4 字节表示类的版本。UserService.class 文件这个值是 0x00000032 ，类的版本是 50.0 。由 JDK1.6 编译的类文件版本是 50.0 ，有 JDK1.5 编译的类文件版本是 49.0 。JVM 必须对比它低版本编译的类文件保持向后兼容。另一方面，当更高版本的类文件在低版本的 JVM 中执行是，java.lang.UnsupportedClassVersionError 就会出现。
- **constant_pool_count,constant_pool[]**：在版本信息之后，是类的类型常量池信息。它的信息包括了运行时常量池区域，我们稍后对此进行解释。当装载类文件时，JVM 将常量池（constant_pool）的信息保存在方法区（method area）的运行时常量池区（Runtime Constant Pool area）。因为类文件 UserService.class 的 constant_pool_count 值为 0x0028 ，那么 constant_pool 有（40-1）个索引，即 39 个索引。
- **access_flags**：这个标志表示类的修饰符信息；换句话说，它表示 public、final、abstract、或是否为 interface 。
- **this_class,super_class**：类对应的 this 和 super 在常量池（constant_pool）中的索引。
- **interfaces_count,interfaces[]**：类实现接口的数量在常量池（constant_pool）中的索引，以及每个接口的索引。
- **fields_count,fields[]**：类中字段的数量以及字段的信息。字段的信息包括字段名、类型信息、修饰符、以及常量池的索引值。
- **methods_count,methods[]**：类中方法的数量以及类方法的信息。方法信息包括方法名、参数的类型及数量、返回类型、修饰符、常量池的索引值、方法执行的代码和异常信息。
- **attributes_count,attributes[]**：attribute_info 的结构包括多个不同的 attributes 。供 field_info、method_info 和 attribute_info 使用。

javap 反编译程序可以将类文件格式反编译为程序员可读的形式。使用 “javap -verbose” 分析 UserService.class 类，得到以下内容。

```
 1 Compiled from "UserService.java"
 2  
 3 public class com.nhn.service.UserService extends java.lang.Object
 4   SourceFile: "UserService.java"
 5   minor version: 0
 6   major version: 50
 7   Constant pool:const #1 = class        #2;     //  com/nhn/service/UserService
 8 const #2 = Asciz        com/nhn/service/UserService;
 9 const #3 = class        #4;     //  java/lang/Object
10 const #4 = Asciz        java/lang/Object;
11 const #5 = Asciz        admin;
12 const #6 = Asciz        Lcom/nhn/user/UserAdmin;;// … omitted - constant pool continued …
13  
14 {
15 // … omitted - method information …
16  
17 public void add(java.lang.String);
18   Code:
19    Stack=2, Locals=2, Args_size=2
20    0:   aload_0
21    1:   getfield        #15; //Field admin:Lcom/nhn/user/UserAdmin;
22    4:   aload_1
23    5:   invokevirtual   #23; //Method com/nhn/user/UserAdmin.addUser:(Ljava/lang/String;)Lcom/nhn/user/User;
24    8:   pop
25    9:   return  LineNumberTable:
26    line 14: 0
27    line 15: 9  LocalVariableTable:
28    Start  Length  Slot  Name   Signature
29    0      10      0    this       Lcom/nhn/service/UserService;
30    0      10      1    userName       Ljava/lang/String; // … Omitted - Other method information …
31 }
```

由于内容太长，这里只提取了部分信息。完整的内容提供了各种信息，包括常量池和每个方法体的内容。

方法 65535 字节的大小限制与 **method_info struct** 的内容相关。结构 method_info struct 里有代码（Code），行号表（LineNumberTable）和本地变量表（LocalVariableTable）属性，如上所示。所有代码内包括如行号表（LineNumberTable），本地变量表（LocalVariableTable）和异常表（exception_table）的属性值都是 2 字节的。因此，方法的大小不能超过行号表（LineNumberTable），本地变量表（LocalVariableTable）和异常表（exception_table）的长度，即 65535 字节。

许多人抱怨过这个方法大小的限制，JVM 规范上解释说 “可能以后会扩展” 。然而，到目前为止还没有明显的行动。考虑到 JVM 规范的特点通常在方法区加载类文件的内容相同，既要向后保持兼容又要能扩展方法的大小是十分困难的

> “如果是因为 Java 编译器的错误导致类文件不正确会怎么样？或者，如果因为网络传输或文件拷贝过程出现错误，类文件会被破坏？”

为了应对这种情形，Java 类装载器（class loader）检查过程是非常严格的。JVM 规范明确规定了这个过程。

> 注意
>
> 我们如何验证 JVM 成功执行了类文件的验证过程？如何验证来自不同 JVM 提供商的各种各样的 JVM 是否满足 JVM 规范的要求？为了验证，Oracle 提供了一个测试工具，TCK（Technology Compatibility Kit）。TCK 通过执行上万个测试来验证 JVM 规范是否能得到满足，包括很多不正确的类文件。如果能通过 TCK 的测试，那一个 JVM 才能称为 JVM 。
>
> 和 TCK 类似，JCP（Java Community Process; [http://jcp.org](https://www.cnblogs.com/richaaaard/p/6196354.html)）会提议新的 Java 技术文档以及 Java 语言规范。对于 JCP 来说，一个文档规范，参考实现，JSR（Java Specification Request）TCK 必须要完成以满足 JSR 。想要使用以 JSR 形式提议的新 Java 技术的用户需要遵守 RI 提供方的许可，或者直接实现它并用 TCK 对实现进行测试。

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