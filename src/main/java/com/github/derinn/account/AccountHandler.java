package com.github.derinn.account;

import com.github.derinn.managers.GlobalSettings;
import com.github.derinn.util.Logger;
import com.github.derinn.util.StopProgram;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountHandler{

    public static ArrayList<Account> accountList = new ArrayList<>();
    static AtomicInteger processedAccounts = new AtomicInteger(0);
    static List<Account> removeAccounts = Collections.synchronizedList(new ArrayList<>());

    public static void loadAccounts(int accounts){
        //java is too retarded and if you start jar from different folder like java -jar something/Comet.jar won't see accounts.txt
        String fileName = "accounts.txt";
        File file = new File(fileName);

        if(file.exists()){

            try{

                InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), Charset.forName("Windows-1252"));
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                int accountAmount = 0;
                String accountString = bufferedReader.readLine();
                while(accountString != null){
                    if(accountString.contains(":") && accountString.contains("@")){
                        accountString = accountString.replaceAll(" ", "");
                        String[] inputs = accountString.split(":");
                        Account account = new Account(inputs[0], inputs[1]);
                        accountList.add(account);
                        if(inputs.length == 5){
                            Logger.logDebug("Detected security questions on account: " + inputs[0]);
                            //do i need a loop here? i dont think so
                            account.getSecQuestions().add(inputs[2]);
                            account.getSecQuestions().add(inputs[3]);
                            account.getSecQuestions().add(inputs[4]);
                        }else{
                            Logger.logInfo("Found account: " + inputs[0]);
                        }
                    }
                    accountAmount++;
                    if(accountAmount == accounts){
                        break;
                    }
                    accountString = bufferedReader.readLine();
                }

                if(accountList.size() == 0){
                    Logger.logError("Couldn't find any accounts in " + fileName);
                    StopProgram.gracefulQuit();
                }

                Logger.logInfo("Loaded " + accountList.size() + " accounts.");

                System.out.println();

            }catch(IOException e){
                e.printStackTrace();
            }

        }else{
            Logger.logError("Couldn't find " + fileName + ", creating it, then quitting...");
            try{
                boolean created = file.createNewFile();
                if(created){
                    Logger.logSuccess("Created " + fileName + " file, please fill it in.");
                }else{
                    Logger.logError("Couldn't create " + fileName + " file, please create it yourself then fill it in.");
                }
            }catch(IOException ex){
                Logger.logError("Couldn't create " + fileName + " file, please create it yourself then fill it in.");
            }
            StopProgram.gracefulQuit();
        }
    }

    public static void authorizeAccounts(){

        ExecutorService executor = Executors.newFixedThreadPool(accountList.size());

        Logger.logInfo("Authenticating " + AccountHandler.accountList.size() + " accounts.");

        CountDownLatch countDownLatch = new CountDownLatch(accountList.size());

        AtomicBoolean slowDown = new AtomicBoolean(false);
        if(GlobalSettings.getSlowAuthDelay() != 0) slowDown.set(true);
        long sleepAmount = (GlobalSettings.getSlowAuthDelay() * 1000) / accountList.size();

        accountList.forEach(account -> {
            Logger.logDebug("Authenticating: " + account.getName());
            executor.execute(new AccountAuthenticator(account, countDownLatch));

            if(slowDown.get()){
                try{
                    Thread.sleep(sleepAmount);
                }catch(InterruptedException e){
                    Logger.logError("InterruptedException while trying to slow down authentication.", e);
                }
            }
        });

        try{
            countDownLatch.await();
        }catch(InterruptedException ex){
            Logger.logError("CountDownLatch failed while authenticating accounts. " + ex.getMessage() + " - " + ex.getStackTrace()[0]);
        }

        if(AccountHandler.accountList.size() == 0){
            Logger.logError("Could not load any accounts...");
            StopProgram.gracefulQuit();
        }

        removeAccounts.forEach(accountList::remove);
        Logger.logCustom("/b/[INFO] Done loading and authenticating /y/" + accountList.size() + " /b/accounts.");

    }

}

