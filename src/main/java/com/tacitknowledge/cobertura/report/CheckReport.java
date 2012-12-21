package com.tacitknowledge.cobertura.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * Created by IntelliJ IDEA. User: byao Date: 12/21/12 Time: 9:52 AM To change this template use File | Settings | File
 * Templates.
 */
public class CheckReport
{
    public File xmlDataFile;
    public Double minimumTotalBranchRate = 0.0;
    public Double minimumTotalLineRate = 0.0;
    public Double minimumGlobalBranchRatePerPackage = 0.0;
    public Double minimumGlobalLineRatePerPackage = 0.0;
    private HashSet<PackageConfig> packageConfigs = new HashSet<PackageConfig>();

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
            //check global packages
            NodeList branchNodeList = (NodeList) packagesGlobalBranchRateExpression.evaluate(doc,
                XPathConstants.NODESET);
            NodeList lineNodeList = (NodeList) packagesGlobalLineRateExpression.evaluate(doc,
                XPathConstants.NODESET);
            addPackagesFailingFromNodeList(coverageResults, branchNodeList);
            addPackagesFailingFromNodeList(coverageResults, lineNodeList);

            //check regex package overrides
            for ( PackageConfig packageConfig : packageConfigs )
            {
                String regex = packageConfig.getRegex();
                if ( regex.endsWith(".*") )
                {
                    regex = regex.substring(0, regex.lastIndexOf("*"));
                }

                //remove any package overrides
                removeOverridenPackageFailures(coverageResults, doc, xpath, regex);

                //Find Override Failures
                //line rate
                String searchValue = "/coverage/packages/package[starts-with(@name,'" + regex +
                    "') and @line-rate<" + packageConfig.getLineRate() + "]/@name";
                addPackagesFailingRegex(coverageResults, doc, xpath, searchValue);

                //branch rate
                searchValue = "/coverage/packages/package[starts-with(@name,'" + regex +
                    "') and @branch-rate<" + packageConfig.getBranchRate() + "]/@name";
                addPackagesFailingRegex(coverageResults, doc, xpath, searchValue);
            }
        }
        catch ( XPathExpressionException e )
        {
            throw new RuntimeException("bad expression", e);
        }

        return coverageResults;
    }

    private void removeOverridenPackageFailures(Set<CoverageResult> coverageResults, Document doc, XPath xpath,
        String regex) throws XPathExpressionException
    {
        String searchValue = "/coverage/packages/package[starts-with(@name,'" + regex +
            "')]/@name";
        XPathExpression expression = xpath.compile(searchValue);
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
//        System.out.println(searchValue);
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
