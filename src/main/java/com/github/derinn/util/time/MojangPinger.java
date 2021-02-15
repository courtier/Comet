package com.github.derinn.util.time;

import com.github.derinn.util.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class MojangPinger{

    public static long getPing(){

        try{
            Process process = Runtime.getRuntime().exec("ping api.mojang.com");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = bufferedReader.readLine();

            Logger.logInfo("Pinging Mojang API.");

            int counter = 0;
            int howManyPings = 6;

            ArrayList<String> pingLines = new ArrayList<>();

            while(line != null){
                counter++;
                //first line is info about bullshit, all rest are pings
                if(counter == howManyPings + 1){
                    process.destroy();
                    break;
                }
                pingLines.add(line);
                line = bufferedReader.readLine();
            }

            if(pingLines.size() < 2){
                return 0;
            }

            AtomicLong pingAverage = new AtomicLong(0);

            pingLines.remove(0);

            pingLines.forEach(pingLine -> {
                pingLine = pingLine.replace(" ", "");
                if(pingLine.contains("time=")){
                    String pingString = pingLine.split("time=")[1].split("ms")[0];
                    if(pingString.contains(".")){
                        pingString = pingString.split("\\.")[0];
                    }
                    pingAverage.addAndGet(Long.parseLong(pingString));
                }
            });

            return pingAverage.get() / (howManyPings - 1);

        }catch(IOException exception){
            Logger.logError("Error occurred while pinging Mojang API.", exception);
        }

        return 0;
    }

}
