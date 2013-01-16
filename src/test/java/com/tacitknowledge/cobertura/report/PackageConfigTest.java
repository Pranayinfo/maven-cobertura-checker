package com.tacitknowledge.cobertura.report;

import org.junit.Assert;
import org.junit.Test;


import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: mshort
 * Date: 1/16/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class PackageConfigTest {

    @Test
    public void testSortOrder() {
        SortedSet<PackageConfig> packageConfigs = new TreeSet<PackageConfig>();
        final PackageConfig packageConfig = new PackageConfig("java.lang", 0.0, 0.0);
        packageConfigs.add(packageConfig);
        final PackageConfig wildCardPackage = new PackageConfig("java.lang.*", 0.0, 0.0);
        packageConfigs.add(wildCardPackage);
        Assert.assertSame("should have moved wildcard to first position",wildCardPackage, packageConfigs.iterator().next());

    }
}
