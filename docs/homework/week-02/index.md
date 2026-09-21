##  本周完成情况：

以完成了最终项目选题，将最终项目定为“宠物医院管理系统”，同时按照本周作业要求，我简单构建了这个项目的部分领域类包括：`PetOwner` 宠物主人领域类、`Pet` 宠物领域类、`Doctor` 医生领域类、`Appointment` 就诊预约领域类，同时构建了两个枚举类：`PetType` 宠物种类枚举、`AppointmentStatus` 预约状态枚举。



## 测试命令输出：

konglei@SHIBEI:~/microservice/microservices-practice-2412190122$ mvn test
[INFO] Scanning for projects...
[INFO]
[INFO] ----------------< com.example:pet-hospital-homework02 >-----------------
[INFO] Building Pet Hospital Homework 02 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- resources:3.4.0:resources (default-resources) @ pet-hospital-homework02 ---
[INFO] skip non existing resourceDirectory /home/konglei/microservice/microservices-practice-2412190122/src/main/resources
[INFO]
[INFO] --- compiler:3.15.0:compile (default-compile) @ pet-hospital-homework02 ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 6 source files with javac [debug release 17] to target/classes
[INFO]
[INFO] --- resources:3.4.0:testResources (default-testResources) @ pet-hospital-homework02 ---
[INFO] skip non existing resourceDirectory /home/konglei/microservice/microservices-practice-2412190122/src/test/resources
[INFO]
[INFO] --- compiler:3.15.0:testCompile (default-testCompile) @ pet-hospital-homework02 ---
[INFO] Recompiling the module because of changed dependency.
[INFO] Compiling 3 source files with javac [debug release 17] to target/test-classes
[INFO]
[INFO] --- surefire:3.5.2:test (default-test) @ pet-hospital-homework02 ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO]
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.practice.AppointmentTest
[正常场景] 预约状态从待确认依次流转为已确认和已完成
[异常或非法输入] 为不可接诊的医生创建预约时抛出异常
[异常或非法输入] 未确认的预约直接完成时抛出异常
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.060 s -- in com.example.practice.AppointmentTest
[INFO] Running com.example.practice.PetOwnerTest
[正常场景] 添加宠物后完成查询、类型筛选和数量统计
[异常或非法输入] 添加属于其他主人的宠物时抛出异常
[异常或非法输入] 添加重复宠物编号时抛出异常
[边界场景] 主人没有宠物时数量为 0，查询和筛选返回空结果
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 s -- in com.example.practice.PetOwnerTest
[INFO] Running com.example.practice.PetTest
[边界场景] 宠物年龄为 0 时可以正常创建
[异常或非法输入] 宠物年龄为负数时抛出异常
[异常或非法输入] 宠物名称为空白时抛出异常
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in com.example.practice.PetTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.888 s
[INFO] Finished at: 2026-09-21T16:49:56+08:00
[INFO] ------------------------------------------------------------------------



##  遇到的问题

1. 一开始把 `pom.xml` 放进了 `src` 文件夹，运行 `mvn test` 时一直提示找不到项目配置文件。
2. 测试运行后虽然显示全部通过了，但终端只显示测试数量，看不出每个测试具体测了什么。
3. 刚开始设计领域类时，不太确定应该选择哪些对象和属性，既要满足本次作业要求，又不能一次把整个宠物医院系统做得过于复杂。