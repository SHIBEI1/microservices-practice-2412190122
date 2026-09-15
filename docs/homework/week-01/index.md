## 环境检查

**终端输出：**

openjdk 26.0.2 2026-07-21
OpenJDK Runtime Environment (build 26.0.2+10-2-26.04.2-Ubuntu)
OpenJDK 64-Bit Server VM (build 26.0.2+10-2-26.04.2-Ubuntu, mixed mode, sharing)
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: /opt/maven
Java version: 26.0.2, vendor: Ubuntu, runtime: /usr/lib/jvm/java-26-openjdk-amd64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "6.6.87.2-microsoft-standard-wsl2", arch: "amd64", family: "unix"
git version 2.53.0
Client:
 Version:           29.8.0
 API version:       1.56
 Go version:        go1.26.8
 Git commit:        88096ef
 Built:             Thu Sep  3 21:49:51 2026
 OS/Arch:           linux/amd64
 Context:           default

Server: Docker Desktop 4.91.0 (239619)
 Engine:
  Version:          29.8.0
  API version:      1.56 (minimum version 1.40)
  Go version:       go1.26.8
  Git commit:       3ce5872
  Built:            Thu Sep  3 21:51:20 2026
  OS/Arch:          linux/amd64
  Experimental:     false
 containerd:
  Version:          v2.3.4
  GitCommit:        db8809540e1a7a9da5d518876894933ff55692ab
 runc:
  Version:          1.4.3
  GitCommit:        v1.4.3-0-gbb14dabe
 docker-init:
  Version:          0.19.0
  GitCommit:        de40ad0
Docker Compose version v5.5.1

**终端截图：**

![image-20260915163601181](C:\Users\32212\AppData\Roaming\Typora\typora-user-images\image-20260915163601181.png)



## 概念回答

1.微服务架构是一种将单一应用程序划分为一组小型服务的架构风格。每个服务围绕特定业务能力构建，独立开发、部署、运行和扩展，服务之间通过轻量级通信机制交互，并可各自采用不同的技术栈和数据存储。
2.单体架构将所有功能集中在一个应用中，统一开发、部署和扩展，模块间耦合度高；微服务则将系统拆分为多个独立服务，每个服务可单独开发、部署和扩展，技术异构性强，但带来了分布式复杂性、服务治理和运维成本。
3.先实现单体系统有助于理解完整业务需求和系统整体结构，降低初学阶段的分布式复杂度；在单体基础上逐步拆分，可以直观对比两种架构的差异，理解拆分的时机、边界和代价。
4.可重复运行的测试或验证脚本能保证作业结果客观、可验证，方便老师快速检查功能是否正确；同时促使学生养成自动化测试和可复现实验的习惯，减少环境差异和手工操作带来的误差，也便于后续在微服务拆分过程中持续验证各服务行为是否保持一致。



## 问题记录

无