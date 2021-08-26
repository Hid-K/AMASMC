package AMASMC.AMASMC_Messenger;

public class AMASMC_Messenger
{
    static private boolean debugEnabled = true;

    public static boolean isDebugEnabled()
    {
        return debugEnabled;
    }

    public static void setDebugEnabled(boolean debugEnabled)
    {
        AMASMC_Messenger.debugEnabled = debugEnabled;
    }

    public static void debug(String message)
    {
        if(debugEnabled)
            System.out.println(message);
    }

    public static void warning(String message)
    {
        System.out.println("Warning:" + message);
    };

    public static void warning(String message, int r, int c)
    {
        System.out.println("Warning on "+r+":"+c+":" + message);
    };

    public static void error(String message)
    {
        System.out.println("Error:" + message);
    };

    public static void error(String message, int r, int c)
    {
        System.out.println("Error on "+r+":"+c+":" + message);
    };

    public static void fatalError(String message)
    {
        System.out.println("Error:" + message);
    };

    public static void fatalError(String message, int r, int c)
    {
        System.out.println("Fatal Error on "+r+":"+c+":" + message);
    };
}
