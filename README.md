
This processes a cobertura formatted XML report against coverage requirements.  This is for use with Testacular
which generates a cobertura formatted report but does not check coverage by itself.

the cobertura maven plugin does have a check goal, but it was unusable against generated reports.

Target report DTD: http://cobertura.sourceforge.net/xml/coverage-04.dtd

Sample Usage:
      <plugin>
        <groupId>com.tacitknowledge.cobertura.report</groupId>
        <artifactId>checker</artifactId>
        <version>1.0-SNAPSHOT</version>
        <executions>
          <execution>

          <id>process-js-coverage</id>
          <phase>verify</phase>
          <configuration>
            <inputFile>${project.build.directory}/jstest/PhantomJS 1.7 (Mac)/coverage.xml</inputFile>
            <skip>false</skip>
            <haltOnFailure>true</haltOnFailure>

            <!-- apply these to coverage element of report total -->
            <totalLineRate>0.0</totalLineRate>
            <totalBranchRate>0.0</totalBranchRate>

            <!-- apply these to each package -->
            <lineRate>0.0</lineRate>
            <branchRate>0.0</branchRate>

            <packages>
                <!-- override for packages specified by wildcard -->
                <PackageConfig>
                  <nameOrPrefix>java.util.*</nameOrPrefix>
                  <branchRate>0.5</branchRate>
                  <lineRate>0.5</lineRate>
                </PackageConfig>
                <!-- override for specific -->
                <PackageConfig>
                  <nameOrPrefix>java.util.concurrent</nameOrPrefix>
                  <branchRate>0.5</branchRate>
                  <lineRate>0.5</lineRate>
                </PackageConfig>
            </packages>


          </configuration>
          <goals>
            <goal>check</goal>
          </goals>
        </execution>
        </executions>
      </plugin>


