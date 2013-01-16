package com.tacitknowledge.cobertura.report;

/**
 * Package coverage configuration
 *
 */
public class PackageConfig
{
    /**
     * Prefixes allow for wild cards.  for instance java.lang.*
     *
     */
    private String nameOrPrefix;
    private Double branchRate = 0.0;
    private Double lineRate = 0.0;

    /**
     * default for dependency injection by maven
     */
    public PackageConfig()
    {
        this(null,0.0,0.0);
    }

    public PackageConfig(String nameOrPrefix, Double branchRate, Double lineRate)
    {

        this.nameOrPrefix = nameOrPrefix;
        this.branchRate = branchRate;
        this.lineRate = lineRate;
    }

    public String getNameOrPrefix()
    {
        return nameOrPrefix;
    }


    public Double getBranchRate()
    {
        return branchRate;
    }


    public Double getLineRate()
    {
        return lineRate;
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

        if ( nameOrPrefix != null ? !nameOrPrefix.equals(that.nameOrPrefix) : that.nameOrPrefix != null )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return nameOrPrefix != null ? nameOrPrefix.hashCode() : 0;
    }
}
