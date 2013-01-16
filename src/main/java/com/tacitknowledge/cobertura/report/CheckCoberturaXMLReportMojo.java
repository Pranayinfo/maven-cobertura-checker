package com.tacitknowledge.cobertura.report;

/*
 * Copyright 20.00.01-20.00.05 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;
import java.util.Collection;

/**
 * Goal which reads and evaluates a cobertura xml file, dtd version 4 Created to support halting on failures for
 * coverage.xml generated by Testacular, which does not have a .ser
 *
 * Please note that this implementation does not support full regex expressions for package names
 * It only supports final wildcards e.g. java.lang.*
 *
 * This targets the http://cobertura.sourceforge.net/xml/coverage-04.dtd version of the report
 */
@Mojo(
    name = "check",
    defaultPhase = LifecyclePhase.VERIFY
)
public class CheckCoberturaXMLReportMojo
    extends AbstractMojo
{
    @Parameter(
        required = true,
        readonly = true,
        defaultValue = "${project}"
    )
    private MavenProject project;

    /**
     * Location of the file.
     */
    @Parameter(
        property = "cobertura.checker.input.file",
        required = true
    )
    private File inputFile;

    @Parameter(
            alias = "packages"
    )
    private List<PackageConfig> packageConfigs;

    @Parameter(
        alias = "branchRate",
        property = "cobertura.checker.branch.rate"
    )
    private Double minBranch = 0.0;

    @Parameter(
        alias = "lineRate",
        property = "cobertura.checker.line.rate"
    )
    private Double minLine = 0.0;

    @Parameter(
        alias = "totalBranchRate",
        property = "cobertura.checker.total.branch.rate"
    )
    private Double minTotalBranch = 0.0;

    @Parameter(
        alias = "totalLineRate",
        property = "cobertura.checker.total.line.rate"
    )
    private Double minTotalLine = 0.0;

    @Parameter(
        alias = "haltOnFailure",
        property = "cobertura.checker.halt.on.failure"
    )
    private Boolean haltOnFailure = Boolean.TRUE;

    @Parameter(
        alias = "skip",
        property = "cobertura.checker.skip"
    )
    private boolean skip = false;

    public void execute() throws MojoExecutionException
    {
        if( skipMojo() )
        {
            return;
        }

        CheckReport checkReport = new CheckReport();
        if (!inputFile.exists())
        {
            throw new MojoExecutionException("File [" + inputFile.getAbsolutePath() + "] not found");
        }
        checkReport.setXMLDataFile(inputFile);
        checkReport.setMinimumGlobalBranchRatePerPackage(minBranch);
        checkReport.setMinimumGlobalLineRatePerPackage(minLine);
        checkReport.setMinimumTotalBranchRate(minTotalBranch);
        checkReport.setMinimumTotalLineRate(minTotalLine);
        
        if ( packageConfigs != null )
        {
            for ( PackageConfig packageConfig : packageConfigs )
            {
                checkReport.addPackageConfig(packageConfig);
            }
        }

        Collection<CoverageResult> results = checkReport.execute();

        if ( !results.isEmpty() )
        {
            getLog().error("These packages failed to meet unit test coverage requirements.");
            for ( CoverageResult result : results )
            {
                getLog().error("\t" + result.getPackageName());
            }
            if ( haltOnFailure )
            {
                throw new MojoExecutionException("Unit test coverage failed!!");
            }
        }
        else
        {
            getLog().info("No coverage failures detected.");
        }
    }

    /**
     * <p>Determine if the mojo execution should get skipped.</p>
     * This is the case if:
     * <ul>
     *   <li>{@link #skip} is <code>true</code></li>
     *   <li>if the mojo gets executed on a project with packaging type 'pom'</li>
     * </ul>
     *
     * @return <code>true</code> if the mojo execution should be skipped.
     */
    protected boolean skipMojo()
    {
        if ( skip )
        {
            getLog().info( "Skipping cobertura checker execution" );
            return true;
        }

        if ( "pom".equals( this.project.getPackaging() ) )
        {
            getLog().info( "Skipping cobertura mojo for project with packaging type 'pom'" );
            return true;
        }

        return false;
    }
}
