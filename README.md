# BD-J - Blu-ray Disc Java

![Blu-ray Disc™](http://blu-raydisc.com/Images/bdalogo.png)

This repo contains mainly [mavenized](http://maven.apache.org/) tools from the [**HD Cookbook**](http://java.net/projects/hdcookbook/) project. To build the tools, [set up](./etc/build.sh) your [*BD-J Platform Definition*](http://java.net/projects/hdcookbook/pages/BDJPlatformDefinition) and run `mvn clean install` in [`AuthoringTools`](./AuthoringTools) and [`DiscCreationTools`](./DiscCreationTools) respectively.

### Fixed in 2021

The JAR signer tool was not working with JDK 1.8.  This has been fixed.  Please see the
release at https://github.com/zathras/java.net/releases/tag/1.0.1 , and the archive of the web content at https://hdcookbook.jovial.com/ 

### known issues
* `com.hdcookbook.grin.io.xml` is broken ([compilation failure](https://gist.github.com/1916339))
* `net.java.bd.tools.bdview` is broken ([runtime failure](https://gist.github.com/1916340))

Trademark Notice:
Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Blu-ray Disc™, Blu-ray™, Blu-ray 3D™, BD-Live™, BONUSVIEW™, BDXL™, AVCREC™, and the logos are trademarks of Blu-ray Disc Association.
Other names may be trademarks of their respective owners.
