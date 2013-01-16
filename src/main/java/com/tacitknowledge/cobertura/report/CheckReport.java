package com.tacitknowledge.cobertura.report;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Check Task
 */
public class CheckReport
{
    public File xmlDataFile;
    public Double minimumTotalBranchRate = 0.0;
    public Double minimumTotalLineRate = 0.0;
    public Double minimumGlobalBranchRatePerPackage = 0.0;
    public Double minimumGlobalLineRatePerPackage = 0.0;
    //use a sorted set so that specific package configs have precedence over wildcards
    private SortedSet<PackageConfig> packageConfigs = new TreeSet<PackageConfig>();

    public Set<CoverageResult> execute()
    {
        Set<CoverageResult> coverageResults = new HashSet<CoverageResult>();

        Document doc = createDocument();

        final String lineRateXPath = "/coverage[@line-rate<" + minimumTotalLineRate + "]/@line-rate";
        final String branchRateXPath = "/coverage[@branch-rate<" + minimumTotalBranchRate + "]/@branch-rate";
        final String packagesGlobalLineRateXpath = "/coverage/packages/package[@line-rate<"
            + minimumGlobalLineRatePerPackage + "]/@name";
        final String packagesGlobalBranchRateXpath = "/coverage/packages/package[@branch-rate<"
            + minimumGlobalBranchRatePerPackage + "]/@name";

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression totalLineRateExpression = createExpression(xpath, lineRateXPath);
        XPathExpression totalBranchRateExpression = createExpression(xpath, branchRateXPath);
        XPathExpression packagesGlobalLineRateExpression = createExpression(xpath, packagesGlobalLineRateXpath);
        XPathExpression packagesGlobalBranchRateExpression = createExpression(xpath,
            packagesGlobalBranchRateXpath);

        try
        {

            //check totals
            String lineRate = totalLineRateExpression.evaluate(doc);
            String branchRate = totalBranchRateExpression.evaluate(doc);

            if ( !branchRate.isEmpty() || !lineRate.isEmpty() )
            {
                CoverageResult totalResult = new CoverageResult("average over all packages");
                coverageResults.add(totalResult);
            }
            //check global package rules
            NodeList branchNodeList = (NodeList) packagesGlobalBranchRateExpression.evaluate(doc,
                XPathConstants.NODESET);
            NodeList lineNodeList = (NodeList) packagesGlobalLineRateExpression.evaluate(doc,
                XPathConstants.NODESET);
            addPackagesFailingFromNodeList(coverageResults, branchNodeList);
            addPackagesFailingFromNodeList(coverageResults, lineNodeList);

            //check package overrides, including wildcards
            for ( PackageConfig packageConfig : packageConfigs )
            {
                String nameOrPrefix = packageConfig.getNameOrPrefix();
                String lineSearchValue = null;
                String branchSearchValue = null;
                String removalSearch = null;

                //if wildcards, we'll use the starts-with xpath expression
                if ( nameOrPrefix.endsWith(".*") )
                {
                    nameOrPrefix = nameOrPrefix.substring(0, nameOrPrefix.lastIndexOf("*"));
                    //lineRate query using starts-with
                    lineSearchValue = "/coverage/packages/package[starts-with(@name,'" + nameOrPrefix +
                        "') and  @line-rate<" + packageConfig.getLineRate() + "]/@name";
                    //branch rate query using starts with
                    branchSearchValue = "/coverage/packages/package[starts-with(@name,'" + nameOrPrefix +
                        "') and @branch-rate<" + packageConfig.getBranchRate() + "]/@name";
                    removalSearch = "/coverage/packages/package[starts-with(@name,'" + nameOrPrefix +
                        "')]/@name";
                } else {
                    //line rate query for specific package
                    lineSearchValue = "/coverage/packages/package[@name='" + nameOrPrefix +
                        "' and  @line-rate<" + packageConfig.getLineRate() + "]/@name";

                    //branch rate query for specific package
                    branchSearchValue = "/coverage/packages/package[@name='" + nameOrPrefix +
                        "' and @branch-rate<" + packageConfig.getBranchRate() + "]/@name";

                    removalSearch = "/coverage/packages/package[@name='" + nameOrPrefix +
                        "']/@name";

                }

                //remove - override - global package entries if package is explicitly specified in config
                removeOverridenPackageFailures(coverageResults, doc, xpath, removalSearch);
                addPackagesFailingRegex(coverageResults, doc, xpath, lineSearchValue);
                addPackagesFailingRegex(coverageResults, doc, xpath, branchSearchValue);


            }
        }
        catch ( XPathExpressionException e )
        {
            throw new RuntimeException("bad expression", e);
        }

        return coverageResults;
    }

