package com.github.derinn.util;

import com.github.derinn.managers.GlobalSettings;
import org.fusesource.jansi.AnsiConsole;

import java.util.Scanner;

public class StopProgram{

    public static void gracefulQuit(){
        if(GlobalSettings.getCometManagers().get(0) != null && GlobalSettings.getCometManagers().get(0).isShutdown()){
            GlobalSettings.getCometManagers().get(0).endThread();
        }
        if(!GlobalSettings.isAutoMode()){
            Logger.logInfo("Shutting down...");
            AnsiConsole.systemUninstall();
            System.exit(0);
        }

    }

    public static void consensualQuit(){
        if(GlobalSettings.getCometManagers().get(0) != null && GlobalSettings.getCometManagers().get(0).isShutdown()){
            GlobalSettings.getCometManagers().get(0).endThread();
        }
        if(!GlobalSettings.isAutoMode()){
            Logger.logInput("Press enter to quit -> ");
            new Scanner(System.in).nextLine();
            AnsiConsole.systemUninstall();
            System.exit(0);
        }

    }

}
