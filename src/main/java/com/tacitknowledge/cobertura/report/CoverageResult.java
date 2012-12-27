package com.tacitknowledge.cobertura.report;

/**
 * Holds coverage results
 */
public class CoverageResult
{
    private String packageName;


    public CoverageResult(String packageName)
    {
        this.packageName = packageName;
    }

    public CoverageResult()
    {
    }

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
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

        CoverageResult that = (CoverageResult) o;

        if ( packageName != null ? !packageName.equals(that.packageName) : that.packageName != null )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return packageName != null ? packageName.hashCode() : 0;
    }
}
