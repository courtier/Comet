package com.github.derinn;

import com.github.derinn.managers.AutoModeManager;
import com.github.derinn.managers.CometManager;
import com.github.derinn.managers.GlobalSettings;
import com.github.derinn.options.SnipeOption;
import com.github.derinn.util.Logger;
import com.github.derinn.util.StopProgram;
import com.github.derinn.util.config.ConfigReader;
import org.fusesource.jansi.AnsiConsole;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


    /*
                                                @@@.
                                           @@@@@@
                         @@@@@@@@@@@     @@@@@@.
                         /@@@@@@@@@@@   @@@@
                         ,@@@@@         @@@          @@@@
                  @&     @@@@@          @@@          #@@@*
                @@@@@@.  @@@@@#         @@@@         @@@@@      @@@
              @@@@@@@    @@@@@@          @@@@&       @@@@@      @@@&
             @@@@@       @@@@@@@@@@        @@@@@@    *@@@       .@@
            @@@@         @@@@@@              @@@@@@   @@@%      @@@(
            @@@&         @@@@@@               @@@@@  *@@@@      @@@/
            @@@@         @@@@@@               *@@@   @@@@@@@@@@@@@@
             @@@@        @@@@@@              @@@@.   @@@@@@@@@@@@@@
              @@@@@@      @@@@@@%@@@      @@@@@@     @@@@      @@@@@
                @@@@@@@@   @@@@@@@      @@@@@@@      @@@@       @@@&
                 .@@@@@@    @@@@           (#        @@@@       @@@
                  *@@@@#    @@*                      @@@@       @@@
                  @@@@              &@@@@@@@@@@@     @@@        &@@
                @@@@@           @@@@@@@@@@@@@@@@@@    @@#       @@@
             @@@@@@.         @@@@@@@@@@@@@@@@@@@@@@#            @@@
             (@@@@@        @@@@@@@@@@@@@@@@@@@@@@@@@           #@@@  *
                          @@@@@@@@@@@@@@@@@@@@@@@@@@@
                         #@@@@@@@@@@@@@@@@@@@@@@@@@@@
                         @@@@@@@@@@@@@@@@@@@@/   (@@@
                          @@@@@@@@@@@@@@@@        @@
                           @@@         @@@        @
                            @@        @  .@@//@@@@@
                             @@      @     @@@@@@@@,
                             @@@@@@@@&   /(@@@@@@@
                             @@@@@@@@@@@@@@@@@
                                  @@@@@@@@@@
                                .   / @.&    #@
                                 #        @@&@@
                                (@@@@@@@@@@@@@
                                   @@@@@@@@/
     */

public class Comet{

    //SESH TILL IM 6 FEET UNDER

    public static void main(String[] args){

        //https://fsymbols.com/generators/tarty/

        GlobalSettings.setDebugMode(false);
        GlobalSettings.setSaveLogs(true);

        if(args.length > 0){
            if(args[0].equalsIgnoreCase("debug")){
                GlobalSettings.setDebugMode(true);
                Logger.logDebug("RUNNING IN DEBUG MODE");
            }else if(args[0].equalsIgnoreCase("snipe") || args[0].equalsIgnoreCase("block")){
                if(args.length >= 4){
                    //format -> java -jar Comet.jar snipe <accounts> <delay> <name>
                    GlobalSettings.setDebugMode(false);
                    GlobalSettings.setRemoteMode(true);
                    ConfigReader.readConfig();
                    Logger.logInfo("Using " + args[1] + " accounts");
                    Logger.logInfo("Using " + args[2] + " delay");
                    ExecutorService executorService = Executors.newFixedThreadPool(1);
                    CometManager cometManager = new CometManager(GlobalSettings.getCometManagers().size(), args[3], Long.parseLong(args[2]), Integer.parseInt(args[1]));
                    executorService.execute(cometManager);
                    GlobalSettings.getCometManagers().put(GlobalSettings.getCometManagers().size(), cometManager);
                    return;
                }
            }
        }


        //HELLO???? AnsiConsole.systemInstall(); BREAKS ALL COLORS CODES???? only for intellij console
        //AnsiConsole.systemUninstall();

        AnsiConsole.systemInstall();

        //java is literally retarded
        //as of 29/09 i dont even know wtf this does but i remember some error was being thrown
        //it is now 07/10 i still dont know what this does and cba to google it, im implementing options
        //18/12/2020 java is now dethroned by go
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");

        System.out.print("\033[H\033[2J");
        System.out.flush();

        Logger.logArt(
                  " █████╗  █████╗ ███╗   ███╗███████╗████████╗\n" +
                        "██╔══██╗██╔══██╗████╗ ████║██╔════╝╚══██╔══╝\n" +
                        "██║  ╚═╝██║  ██║██╔████╔██║█████╗     ██║   \n" +
                        "██║  ██╗██║  ██║██║╚██╔╝██║██╔══╝     ██║   \n" +
                        "╚█████╔╝╚█████╔╝██║ ╚═╝ ██║███████╗   ██║   \n" +
                        " ╚════╝  ╚════╝ ╚═╝     ╚═╝╚══════╝   ╚═╝   \n");

        Logger.logInfo("By yours truly - courtier#1305");
        Logger.logInfo("S/O SESH & G59");

        System.out.println();

        Logger.logWarning("Necessary files: accounts.txt, config.txt");
        Logger.logWarning("Optional files: proxies.txt, webhooks.txt");

        System.out.println();

        Scanner scanner = new Scanner(System.in);

        Logger.logInfo("1- Snipe");
        Logger.logInfo("2- Turbo snipe");
        Logger.logInfo("3- Retrieve default config.txt");
        Logger.logInfo("4- Auto mode");

        System.out.println();

        Logger.logInput("Your choice -> ");

        int option = 0;

        if(scanner.hasNextInt()){
            option = scanner.nextInt();
        }else{
            Logger.logError("Pick an option and put the number in.");
            StopProgram.gracefulQuit();
        }

        if(option == 1 || option == 2){

            SnipeOption.startSnipeOption(option);

        }else if(option == 3){

            Logger.logInfo("Retrieving default config.");
            ConfigReader.saveDefault();

        }else if(option == 4){

            AutoModeManager manager = new AutoModeManager();
            manager.startAutoMode();

        }else{

            Logger.logInfo("SESH TILL IM 6 FEET DEEP");
            StopProgram.gracefulQuit();

        }

    }

}
