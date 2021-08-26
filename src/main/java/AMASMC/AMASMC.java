package AMASMC;

import AMASMC.AMASMC_Function.AMASMC_Function;
import AMASMC.AMASMC_Function.AMASMC_Variable;
import AMASMC.AMASMC_Messenger.AMASMC_Messenger;

import java.io.*;
import java.util.*;

public class AMASMC
{
    private final int maxFunctionNameSize = 1024;
    private int maxVarilableNameSize = 1024;

    private Map<String, Integer> jumpTags = new HashMap<String, Integer>();
    private AMASMC_Variable[] globalVarilables;
    private AMASMC_Function[] globalFunctions;

    public String getCompilable()
    {
        return compilable;
    }

    public void setCompilable(String compilable)
    {
        this.compilable = compilable;
    }

    private String compilable;

    public AMASMC(String compilableFilaName) throws IOException
    {
        if(!compilableFilaName.toUpperCase().endsWith("AMASM"))
        {
            AMASMC_Messenger.warning("File " + compilableFilaName + "ends not with \".amasm\", but should");
        }

        FileInputStream compilable;
        try
        {
            this.compilable = new String(new FileInputStream(compilableFilaName).readAllBytes());
        } catch (FileNotFoundException e)
        {
            AMASMC_Messenger.error("File "+compilableFilaName+" not found");
            return;
        } catch (IOException e)
        {
            e.printStackTrace();
        }

//        AMASMC_Messenger.debug("Compilable:\n" + this.getCompilable());

        String validationResult = validateCompilable();

        if(validationResult.startsWith("Validation Error:"))
        {
            AMASMC_Messenger.error(validationResult);
            return;
        }
        else AMASMC_Messenger.debug("Validation passed successfully!");

        formatCompilable();

        this.globalFunctions = this.extractCompilableFunctions();

        AMASMC_Messenger.debug("Found global functions:");

        for (int i = 0; i < this.globalFunctions.length; i++)
        {
            AMASMC_Messenger.debug(globalFunctions[i].getName());
            AMASMC_Messenger.debug(globalFunctions[i].getBody());
            AMASMC_Messenger.debug(Arrays.toString(globalFunctions[i].getArgs()));
            AMASMC_Messenger.debug("");
        }

        AMASMC_Messenger.debug(this.getCompilable());

        this.globalVarilables = extractGlobalVarilables();

        AMASMC_Messenger.debug("Found global variables:");
        for (int i = 0; i < this.globalVarilables.length; i++)
        {
            AMASMC_Messenger.debug(this.globalVarilables[i].toString());
        };


    }

    private String validateCompilable()
    {
        ArrayList<Integer> unclosedBrackets = new ArrayList<>();
        for (int i = 0; i < this.compilable.split("\n").length; ++i)
        {
            for (int j = 0; j < this.compilable.split("\n")[i].length(); ++j)
            {
                if(this.compilable.split("\n")[i].charAt(j) == '{')unclosedBrackets.add(i);
                if(this.compilable.split("\n")[i].charAt(j) == '}')unclosedBrackets.remove(unclosedBrackets.size()-1);
            };
        }
        if(unclosedBrackets.size() > 0) return "Validation Error: Unclosed bracket detecled on line " +
                                                (unclosedBrackets.get(unclosedBrackets.size()-1)+1);
        else return "No any problems found!";
    };

    private void formatCompilable() throws IOException
    {
        InputStream compilable = new ByteArrayInputStream(this.compilable.getBytes());
        String result = new String();
        for(; compilable.available() > 0 ;)
        {
            byte[] data = compilable.readNBytes(4);
            if(data.length > 0)
            {
                try
                {

                } catch (ArrayIndexOutOfBoundsException ignored){};

                if (data.length >= 3 && ((char) data[0]) == '/' && ((char) data[1]) == '*' && ((char) data[2]) == '*')
                {
                    boolean stop = false;
                    for (; !stop; )
                    {
                        for (; ((char) compilable.read()) != '*'; ) ;
                        if (((char) compilable.read()) == '/') stop = true;
                    }
                } else if (data.length >= 2 && ((char) data[0]) == '/' && ((char) data[1]) == '/')
                {
                    for (; ((char) compilable.read()) != '\n'; ) ;
                } else
                {
                    try
                    {
                        result += ((char) data[0]);
                        result += ((char) data[1]);
                        result += ((char) data[2]);
                        result += ((char) data[3]);
                    } catch (ArrayIndexOutOfBoundsException ignored){};
                }
            }
        };

        this.compilable = result.replaceAll("\n", "").replace(" ", "");
    }



