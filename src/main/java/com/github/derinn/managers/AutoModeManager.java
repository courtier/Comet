package com.github.derinn.managers;

import com.github.derinn.util.Logger;
import com.github.derinn.util.StopProgram;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoModeManager{

    public void startAutoMode(){

        GlobalSettings.setAutoMode(true);

        System.out.println();

        Logger.logWarning("Make sure all names are at least a minute apart");
        Logger.logInfo("Will be reading names from automode.txt");
        Logger.logInput("Press enter to continue");

        new Scanner(System.in).nextLine();

        System.out.println();

        ArrayList<String> options = readNames();

        if(options == null){
            Logger.logError("Null names");
            StopProgram.gracefulQuit();
            return;
        }

        ExecutorService executorService;

        for(String option : options){

            String[] optionSplit = option.split("-");
            String desiredName = optionSplit[0];
            long delay = Long.parseLong(optionSplit[1]);
            int accounts = Integer.parseInt(optionSplit[2]);

            CometManager cometManager = new CometManager(GlobalSettings.getCometManagers().size(), desiredName, delay, accounts);
            GlobalSettings.getCometManagers().put(GlobalSettings.getCometManagers().size(), cometManager);

        }

        executorService = Executors.newFixedThreadPool(GlobalSettings.getCometManagers().size());

        GlobalSettings.getCometManagers().forEach((integer, cometManager) -> executorService.execute(cometManager));

    }

    private ArrayList<String> readNames(){

        String fileName = "automode.txt";
        File file = new File(fileName);

        if(!file.exists()){
            Logger.logError("automode.txt does not exist");
            StopProgram.gracefulQuit();
        }

        try{

            ArrayList<String> names = new ArrayList<>();

            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;

            while((line=bufferedReader.readLine())!=null){

                line = line.replaceAll(" ", "");

                if(line.split("-").length == 3){

                    names.add(line);

                }else{
                    Logger.logWarning("Removing name, bad format. Use <name-accounts-delay>");
                }

            }

            return names;

        }catch(IOException e){
            Logger.logError("Error while reading names.", e);
            StopProgram.gracefulQuit();
        }

        return null;

    }

}
