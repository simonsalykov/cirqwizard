package org.cirqoid.cnc.controller.interpreter;

/**
 * Created by simon on 16.06.17.
 */
public class ParsingException extends Exception
{
    private String failedBlock;

    public ParsingException(String message)
    {
        super(message);
    }

    public ParsingException(String message, String failedBlock)
    {
        super(message);
        this.failedBlock = failedBlock;
    }

    public String getFailedBlock()
    {
        return failedBlock;
    }
}
