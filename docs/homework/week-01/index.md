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



## 概念回答

**1. 什么是微服务架构？**

我理解的微服务架构，就是不再把项目做成一个大而全的 "大应用"，而是按业务功能拆成一个个独立的小服务。比如用户、订单、支付各自是一个服务，它们可以分别开发、分别部署、分别运行，一个出问题了不影响其他服务；服务之间通过网络通信，每个服务还能选自己合适的语言和技术栈，数据也各管各的。

**2. 微服务和单体架构的主要区别？**

单体架构是把所有功能都放在一个应用里：一个代码库、一起打包、一起部署，好处是简单直接，但模块之间耦合很紧，改一处可能影响整体，扩容也只能整个应用一起扩。微服务则把系统拆成多个独立小服务，每个都能单独开发、部署、扩容，技术上各用各的，灵活很多。代价是系统复杂度上去了：服务之间的通信、数据一致性、监控运维这些都要额外处理，对初学者来说上手难度明显更高。

**3. 为什么本课程先实现单体，再逐步拆分为微服务？**

我觉得这个顺序很合理。刚开始学的时候，业务和代码结构都不熟，如果一上来就面对服务发现、网关、负载均衡这一堆分布式概念，很容易被劝退。先做一个单体，能把业务流程、模块划分这些基础先吃透；等单体跑通了，再逐步把某个模块拆成独立服务，就能直观对比 "拆之前" 和 "拆之后" 的区别，也更清楚哪些部分适合拆、拆了会带来什么额外成本，而不是凭空理解微服务。

**4. 为什么作业需要提供可重复运行的测试或验证脚本？**

因为作业是给别人看的，只说 "我测过了" 没有说服力，别人也没法复现。提供一个能直接运行的脚本，老师一条命令就能验证功能对不对，结果客观、可查。对自己来说，把验证过程脚本化也是好习惯：以后每次改代码、每次拆分服务，都能用同一个脚本快速确认 "改动有没有把原本好的功能弄坏"，不用反复手工操作，也避免 "我这边能跑、你那边跑不了" 这种环境差异问题。



## 问题记录

无