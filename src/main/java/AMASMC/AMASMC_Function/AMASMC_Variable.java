package AMASMC.AMASMC_Function;

public class AMASMC_Variable
{
    String name;
    String definitionValue;

    public AMASMC_Variable(String name, String definitionValue)
    {
        this.name = name;
        this.definitionValue = definitionValue;
    }

    public AMASMC_Variable(String name)
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
        return "AMASMC_Variable{" +
                "name='" + name + '\'' +
                ", definitionValue='" + definitionValue + '\'' +
                '}';
    }
}
