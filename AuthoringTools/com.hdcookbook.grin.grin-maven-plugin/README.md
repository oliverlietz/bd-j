# Grin Maven Plugin

compiles GRIN show files from text to binary and makes optimized mosaics from shows

  <build>
    <plugins>
      <plugin>
        <groupId>com.hdcookbook.grin</groupId>
        <artifactId>com.hdcookbook.grin.grin-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>compile</goal>
            </goals>
            <configuration>
              <showFiles>
                <showFile>show.txt</showFile>
              </showFiles>
            </configuration>
          </execution>
        </executions>
      </plugin>
      [...]
    </plugins>
  </build>
