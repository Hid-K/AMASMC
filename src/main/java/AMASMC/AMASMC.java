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
    Map<String, String> keywords = new HashMap<>();
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
        this.keywords.put("@thisx", "getThisX()");
        this.keywords.put("@thisy", "getThisY()");
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

        String validationResult = validateCompilable();

        if(validationResult.startsWith("Validation Error:"))
        {
            AMASMC_Messenger.error(validationResult);
            return;
        }
        else AMASMC_Messenger.debug("Validation passed successfully!");

        formatCompilable();

        AMASMC_Messenger.debug("Formatted compilable:\n" + this.getCompilable());

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

        AMASMC_Messenger.debug("Compiling functions....");
        for (int i = 0; i < this.globalFunctions.length; i++)
        {
            compileFunctionBody(this.globalFunctions[i]);
        }
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

    private void replaceKeywords()
    {
        for (int i = 0; i < this.compilable.length(); i++)
        {
            for (int j = 0; j < this.keywords.size(); j++)
            {
                for (Map.Entry<String, String> entry : this.keywords.entrySet()) {
                    String keyword = entry.getKey();
                    this.compilable = this.compilable.replace(keyword, this.keywords.get(keyword));
                }
            }
        }
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
        replaceKeywords();
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
            if(compilableSemiconsDevided[i].length() > 0)
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
                    ++j;
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
        ArrayList<AMASMC_Variable> localVariables = new ArrayList<>();
        String[] compilableSemiconsDevided = function.getBody().split(";");
        for (int i = 0; i < compilableSemiconsDevided.length; i++)
        {
            if(compilableSemiconsDevided[i].length() > 0)
            {
                String vName = "";
                int j = 0;
                for(;   j < compilableSemiconsDevided[i].length() &&
                        compilableSemiconsDevided[i].charAt(j) != '=' &&
                        compilableSemiconsDevided[i].charAt(j) != ';' &&
                        j < this.maxVarilableNameSize;j++)
                {
                    vName += compilableSemiconsDevided[i].charAt(j);
                };

                if(checkAlphabet(vName, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                {
                    String vVal = "";

                    if (j < compilableSemiconsDevided[i].length() && compilableSemiconsDevided[i].charAt(j) == '=')
                    {
                        ++j;
                        for (; j < compilableSemiconsDevided[i].length() &&
                                compilableSemiconsDevided[i].charAt(j) != ';' &&
                                j < this.maxVarilableNameSize; j++)
                        {
                            vVal += compilableSemiconsDevided[i].charAt(j);
                        };
                    };

                    localVariables.add(new AMASMC_Variable(vName, vVal));
                }
            };
        }
        AMASMC_Messenger.debug(Arrays.toString(localVariables.toArray(new AMASMC_Variable[0])));
    };

    private boolean checkAlphabet( String alphabet, String desiredAlphabet )
    {

        for(int i = 0; i < alphabet.length(); ++i)
        {
            boolean alphabetValid = false;
            for(int j = 0; j < desiredAlphabet.length(); ++j)
            {
                if (alphabet.charAt(i) == desiredAlphabet.charAt(j))
                {
                    alphabetValid = true;
                    break;
                }
            }
            if(!alphabetValid) return false;
        }
        return true;
    }

    private String getStringAlphabet(final String str)
    {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < str.length(); ++i)
        {
            boolean notInResult = true;
            for(int j = 0; j < result.length() && notInResult; ++j) notInResult = (str.charAt(i) == result.charAt(j));
            if(notInResult) result.append(str.charAt(i));
        }
        return result.toString();
    }

    public static void main(String[] args) throws IOException
    {
        String inputFileName = args[0];

        AMASMC compiler = new AMASMC(inputFileName);
    }
}
