package AMASMC.AMASMC_Function;

public class AMASMC_Function
{
    String name;
    String body;
    String compiled;
    AMASMC_Variable[] args;

    public String getCompiled()
    {
        return compiled;
    }

    public void setCompiled(String compiled)
    {
        this.compiled = compiled;
    }

    public AMASMC_Variable[] getArgs()
    {
        return args;
    }

    public void setArgs(AMASMC_Variable[] args)
    {
        this.args = args;
    }

    public AMASMC_Function(String name, String body, AMASMC_Variable[] args)
    {
        this.name = name;
        this.body = body;
        this.args = args;
    }

    public AMASMC_Function(String name, String body)
    {
        this.name = name;
        this.body = body;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }
}
