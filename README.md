
This processes a cobertura formatted XML report against coverage requirements.  This is for use with [Testacular] (http://url.com/ "Title")
which generates a cobertura formatted report but does not check coverage by itself.

[example](http://url.com/ "Title")
Testacular does not allow complete control over the directory which outputs its reports.  It creates subdirectories
named by browser, browser version, and operating system.  maven properties and profiles can handle these situations.

the standard cobertura maven plugin does have a check goal, but it was unusable against previously generated reports.

Target report DTD: http://cobertura.sourceforge.net/xml/coverage-04.dtd

The sample directory contains a maven pom that exercises the plugin.  Please see it for more details.

Sample Usage:
                        <configuration>
                            <!-- note that testacular will create multiple sub directories
                                 based on browser and operating system.  These need to be configured.
                                 An example is this sub directory for executing in Phantom JS on a mac.

                                     'PhantomJS 1.7 (Mac)'

                                     To handle running the build on different platforms, use profile overrides
                                 -->
                            <inputFile>${project.build.dir}/jstest/${cobertura.report.dir}/coverage.xml</inputFile>
                            <skip>false</skip>
                            <haltOnFailure>true</haltOnFailure>

                            <!-- apply these to coverage element of report total -->
                            <totalLineRate>1.0</totalLineRate>
                            <totalBranchRate>1.0</totalBranchRate>

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
                                <!-- override for specific package -->
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


