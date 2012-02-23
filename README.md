# BD-J - Blu-ray Disc Java

This repo contains mainly [mavenized](http://maven.apache.org/) tools from the [**HD Cookbook**](http://java.net/projects/hdcookbook/) project. To build the tools, [install](http://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html) your [*BD-J Platform Definition*](http://java.net/projects/hdcookbook/pages/BDJPlatformDefinition) with `mvn install:install-file -Dfile=/path/to/classes.zip -DgroupId=bdj -DartifactId=bdj -Dversion=1.0 -Dpackaging=jar` in your local [Maven](http://maven.apache.org/) repo and run `mvn clean install` in [`AuthoringTools`](bd-j/tree/master/AuthoringTools) and [`DiscCreationTools`](bd-j/tree/master/DiscCreationTools) respectively.


![Blu-ray Disc™](http://blu-raydisc.com/Images/bdalogo.png)

Trademark Notice:
Blu-ray Disc™, Blu-ray™, Blu-ray 3D™, BD-Live™, BONUSVIEW™, BDXL™, AVCREC™,
and the logos are trademarks of Blu-ray Disc Association.
