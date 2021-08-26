package AMASMC;

import AMASMC.AMASMC_Function.AMASMC_Function;
import AMASMC.AMASMC_Function.AMSMC_Variable;
import AMASMC.AMASMC_Messenger.AMASMC_Messenger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AMASMC
{
    private final int maxFunctionNameSize = 1024;

    public String getCompilable()
    {
        return compilable;
    }

    public void setCompilable(String compilable)
    {
        this.compilable = compilable;
    }

    private String compilable;

    public static void main(String[] args) throws IOException
    {
        String inputFileName = args[0];

        AMASMC compiler = new AMASMC(inputFileName);

        AMASMC_Messenger.debug(compiler.getCompilable());

        AMASMC_Function[] functions = compiler.extractCompilableFunctions();

        for (int i = 0; i < functions.length; i++)
        {
            AMASMC_Messenger.debug(functions[i].getName());
            AMASMC_Messenger.debug(functions[i].getBody());
            AMASMC_Messenger.debug(Arrays.toString(functions[i].getArgs()));
            AMASMC_Messenger.debug("");
        }

        AMASMC_Messenger.debug(compiler.getCompilable());
    }

    public AMASMC(String compilableFilaName) throws FileNotFoundException
    {
        if(!compilableFilaName.toUpperCase().endsWith("AMASM"))
        {
            AMASMC_Messenger.warning("File " + compilableFilaName + "ends not with \".amasm\", but should");
        }

        FileInputStream compilable;
        try
        {
            this.compilable = formatCompilable(new FileInputStream(compilableFilaName));
        } catch (FileNotFoundException e)
        {
            AMASMC_Messenger.error("File "+compilableFilaName+" not found");
            return;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private String formatCompilable(FileInputStream compilable) throws IOException
    {
        String result = new String();
        for(; compilable.available() > 0 ;)
        {
            byte[] data = compilable.readNBytes(4);
            if(data.length > 0)
            {
                try
                {
//                    AMASMC_Messenger.debug(String.valueOf(((char) data[0])));
//                    AMASMC_Messenger.debug(String.valueOf(((char) data[1])));
//                    AMASMC_Messenger.debug(String.valueOf(((char) data[2])));
//                    AMASMC_Messenger.debug(String.valueOf(((char) data[3])));
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

        return result.replaceAll("\n", "").replace(" ", "");
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

                List<AMSMC_Variable> fArgsFinal = new ArrayList<>();

                for (int k = 0; k < fArgs.length; k++)
                {
                    fArgsFinal.add( new AMSMC_Variable(fArgs[k]) );
                }

                result.add( new AMASMC_Function(fName, fBody, fArgsFinal.toArray(new AMSMC_Variable[0])) );
                this.compilable = sb.toString();
            } else
            {
            }
        }
        this.compilable = compilable.replaceAll(" ", "");
        return result.toArray(new AMASMC_Function[0]);
    };
}
