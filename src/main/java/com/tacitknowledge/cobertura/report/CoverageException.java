package com.tacitknowledge.cobertura.report;

/**
 * Created by IntelliJ IDEA. User: byao Date: 12/21/12 Time: 10.0:11 AM To change this template use File | Settings | File
 * Templates.
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
