![license](https://img.shields.io/github/license/Jiankun-Zhang/Pyrgus?style=for-the-badge)

# Pyrgus

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b10df9554bbb41259ae2d409d1469cd1)](https://app.codacy.com/gh/Jiankun-Zhang/Pyrgus?utm_source=github.com&utm_medium=referral&utm_content=Jiankun-Zhang/Pyrgus&utm_campaign=Badge_Grade_Settings)

一个尽可能提供 [CQRS](https://microservices.io/patterns/data/cqrs.html)
风格的轻量级 [Messaging Patterns](https://www.enterpriseintegrationpatterns.com/patterns/messaging/) 实践框架.

## About The Project

在过去几年中我曾尝试寻找合适的框架以在 Java 世界中更好得实践 [Domain-Driven Design](https://en.wikipedia.org/wiki/Domain-driven_design).

开源的 [Axon Framework](https://developer.axoniq.io/axon-framework/overview) 似乎是个不错的选择, 只是在经过一些简单的体验后我觉得不够得心应手, 但它确实在 API
设计上启发了我很多, 于是我决定自己来实现一个框架:

* 足够轻量, 在 low-level 仅仅实现一个 Messaging Patterns 骨架;

* 实现一个参考自 [Hibernate Service Registry](https://docs.jboss.org/hibernate/orm/5.4/topical/html_single/registries/ServiceRegistries.html)
的简易容器以管理所有的组件;

* 在 high-level 实现 CQRS 风格的 API 接口;

* 提供过滤器和拦截器机制以便后期扩展;

* 支持从单体到微服务的无感迁移;

## Getting Started

TODO

### Installation

```shell
cd framework
mvn install
```

### Usage

TODO

## License

Pyrgus 以 [MIT](https://opensource.org/licenses/MIT) 协议发布.
