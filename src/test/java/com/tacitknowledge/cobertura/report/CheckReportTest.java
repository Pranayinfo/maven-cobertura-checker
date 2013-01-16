package com.tacitknowledge.cobertura.report;

import java.io.File;
import java.util.Collection;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for CheckCoberturaXMLReportMojo
 */
public class CheckReportTest
{
    @Test
    public void testCheckReportFailureOnLineRate() throws Exception {

        CheckReport checkReport = new CheckReport();
        checkReport.setMinimumTotalLineRate(5.0);
        checkReport.setXMLDataFile(new File("src/test/resources/coverage.xml"));

        Collection<CoverageResult> results = checkReport.execute();
        assertFalse(results.isEmpty());
    }

    @Test
    public void testCheckReportFailureOnBranchRate() throws Exception {

        CheckReport checkReport = new CheckReport();
        checkReport.setMinimumTotalBranchRate(5.0);
        checkReport.setXMLDataFile(new File("src/test/resources/coverage.xml"));

        Collection<CoverageResult> results = checkReport.execute();
        assertFalse(results.isEmpty());
    }

    @Test
    public void testCheckReportSuccess() throws Exception {

        CheckReport checkReport = new CheckReport();
        checkReport.setMinimumTotalLineRate(1.0);
        checkReport.setMinimumTotalBranchRate(1.0);
        checkReport.setXMLDataFile(new File("src/test/resources/coverage.xml"));

        assertTrue(checkReport.execute().isEmpty());
    }

    @Test
    public void testCheckReportPackageFailed() throws Exception {

        CheckReport checkReport = new CheckReport();
        checkReport.setMinimumGlobalLineRatePerPackage(1.0);
        checkReport.setMinimumGlobalBranchRatePerPackage(1.0);
        checkReport.setXMLDataFile(new File("src/test/resources/coverage.xml"));

        Collection<CoverageResult> coverageFailures = checkReport.execute();
        assertFalse(coverageFailures.isEmpty());
        String uncoveredPackage = "js-uncovered";
        assertTrue(coverageFailures.iterator().next().getPackageName().equals(uncoveredPackage));
    }

    @Test
    public void testCheckReportPackageConfigOverridesWithWildcard() throws Exception {

        CheckReport checkReport = new CheckReport();
        checkReport.setMinimumGlobalBranchRatePerPackage(5.0);
        checkReport.addPackageConfig(new PackageConfig("js.*",0.0,0.0));
        checkReport.setXMLDataFile(new File("src/test/resources/coverage.xml"));

        Collection<CoverageResult> coverageFailures = checkReport.execute();
        assertEquals(2,coverageFailures.size());
    }
    @Test
    public void testCheckReportPackageConfigOverridesNoWildcard() throws Exception {

        CheckReport checkReport = new CheckReport();
        checkReport.setMinimumGlobalBranchRatePerPackage(5.0);
        checkReport.addPackageConfig(new PackageConfig("js",0.0,0.0));
        checkReport.setXMLDataFile(new File("src/test/resources/coverage.xml"));

        Collection<CoverageResult> coverageFailures = checkReport.execute();
        assertEquals(2,coverageFailures.size());
    }


    @Test
    public void testCheckReportPackageConfigFailure() throws Exception {

        CheckReport checkReport = new CheckReport();
        checkReport.setMinimumGlobalBranchRatePerPackage(0.0);
        checkReport.addPackageConfig(new PackageConfig("js.*",2.0,2.0));
        checkReport.setXMLDataFile(new File("src/test/resources/coverage.xml"));

        Collection<CoverageResult> coverageFailures = checkReport.execute();
        assertFalse(coverageFailures.isEmpty());
        assertEquals(1,coverageFailures.size());

    }

}
