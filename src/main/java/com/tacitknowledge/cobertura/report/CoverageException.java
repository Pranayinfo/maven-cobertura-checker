package com.tacitknowledge.cobertura.report;

/**
 * Coverage Exception
 */
public class CoverageException extends Exception
{
    public CoverageException()
    {
    }

    public CoverageException(String s)
    {
        super(s);
    }

    public CoverageException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public CoverageException(Throwable throwable)
    {
        super(throwable);
    }
}
