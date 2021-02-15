package com.github.derinn.proxies;

import com.github.derinn.account.Account;
import com.github.derinn.account.AccountHandler;
import com.github.derinn.managers.GlobalSettings;
import com.github.derinn.util.Logger;
import com.github.derinn.util.StopProgram;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ProxyManager{

    public static ArrayList<Proxy> proxyList = new ArrayList<>();

    public static void loadProxies(int proxies){
        //java is too retarded and if you start jar from different folder like java -jar something/Comet.jar won't see accounts.txt
        String fileName = "proxies.txt";
        File file = new File(fileName);

        if(file.exists()){

            try{

                InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), Charset.forName("Windows-1252"));
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                int proxyAmount = 0;
                String proxyString = bufferedReader.readLine();
                while(proxyString != null){
                    if(proxyString.contains(":")){
                        proxyString = proxyString.replaceAll(" ", "");
                        String[] inputs = proxyString.split(":");
                        Proxy proxy = new Proxy(inputs[0], Integer.parseInt(inputs[1]));
                        if(inputs.length == 4){
                            Logger.logDebug("Detected auth on proxy: " + inputs[0]);
                            proxy.setAuth(inputs[2], inputs[3]);
                        }else{
                            Logger.logDebug("Found proxy: " + inputs[0]);
                        }
                        proxyList.add(proxy);
                    }
                    proxyAmount++;
                    if(proxyAmount == proxies){
                        break;
                    }
                    proxyString = bufferedReader.readLine();
                }

                if(proxyList.size() == 0){
                    Logger.logError("Couldn't find any proxies in " + fileName);
                    StopProgram.gracefulQuit();
                }

                if(proxyAmount < proxies){
                    Logger.logWarning("Found less proxies than accounts, some will be reused.");
                }

                Logger.logInfo("Loaded " + proxyList.size() + " proxies.");

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

    public static void mapProxiesToAccounts(){
        int counter = 0;
        for(Account account : AccountHandler.accountList){
            if(counter > proxyList.size()){
                counter = 0;
            }
            GlobalSettings.accountsProxies.put(account, proxyList.get(counter));
            counter++;
        }
    }

}
