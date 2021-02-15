package com.github.derinn.options;

import com.github.derinn.managers.CometManager;
import com.github.derinn.managers.GlobalSettings;
import com.github.derinn.proxies.ProxyManager;
import com.github.derinn.proxies.ProxyPinger;
import com.github.derinn.util.Logger;
import com.github.derinn.util.StopProgram;
import com.github.derinn.util.config.ConfigReader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SnipeOption{

    public static void startSnipeOption(int type){

        //1 = snipe, 2 = turbo

        if(!ConfigReader.doesConfigExist()){
            Logger.logWarning("Config does not exist. Comet will retrieve default config and use that.");
            ConfigReader.saveDefault();
        }

        Scanner scanner = new Scanner(System.in);

        GlobalSettings.setTurboMode(type == 2);
        GlobalSettings.setRemoteMode(false);

        Logger.logInput("Number of accounts (enter to use all) -> ");
        String accountAmountString = new Scanner(System.in).nextLine();
        int accountAmount = 1000;
        if(accountAmountString.length() > 0){
            accountAmount = Integer.parseInt(accountAmountString);
            if(accountAmount != 0){
                Logger.logInfo("Using " + accountAmount + " accounts.");
            }else{
                Logger.logInfo("Using all accounts.");
            }
        }else{
            Logger.logInfo("Using all accounts.");
        }

        Logger.logInput("Desired username -> ");
        String desiredName = scanner.next().replace(" ", "");
        Logger.logInfo("Sniping " + desiredName);

        //String previousNameChange = "";
        scanner.useDelimiter("\n");

        Instant unblockTime = null;
        if(type == 2){
            Logger.logInput("Time of unblock (NameMC => day/month/year, hour:minute:seconds) -> ");
            //scanner.useDelimiter("\n");
            //Logger.logInfo("Date of previous name change (name change from " + desiredName + ") -> ");
            String unblockString = scanner.next();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss");

            try{
                Date dateParsed = simpleDateFormat.parse(unblockString);
                unblockTime = dateParsed.toInstant();
            }catch(ParseException ex){
                Logger.logError("Wrong date format.", ex);
                StopProgram.gracefulQuit();
            }

        }

        Logger.logInput("Enter delay in milliseconds (seconds*1000) -> ");
        long milliDelay = new Scanner(System.in).nextLong();
        if(milliDelay < 0){
            Logger.logError("Need positive delay.");
            StopProgram.gracefulQuit();
        }
        Logger.logInfo("Using " + milliDelay + "ms delay");

        Logger.logInput("Use proxies (y/n/enter for yes) -> ");
        String useProxy = new Scanner(System.in).nextLine();
        if(useProxy.length() > 0){
            if(useProxy.startsWith("y")){
                Logger.logInfo("Using proxies.");
                GlobalSettings.setUseProxy(true);
            }else{
                Logger.logInfo("Not using proxies.");
                GlobalSettings.setUseProxy(false);
            }
        }else{
            Logger.logInfo("Using proxies.");
            GlobalSettings.setUseProxy(true);
        }

        if(GlobalSettings.isUseProxy()){
            System.out.println();
            Logger.logInfo("Loading proxies.");
            ProxyManager.loadProxies(accountAmount);
            System.out.println();
            Logger.logInfo("Testing proxy pings.");
            long diff = ProxyPinger.pingProxyDifference();
            Logger.logInfo("Proxy ping is " + diff + " higher than ping without proxy.");
            Logger.logInput("Do you wish to change your delay (y/n/enter for no) -> ");
            String addDifference = new Scanner(System.in).nextLine();
            if(addDifference.length() > 0){
                if(addDifference.startsWith("y")){
                    Logger.logInput("New delay -> ");
                    milliDelay = new Scanner(System.in).nextLong();
                    if(milliDelay < 0){
                        Logger.logError("Need positive delay.");
                        StopProgram.gracefulQuit();
                    }
                    Logger.logInfo("Changed delay to " + milliDelay);
                }else{
                    Logger.logInfo("Not adding ping.");
                }
            }else{
                Logger.logInfo("Not adding ping.");
            }
            System.out.println();
        }

        System.out.println();
        Logger.logInfo("Reading config options.");
        ConfigReader.readConfig();
        System.out.println();

        Logger.logInfo("Starting Comet thread.");

        System.out.println();

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        CometManager cometManager = new CometManager(GlobalSettings.getCometManagers().size(), desiredName, milliDelay, accountAmount);
        if(type == 2)
            cometManager.setUnblockTime(unblockTime);
        executorService.execute(cometManager);
        GlobalSettings.getCometManagers().put(GlobalSettings.getCometManagers().size(), cometManager);

    }

}
