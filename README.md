<div align="center">
<img src="https://github.com/quarkiverse/quarkus-jasperreports/blob/main/docs/modules/ROOT/assets/images/quarkus.svg" width="67" height="70" ><img src="https://github.com/quarkiverse/quarkus-jasperreports/blob/main/docs/modules/ROOT/assets/images/plus-sign.svg" height="70" ><img src="https://github.com/quarkiverse/quarkus-jasperreports/blob/main/docs/modules/ROOT/assets/images/jasperreports.svg" height="70" >

# Quarkus JasperReports
</div>
<br>

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.jasperreports/quarkus-jasperreports?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.jasperreports/quarkus-jasperreports)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)
[![Build](https://github.com/quarkiverse/quarkus-jasperreports/actions/workflows/build.yml/badge.svg)](https://github.com/quarkiverse/quarkus-jasperreports/actions/workflows/build.yml)

A Quarkus extension that lets you utilize [JasperReports](https://community.jaspersoft.com/download-jaspersoft/community-edition/). JasperReports is an open source Java reporting tool that can write to a variety of targets, such as: screen, a printer, into PDF, HTML, Microsoft Excel, RTF, ODT, comma-separated values (CSV), XSL, or XML files.

## Getting started

Read the full [JasperReports documentation](https://docs.quarkiverse.io/quarkus-jasperreports/dev/index.html).

### Installation

Create a new JasperReports project (with a base jasperreports starter code):

- With [code.quarkus.io](https://code.quarkus.io/?a=jasperreports-bowl&j=17&e=io.quarkiverse.jasperreports%3Aquarkus-jasperreports)
- With the [Quarkus CLI](https://quarkus.io/guides/cli-tooling):

```bash
quarkus create app jasperreports-app -x=io.quarkiverse.jasperreports:quarkus-jasperreports
```
Or add to your pom.xml directly:

```xml
<dependency>
    <groupId>io.quarkiverse.jasperreports</groupId>
    <artifactId>quarkus-jasperreports</artifactId>
    <version>{project-version}</version>
</dependency>
```

## Docker

When building native images in Docker using the standard Quarkus Docker configuration files some additional features need to be installed to support fonts.  Specifically font information is not included in [Red Hat's ubi-minimal images](https://developers.redhat.com/products/rhel/ubi).  To install it
simply add these lines to your `DockerFile.native` file:

```shell
FROM registry.access.redhat.com/ubi8/ubi-minimal:8.9

######################### Set up environment for POI #############################
RUN microdnf update && microdnf install freetype fontconfig && microdnf clean all
######################### Set up environment for POI #############################

WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
# Shared objects to be dynamically loaded at runtime as needed,
COPY --chown=1001:root target/*.properties target/*.so /work/
COPY --chown=1001:root target/*-runner /work/application
# Permissions fix for Windows
RUN chmod "ugo+x" /work/application
EXPOSE 8080
USER 1001

CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
```

> [!CAUTION]
> Make sure `.dockerignore` does not exclude `.so` files!

## 🧑‍💻 Contributing

- Contribution is the best way to support and get involved in the community!
- Please, consult our [Code of Conduct](./CODE_OF_CONDUCT.md) policies for interacting in our community.
- Contributions to `quarkus-jasperreports` - please check our [CONTRIBUTING.md](./CONTRIBUTING.md)

### If you have any ideas or questions 🤷

- [Ask a question](https://github.com/quarkiverse/quarkus-jasperreports/discussions)
- [Raise an issue](https://github.com/quarkiverse/quarkus-jasperreports/issues)
- [Feature request](https://github.com/quarkiverse/quarkus-jasperreports/issues)
- [Code submission](https://github.com/quarkiverse/quarkus-jasperreports/pulls)

## Contributors ✨

Thanks go to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://melloware.com"><img src="https://avatars.githubusercontent.com/u/4399574?v=4?s=100" width="100px;" alt="Melloware"/><br /><sub><b>Melloware</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-jasperreports/commits?author=melloware" title="Code">💻</a> <a href="#maintenance-melloware" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://martinpanzer.de"><img src="https://avatars.githubusercontent.com/u/1223135?v=4?s=100" width="100px;" alt="Martin Panzer"/><br /><sub><b>Martin Panzer</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-jasperreports/commits?author=Postremus" title="Code">💻</a> <a href="#maintenance-Postremus" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/kauanmocelin"><img src="https://avatars.githubusercontent.com/u/3020794?v=4?s=100" width="100px;" alt="Kauan Mocelin"/><br /><sub><b>Kauan Mocelin</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-jasperreports/issues?q=author%3Akauanmocelin" title="Bug reports">🐛</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/nderwin"><img src="https://avatars.githubusercontent.com/u/3226831?v=4?s=100" width="100px;" alt="Nathan Erwin"/><br /><sub><b>Nathan Erwin</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-jasperreports/commits?author=nderwin" title="Documentation">📖</a> <a href="https://github.com/quarkiverse/quarkus-jasperreports/commits?author=nderwin" title="Code">💻</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
