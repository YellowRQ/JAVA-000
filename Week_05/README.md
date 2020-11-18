# 作业

## **Week05 作业题目（周四）：**

### **1.（选做）**使 Java 里的动态代理，实现一个简单的 AOP。

### **2.（必做）**写代码实现 Spring Bean 的装配，方式越多越好（XML、Annotation 都可以）, 提交到 Github。

### **3.（选做）**实现一个 Spring XML 自定义配置，配置一组 Bean，例如：Student/Klass/School。

以上三个作业都在org.example.spring.homework1中完成了

#### 	(1) 注解

```java
//切面类
@Aspect
public class MyAspectJ1 {

    @Pointcut(value = "execution(* org.example.spring.homework1.aspectJ.Klass.study(..))")
    public void point(){}

    @Before(value = "point()")
    public void before() { System.out.println("============== MyAspectJ1 before=============="); }

    @After(value = "point()")
    public void after() {  System.out.println("============== MyAspectJ1 after==============");  }

    @Around(value = "point()")
    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("============== MyAspectJ1 around start==============");
        joinPoint.proceed();
        System.out.println("============== MyAspectJ1 around end==============");
    }
}
```

```XML
<bean id="myAspectJ1" class="org.example.spring.homework1.aspectJ.MyAspectJ1"/>
```



```java
//目标类
public class Klass {
    private int id;
    private List<Student> students;

    public void study() {
        System.out.println(getStudents() + "在学习...");
    }
}
```

```xml
<bean id="student001"
      class="org.example.spring.homework1.aspectJ.Student">
    <property name="id" value="001" />
    <property name="name" value="吴宣仪" />
</bean>

<bean id="student002"
      class="org.example.spring.homework1.aspectJ.Student">
    <property name="id" value="002" />
    <property name="name" value="程潇" />
</bean>

<bean id="class1" class="org.example.spring.homework1.aspectJ.Klass">
    <property name="id" value="111"/>
    <property name="students">
        <list>
            <ref bean="student001" />
            <ref bean="student002" />
        </list>
    </property>
</bean>
```



```java
@Test
public void cglibTest() {
    Klass class1 = context.getBean(Klass.class);
    class1.study();
    System.out.println("Klass对象AOP代理后的实际类型："+class1.getClass());
    System.out.println("Klass对象AOP代理后的实际类型是否是Klass子类："+(class1 instanceof Klass));
}
```

```
输出：
============== MyAspectJ1 around start==============
============== MyAspectJ1 before==============
[Student(id=1, name=吴宣仪), Student(id=2, name=程潇)]在学习...
============== MyAspectJ1 around end==============
============== MyAspectJ1 after==============
Klass对象AOP代理后的实际类型：class org.example.spring.homework1.aspectJ.Klass$$EnhancerBySpringCGLIB$$a05bd1ec
Klass对象AOP代理后的实际类型是否是Klass子类：true
```



#### 	(2)XML

```java
//切面类
public class MyAspectJ2 {

    public void before() { System.out.println("==============MyAspectJ2 before==============");  }

    public void after() {  System.out.println("==============MyAspectJ2 after=============="); }

    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("==============MyAspectJ2 around start==============");
        joinPoint.proceed();
        System.out.println("==============MyAspectJ2 around end==============");
    }
}
```

```xml
<aop:aspectj-autoproxy  />
<bean id="myAspectJ2" class="org.example.spring.homework1.aspectJ.MyAspectJ2"/>
<aop:config>
    <aop:pointcut id="point" expression="execution(* org.example.spring.homework1.aspectJ.School.ding(..))"/>
    <aop:aspect ref="myAspectJ2">
        <aop:around method="around" pointcut-ref="point"/>
        <aop:before method="before" pointcut-ref="point"/>
        <aop:after method="after" pointcut-ref="point"/>
    </aop:aspect>
</aop:config>
```



```java
@Data
public class School implements ISchool {

    @Autowired(required = true)
    private Klass klass;

    @Resource(name = "student001")
    private Student stu;

    @Override
    public void ding() {
        System.out.println("Class1 have " + this.klass.getStudents().size() + " students and one is " + this.stu);
    }
}
```

```java
@Test
public void jdkTest() {
    ISchool school = context.getBean(ISchool.class);
    System.out.println(school);
    school.ding();
    System.out.println("ISchool接口的对象AOP代理后的实际类型："+school.getClass());
}
```

```
School(klass=Klass(id=111, students=[Student(id=1, name=吴宣仪), Student(id=2, name=程潇)]), stu=Student(id=1, name=吴宣仪))
==============MyAspectJ2 around start==============
==============MyAspectJ2 before==============
Class1 have 2 students and one is Student(id=1, name=吴宣仪)
==============MyAspectJ2 around end==============
==============MyAspectJ2 after==============
ISchool接口的对象AOP代理后的实际类型：class com.sun.proxy.$Proxy16
```





## **Week05 作业题目（周六）：**


**4.（必做）**给前面课程提供的 Student/Klass/School 实现自动配置和 Starter。

代码位置: org.example.spring.homework2.configStart


**6.（必做）**研究一下 JDBC 接口和数据库连接池，掌握它们的设计和用法：
1）使用 JDBC 原生接口，实现数据库的增删改查操作。
2）使用事务，PrepareStatement 方式，批处理方式，改进上述操作。
3）配置 Hikari 连接池，改进上述操作。提交代码到 Github。

代码位置: org.example.spring.homework2