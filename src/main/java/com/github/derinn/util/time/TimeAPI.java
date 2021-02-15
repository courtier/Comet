package com.github.derinn.util.time;

import com.github.derinn.util.Logger;
import com.github.derinn.util.StopProgram;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.json.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TimeAPI{

    //https://time.is/t/?en.1.124.-8709.0p.120.704.1597853186597. we could use this but i will try to use ntp pool first

    private static final String NTP_POOL_SUFFIX = ".pool.ntp.org";

    //get local time offset
    public static long getOffset(){

        try{
            String timeServer = "1.pool.ntp.org";
            InetAddress inetAddress = connectToServer(timeServer);
            if(inetAddress == null){
                Logger.logWarning("Couldn't get time from NTP, won't be syncing time.");
                return 0;
            }
            NTPUDPClient timeClient = new NTPUDPClient();
            TimeInfo timeInfo = timeClient.getTime(inetAddress);
            timeInfo.computeDetails();
            return timeInfo.getOffset();
        }catch(IOException e){
            Logger.logError("IOException while getting local time offset.", e);
        }
        return 0;
    }

    static InetAddress connectToServer(String address){

        final int TIMEOUT = 3000;

        try{
            InetAddress inetAddress = InetAddress.getByName(address);
            if(inetAddress.isReachable(TIMEOUT)){
                return inetAddress;
            }else{

                for(int x = 1; x < 4; x++){
                    String timeServer = x + NTP_POOL_SUFFIX;
                    inetAddress = InetAddress.getByName(timeServer);
                    if(inetAddress.isReachable(TIMEOUT)){
                        return inetAddress;
                    }
                }

                //time.nist.gov
                String nistTimeServer = "time.nist.gov";
                inetAddress = InetAddress.getByName(nistTimeServer);
                if(inetAddress.isReachable(TIMEOUT)){
                    return inetAddress;
                }

            }
        }catch(IOException exception){
            Logger.logError("IOException while getting connecting to NTP servers.", exception);
        }

        return null;

    }

    public static void getMojangTimestamp(){

        //very secret url dont leak
        String url = "https://help.minecraft.net/api/v2/community/topics/360001288571/posts.json";

        try{
            Connection.Response response = Jsoup.connect(url).method(Connection.Method.GET).ignoreContentType(true).execute();
            String timestampString = response.header("report-to").split("lkg-time=")[1].split("\"}]")[0];
            Instant instant = Instant.ofEpochSecond(Long.parseLong(timestampString));
            System.out.println(instant);
            System.out.println(Instant.now());
        }catch(IOException e){
            Logger.logError("IOException while getting local time offset.", e);
        }
    }

    public static Instant getNXDropTime(String name){

        //very secret url dont leak
        String url = "https://api.nathan.cx/check/" + name;

        try{
            Connection.Response response = Jsoup.connect(url).method(Connection.Method.GET).ignoreContentType(true).execute();
            String timestampString = response.body().split("drop_time\":\"")[1].replace("\"}", "");
            return Instant.parse(timestampString);
        }catch(IOException e){
            Logger.logError("IOException while getting local time offset.", e);
        }
        return null;
    }

    public static Instant getMojangDropTime(String desiredName){

        //String url = "https://api.mojang.com/users/profiles/minecraft/" + desiredName + "?at=" + (Instant.now().getEpochSecond() - 3456000);
        String url = "https://api.mojang.com/users/profiles/minecraft/" + desiredName + "?at=" + (Instant.now().getEpochSecond() - TimeUnit.DAYS.toSeconds(40));

        try{

            Document document = Jsoup.connect(url).ignoreContentType(true).get();
            JsonReader reader = Json.createReader(new StringReader(document.select("body").first().html()));
            JsonObject responseJson = reader.readObject();
            String oldOwnerID = responseJson.getString("id");
            url = "https://api.mojang.com/user/profiles/" + oldOwnerID + "/names";
            document = Jsoup.connect(url).ignoreContentType(true).get();
            reader = Json.createReader(new StringReader(document.select("body").first().html()));
            JsonArray responseJsonArray = reader.readArray();

            ArrayList<JsonObject> pastNameList = new ArrayList<>();

            responseJsonArray.forEach(jsonObject -> {
                String tempName = jsonObject.asJsonObject().getString("name");
                if(desiredName.equalsIgnoreCase(tempName)){
                    if(responseJsonArray.indexOf(jsonObject) < (responseJsonArray.size() - 1)){
                        pastNameList.add(responseJsonArray.get(responseJsonArray.indexOf(jsonObject) + 1).asJsonObject());
                    }else{
                        Logger.logError(desiredName + " is taken.");
                        StopProgram.gracefulQuit();
                    }
                }
            });

            JsonNumber jsonNumber = (JsonNumber) pastNameList.get(pastNameList.size() - 1).get("changedToAt");

            //long timestamp = jsonNumber.longValue() + 3196800000L;
            long timestamp = jsonNumber.longValue() + TimeUnit.DAYS.toMillis(37);

            Instant dropTime = Instant.ofEpochMilli(timestamp);
            ZonedDateTime tempDropTime = ZonedDateTime.ofInstant(dropTime, ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM HH:mm:ss");
            Logger.logInfo(desiredName + " available @ " + tempDropTime.format(formatter));

            return dropTime;

        }catch(IOException ex){
            Logger.logError("Could not get drop time.", ex);
        }

        return null;

    }

}
