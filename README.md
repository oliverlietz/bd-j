# BD-J - Blu-ray Disc Java

![Blu-ray Disc™](http://blu-raydisc.com/Images/bdalogo.png)

This repo contains mainly [mavenized](http://maven.apache.org/) tools from the [**HD Cookbook**](http://java.net/projects/hdcookbook/) project. To build the tools, [install](http://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html) your [*BD-J Platform Definition*](http://java.net/projects/hdcookbook/pages/BDJPlatformDefinition) with `mvn install:install-file -Dfile=/path/to/classes.zip -DgroupId=bdj -DartifactId=bdj -Dversion=1.0 -Dpackaging=jar` in your local [Maven](http://maven.apache.org/) repo and run `mvn clean install` in [`AuthoringTools`](/oliverlietz/bd-j/tree/master/AuthoringTools) and [`DiscCreationTools`](/oliverlietz/bd-j/tree/master/DiscCreationTools) respectively.

### known issues
* `com.hdcookbook.grin.io.xml` is broken ([compilation failure](https://gist.github.com/1916339))
* `net.java.bd.tools.bdview` is broken ([runtime failure](https://gist.github.com/1916340))

Trademark Notice:
Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Blu-ray Disc™, Blu-ray™, Blu-ray 3D™, BD-Live™, BONUSVIEW™, BDXL™, AVCREC™, and the logos are trademarks of Blu-ray Disc Association.
Other names may be trademarks of their respective owners.
