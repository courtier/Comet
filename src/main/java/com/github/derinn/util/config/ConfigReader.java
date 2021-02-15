package com.github.derinn.util.config;

import com.github.derinn.managers.GlobalSettings;
import com.github.derinn.util.Logger;
import com.github.derinn.util.StopProgram;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class ConfigReader{

    public static void readConfig(){

        File file = new File("config.txt");

        if(file.exists()){

            try{

                InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), Charset.forName("Windows-1252"));
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String optionLine = bufferedReader.readLine();

                HashMap<String, Object> options = new HashMap<>();

                while(optionLine != null){
                    if(!optionLine.startsWith("#") && optionLine.contains("=")){
                        optionLine = optionLine.replaceAll(" ", "");
                        String key = optionLine.split("=")[0];
                        String value = optionLine.split("=")[1];
                        if(value.equals("false") || value.equals("true")){
                            options.put(key, Boolean.parseBoolean(value));
                        }else if(value.matches("\\d+")){
                            options.put(key, Integer.parseInt(value));
                        }else if(key.equals("skin")){
                            if(value.equals("n")){
                                options.put(key, null);
                            }else{
                                options.put(key, value.replaceAll(" ", ""));
                            }
                        }else if(key.equals("thumbnail")){
                            if(value.equals("default")){
                                options.put(key, null);
                            }else{
                                options.put(key, value.replaceAll(" ", ""));
                            }
                        }
                    }
                    optionLine = bufferedReader.readLine();
                }

                if(options.isEmpty()){
                    Logger.logError("No configurations found!");
                    StopProgram.gracefulQuit();
                }

                assignKeys(options);

                Logger.logSuccess("Read config successfully.");

            }catch(IOException e){
                Logger.logError("IOException occurred while reading config.txt", e);
            }

        }else{
            Logger.logError("Couldn't find config.txt, will create it, but you will have to input everything.");
            try{
                if(!file.createNewFile()){
                    Logger.logError("Couldn't create config.txt");
                }
            }catch(IOException exception){
                Logger.logError("IOException occurred while creating config.txt", exception);
            }
        }

    }

    private static void assignKeys(HashMap<String, Object> keyValuePairs){

        keyValuePairs.keySet().forEach(key -> {
            if(key.equalsIgnoreCase("requests")){
                GlobalSettings.setReqsPerAccount((Integer) keyValuePairs.get(key));
                Logger.logCustom("/b/[INFO] Requests per account: /y/" + keyValuePairs.get(key));
            }else if(key.equalsIgnoreCase("skin")){
                GlobalSettings.setChangeSkin((String) keyValuePairs.get(key));
                Logger.logCustom("/b/[INFO] Skin: /y/" + keyValuePairs.get(key));
            }else if(key.equalsIgnoreCase("webhook")){
                GlobalSettings.setSendWebhook((Boolean) keyValuePairs.get(key));
                Logger.logCustom("/b/[INFO] Send webhook: /y/" + keyValuePairs.get(key));
            }else if(key.equalsIgnoreCase("thumbnail")){
                GlobalSettings.setWebhookThumbnail((String) keyValuePairs.get(key));
                Logger.logCustom("/b/[INFO] Webhook thumbnail: /y/" + keyValuePairs.get(key));
            }else if(key.equalsIgnoreCase("warmup")){
                GlobalSettings.setWarmUp((Boolean) keyValuePairs.get(key));
                Logger.logCustom("/b/[INFO] Warmup: /y/" + keyValuePairs.get(key));
            }else if(key.equalsIgnoreCase("ping")){
                GlobalSettings.setPingMojang((Boolean) keyValuePairs.get(key));
                Logger.logCustom("/b/[INFO] Ping Mojang: /y/" + keyValuePairs.get(key));
            }else if(key.equalsIgnoreCase("slowauth")){
                GlobalSettings.setSlowAuthDelay((Integer) keyValuePairs.get(key));
                Logger.logCustom("/b/[INFO] Slow auth: /y/" + keyValuePairs.get(key));
            }else if(key.equalsIgnoreCase("auth")){
                GlobalSettings.setAuthDelay((Integer) keyValuePairs.get(key));
                Logger.logCustom("/b/[INFO] Authentication: /y/" + keyValuePairs.get(key));
            }else if(key.equalsIgnoreCase("savelogs")){
                GlobalSettings.setSaveLogs((Boolean) keyValuePairs.get(key));
                Logger.logCustom("/b/[INFO] Save logs: /y/" + keyValuePairs.get(key));
            }else if(key.equalsIgnoreCase("killprocess")){
                GlobalSettings.setKillProcess((Boolean) keyValuePairs.get(key));
                Logger.logCustom("/b/[INFO] Kill Process: /y/" + keyValuePairs.get(key));
            }else if(key.equalsIgnoreCase("namemcproxy")){
                GlobalSettings.setUseNamemcProxy((Boolean) keyValuePairs.get(key));
                Logger.logCustom("/b/[INFO] NameMC Proxy: /y/" + keyValuePairs.get(key));
            }else if(key.equalsIgnoreCase("speedcap")){
                GlobalSettings.setSpeedCap(Integer.toUnsignedLong((Integer) keyValuePairs.get(key)));
                Logger.logCustom("/b/[INFO] Speed cap: /y/" + keyValuePairs.get(key));
            }
        });

    }

    public static boolean doesConfigExist(){
        return new File("config.txt").exists();
    }

    public static void saveDefault(){

        URL resource = ConfigReader.class.getClassLoader().getResource("config.txt");
        if(resource == null){
            Logger.logError("Default config not found.");
            StopProgram.gracefulQuit();
        }else{
            try{

                InputStream resourceStream = resource.openStream();
                File exportConfig = new File("config.txt");

                if(exportConfig.exists()){
                    if(exportConfig.delete()){
                        copyStream(resourceStream, exportConfig.toPath());
                        return;
                    }
                }else{
                    copyStream(resourceStream, exportConfig.toPath());
                    return;
                }

                Logger.logError("Couldn't retrieve default config.txt");
                StopProgram.gracefulQuit();

            }catch(IOException e){
                Logger.logError("IOException when trying to retrieve default config.", e);
                StopProgram.gracefulQuit();
            }
        }

    }

    private static void copyStream(InputStream inputStream, Path targetPath){
        try{
            Files.copy(inputStream, targetPath);
            Logger.logSuccess("Retrieved default config.txt");
            StopProgram.gracefulQuit();
        }catch(IOException exception){
            Logger.logError("IOException when trying to save default config.", exception);
            StopProgram.gracefulQuit();
        }
    }

}
