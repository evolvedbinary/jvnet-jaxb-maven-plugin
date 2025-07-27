# JAXB Maven Plugin (jvnet)

[![CI](https://github.com/evolvedbinary/maven-jaxb2-plugin/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/evolvedbinary/jvnet-jaxb-maven-plugin/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.evolvedbinary.maven.jvnet/jaxb-maven-plugin-project.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.evolvedbinary.maven.jvnet%22%20AND%20a:%22jaxb-maven-plugin-project%22)
[![License](https://img.shields.io/badge/license-BSD%202-blue.svg)](https://opensource.org/licenses/BSD-2-Clause)

* **Supports JAXB 2, JAXB 3, and JAXB 4.**

## Provenance
This is a fork of the JAXB2 Maven Plugin from [phax/maven-jaxb2-plugin](https://github.com/phax/maven-jaxb2-plugin),
which is itself a fork on [highsource/maven-jaxb2-plugin](https://github.com/highsource/maven-jaxb2-plugin), and we suspect
that was itself a fork of the [javaee/metro-maven-jaxb2-plugin](https://github.com/javaee/metro-maven-jaxb2-plugin) which originated
from *java.net*.

The plugin from which this was forked has been commonly known as the: *"jvnet JAXB2 Plugin"*.

The purpose of our fork is to add support for JAXB 3 and 4.

### Notes on Licensing
The forks from which this project came claim that it is licensed under a [BSD-2 Clause](https://opensource.org/licenses/BSD-2-Clause) license.
However, there is also evidence of Apache 2.0 licensed code within the project. The code in the original
*java.net* project appears instead to have been dual-licensed as [CDDL 1.1](https://spdx.org/licenses/CDDL-1.1.html) and [GPL 2 + Classpath Exception](https://openjdk.java.net/legal/gplv2+ce.html) license.

Whilst we believe it is likely that this code is Open Source and resides under a mix of the above discussed licenses, the exact licensing of
the code remains murky at best. 

## Introduction

Welcome to the JAXB Maven Plugin, the most advanced and feature-full Maven plugin for XML Schema compilation.

This Maven plugin wraps and enhances the [JAXB](https://jaxb.java.net/) [Schema Compiler (XJC)](http://docs.oracle.com/javase/6/docs/technotes/tools/share/xjc.html) and allows
compiling XML Schemas (as well as WSDL, DTDs, RELAX NG) into Java classes in Maven builds.

> If you are interested in the Mojohaus JAXB2 Maven Plugin (`org.codehaus.mojo:jaxb2-maven-plugin`),
> please follow [this link](https://github.com/mojohaus/jaxb2-maven-plugin) to the corresponding website.

## Quick start

* Put your schemas (`*.xsd`) and bindings (`*.xjb`) into the `src/main/resources` folder.
* Add the plugin to your `pom.xml`:

### For JAXB 4:
```xml
<project ...>
  ...
  <build>
    <plugins>
      ...
      <plugin>
        <groupId>com.evolvedbinary.maven.jvnet</groupId>
        <artifactId>jaxb40-maven-plugin</artifactId>
        <version>0.16.0</version>
        <executions>
          <execution>
            <goals>
              <goal>generate</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      ...
    </plugins>
  </build>
  ...
</project>
```

### For JAXB 3:
```xml
<project ...>
  ...
  <build>
    <plugins>
      ...
      <plugin>
        <groupId>com.evolvedbinary.maven.jvnet</groupId>
        <artifactId>jaxb30-maven-plugin</artifactId>
        <version>0.16.0</version>
        <executions>
          <execution>
            <goals>
              <goal>generate</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      ...
    </plugins>
  </build>
  ...
</project>
```

### For JAXB 2:
Use: `<artifactId>jaxb2-maven-plugin</artifactId>` instead.

### JAXB Versions

If you need a specific JAXB version, you can explicitly use one of the following variants:

* `com.evolvedbinary.maven.jvnet:jaxb40-maven-plugin` - JAXB 4.0.
* `com.evolvedbinary.maven.jvnet:jaxb30-maven-plugin` - JAXB 3.0.
* `com.evolvedbinary.maven.jvnet:jaxb23-maven-plugin` - JAXB 2.3.
* `com.evolvedbinary.maven.jvnet:jaxb22-maven-plugin` - JAXB 2.2.
* `com.evolvedbinary.maven.jvnet:jaxb2-maven-plugin` - "latest 2.x version"; at the moment this is same as `com.evolvedbinary.maven.jvnet:jaxb23-maven-plugin`.

### Java versions

Supported Java versions are `1.8` and onwards.

## [Documentation](https://github.com/highsource/maven-jaxb2-plugin/wiki)

Please refer to the [Wiki](https://github.com/highsource/maven-jaxb2-plugin/wiki) for the full documentation.

### Support
Whilst this fork is an Open Source project, developing this plugin is not
our key occupation. We are happy for users to open issues, however we make
no claims that we will investigate or resolve them.

We will gladly accept and merge Pull-Requests that address issues, but they
must be accompanied by tests and be compatible with Java 8+.

The original author of the plugin *may* also be able to offer you some support, see [Support](https://github.com/highsource/maven-jaxb2-plugin/wiki/Support).

* [User Guide](https://github.com/highsource/maven-jaxb2-plugin/wiki/User-Guide)
* Maven Documentation  (Work in progress)
* [Configuration Cheat Sheet](https://github.com/highsource/maven-jaxb2-plugin/wiki/Configuration-Cheat-Sheet)
* [Common Pitfalls and Problems](https://github.com/highsource/maven-jaxb2-plugin/wiki/Common-Pitfalls-and-Problems) (Work in progress)
* [Best Practices](https://github.com/highsource/maven-jaxb2-plugin/wiki/Best-Practices) (Work in progress)
* [FAQ](https://github.com/highsource/maven-jaxb2-plugin/wiki/FAQ)
* [Sample Projects](https://github.com/highsource/maven-jaxb2-plugin/wiki/Sample-Projects)
* [License](https://github.com/evolvedbinary/jvnet-jaxb-maven-plugin/blob/main/LICENSE)