    AMASMC_Function[] extractCompilableFunctions()
    {
        List<AMASMC_Function> result = new ArrayList<>();
        for (int i = 0; i < compilable.length(); i++)
        {
            if(compilable.charAt(i) == ')' &&
               compilable.charAt(++i) == '{')
            {
                StringBuilder sb = new StringBuilder(this.compilable);
                int j = i;
                for (;
                     j>0 &&
                     ( compilable.charAt(j-1) != ';' && compilable.charAt(j-1) != '}' ) &&
                     (i - j < this.maxFunctionNameSize) &&
                     j < compilable.length()
                ; --j);

                String fName = new String();
                for (; compilable.charAt(j) != '(' && j < compilable.length(); ++j)
                {
                    fName += compilable.charAt(j);
                    sb.setCharAt(j, ' ');
                };

                sb.setCharAt(j, ' ');

                String fArgsRaw = "";
                for (++j; compilable.charAt(j) != ')' && j < compilable.length(); ++j)
                {
                    fArgsRaw += compilable.charAt(j);
                    sb.setCharAt(j, ' ');
                };

                sb.setCharAt(j, ' ');

                String[] fArgs = fArgsRaw.replaceAll(" ", "").split(",");

                sb.setCharAt(i, ' ');

                String fBody = new String();
                for(++i; compilable.charAt(i) != '}' && i < compilable.length(); ++i)
                {
                    fBody += compilable.charAt(i);
                    sb.setCharAt(i, ' ');
                };

                sb.setCharAt(i, ' ');

                List<AMASMC_Variable> fArgsFinal = new ArrayList<>();

                for (int k = 0; k < fArgs.length; k++)
                {
                    fArgsFinal.add( new AMASMC_Variable(fArgs[k]) );
                }

                result.add( new AMASMC_Function(fName, fBody, fArgsFinal.toArray(new AMASMC_Variable[0])) );
                this.compilable = sb.toString();
            } else
            {
            }
        }
        this.compilable = compilable.replaceAll(" ", "");
        return result.toArray(new AMASMC_Function[0]);
    };

    private AMASMC_Variable[] extractGlobalVarilables()
    {
        ArrayList<AMASMC_Variable> result = new ArrayList<>();
        String[] compilableSemiconsDevided = this.compilable.split(";");
        for (int i = 0; i < compilableSemiconsDevided.length; i++)
        {
            if(!compilableSemiconsDevided[i].startsWith("0") &&
               !compilableSemiconsDevided[i].startsWith("1") &&
               !compilableSemiconsDevided[i].startsWith("2") &&
               !compilableSemiconsDevided[i].startsWith("3") &&
               !compilableSemiconsDevided[i].startsWith("4") &&
               !compilableSemiconsDevided[i].startsWith("5") &&
               !compilableSemiconsDevided[i].startsWith("6") &&
               !compilableSemiconsDevided[i].startsWith("7") &&
               !compilableSemiconsDevided[i].startsWith("8") &&
               !compilableSemiconsDevided[i].startsWith("9") &&
                compilableSemiconsDevided[i].length() > 0)
            {
                String vName = "";
                int j = 0;
                for(;j<compilableSemiconsDevided[i].length() &&
                                compilableSemiconsDevided[i].charAt(j) != '=' &&
                                compilableSemiconsDevided[i].charAt(j) != ';' &&
                                j<this.maxVarilableNameSize;j++)
                {
                    vName += compilableSemiconsDevided[i].charAt(j);
                };

                String vVal = "";

                if(compilableSemiconsDevided[i].charAt(j) == '=')
                {
                    for(;j<compilableSemiconsDevided[i].length() &&
                           compilableSemiconsDevided[i].charAt(j) != ';' &&
                           j<this.maxVarilableNameSize;j++)
                    {
                        vVal += compilableSemiconsDevided[i].charAt(j);
                    };
                };

                result.add(new AMASMC_Variable(vName, vVal));
            };
        }
        return result.toArray(new AMASMC_Variable[0]);
    }

    private void compileFunctionBody(AMASMC_Function function)
    {
        String[] commands = function.getBody().split(";");
    };

    public static void main(String[] args) throws IOException
    {
        String inputFileName = args[0];

        AMASMC compiler = new AMASMC(inputFileName);
    }
}
