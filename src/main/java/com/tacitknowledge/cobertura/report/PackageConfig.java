package com.tacitknowledge.cobertura.report;

/**
 * Created by IntelliJ IDEA. User: byao Date: 12/21/12 Time: 12:00 PM To change this template use File | Settings | File
 * Templates.
 */
public class PackageConfig
{
    private String regex;
    private Double branchRate = 0.0;
    private Double lineRate = 0.0;

    public PackageConfig()
    {
        this(null,0.0,0.0);
    }

    public PackageConfig(String regex, Double branchRate, Double lineRate)
    {

        this.regex = regex;
        this.branchRate = branchRate;
        this.lineRate = lineRate;
    }

    public String getRegex()
    {
        return regex;
    }

    public void setRegex(String regex)
    {
        this.regex = regex;
    }

    public Double getBranchRate()
    {
        return branchRate;
    }

    public void setBranchRate(Double branchRate)
    {
        this.branchRate = branchRate;
    }

    public Double getLineRate()
    {
        return lineRate;
    }

    public void setLineRate(Double lineRate)
    {
        this.lineRate = lineRate;
    }

    @Override
    public boolean equals(Object o)
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        PackageConfig that = (PackageConfig) o;

        if ( regex != null ? !regex.equals(that.regex) : that.regex != null )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return regex != null ? regex.hashCode() : 0;
    }
}