    private void removeOverridenPackageFailures(Set<CoverageResult> coverageResults, Document doc, XPath xpath,
        String removalString) throws XPathExpressionException
    {
        XPathExpression expression = xpath.compile(removalString);
        NodeList matchingPackages = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
        List<CoverageResult> removalList = new ArrayList<CoverageResult>();
        for ( int i = 0; i < matchingPackages.getLength(); i++ )
        {
            for ( CoverageResult coverageResult : coverageResults )
            {
                if (coverageResult.getPackageName().equals(matchingPackages.item(i).getTextContent()))
                {
                    removalList.add(coverageResult);
                }
            }
        }
        for (CoverageResult coverageResult : removalList) {
            coverageResults.remove(coverageResult);
        }
    }

    private void addPackagesFailingRegex(Set<CoverageResult> coverageResults, Document doc, XPath xpath,
        String searchValue) throws XPathExpressionException
    {
        XPathExpression expression = xpath.compile(searchValue);
        NodeList matchingPackages = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
        addPackagesFailingFromNodeList(coverageResults, matchingPackages);
    }

    private void addPackagesFailingFromNodeList(Set<CoverageResult> coverageResults, NodeList branchNodeList)
    {
        for ( int i = 0; i < branchNodeList.getLength(); i++ )
        {
            CoverageResult coverageResult = new CoverageResult();
            coverageResult.setPackageName(branchNodeList.item(i).getTextContent());
            coverageResults.add(coverageResult);
        }
    }

    private XPathExpression createExpression(XPath xpath, String expression)
    {
        XPathExpression expr;
        try
        {
            expr = xpath.compile(expression);
        }
        catch ( XPathExpressionException e )
        {
            throw new RuntimeException("Could not compile XPath Expression", e);
        }
        return expr;
    }

    private Document createDocument()
    {
        Document doc;
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(xmlDataFile);
        }
        catch ( ParserConfigurationException e )
        {
            throw new RuntimeException("could not create XML parser", e);
        }
        catch ( SAXException e )
        {
            throw new RuntimeException("error parsing xml file", e);
        }
        catch ( IOException e )
        {
            throw new RuntimeException("Error accessing file [" + xmlDataFile.getAbsolutePath() + "]", e);
        }
        return doc;
    }

    public void setXMLDataFile(File xmlDataFile)
    {
        this.xmlDataFile = xmlDataFile;
    }

    public void setMinimumTotalBranchRate(Double minimumTotalBranchRate)
    {
        this.minimumTotalBranchRate = minimumTotalBranchRate;
    }

    public void setMinimumTotalLineRate(Double minimumTotalLineRate)
    {
        this.minimumTotalLineRate = minimumTotalLineRate;
    }

    public void setMinimumGlobalBranchRatePerPackage(Double minimumGlobalBranchRatePerPackage)
    {
        this.minimumGlobalBranchRatePerPackage = minimumGlobalBranchRatePerPackage;
    }

    public void setMinimumGlobalLineRatePerPackage(Double minimumGlobalLineRatePerPackage)
    {
        this.minimumGlobalLineRatePerPackage = minimumGlobalLineRatePerPackage;
    }

    /**
     * PackageConfig will not replace an existing config of same name
     */
    public void addPackageConfig(PackageConfig packageConfig)
    {
        packageConfigs.add(packageConfig);
    }
}
