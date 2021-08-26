package AMASMC.AMASMC_Function;

public class AMSMC_Variable
{
    String name;

    public AMSMC_Variable(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "name=" + name;
    }
}
