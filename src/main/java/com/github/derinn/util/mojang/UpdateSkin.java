package com.github.derinn.util.mojang;

import com.github.derinn.account.Account;
import com.github.derinn.managers.GlobalSettings;
import com.github.derinn.util.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateSkin implements Runnable{

    private final Account account;

    UpdateSkin(Account account){
        this.account = account;
    }

    public static void updateSkin(Account account){

        try{

            if(GlobalSettings.getChangeSkin() == null){
                Logger.logWarning("Skin not found, not changing it.");
                return;
            }

            if(account.getUuid().equals("-")){
                Logger.logError("Can't change skin on this account.");
                return;
            }

            Map<String, String> requestParams = new HashMap<>();
            requestParams.put("model", "");
            requestParams.put("url", GlobalSettings.getChangeSkin());

            String basedUrl = "https://api.mojang.com/user/profile/" + account.getUuid() + "/skin?";

            String encodedURL = requestParams.keySet().stream()
                    .map(key -> key + "=" + encodeValue(requestParams.get(key)))
                    .collect(Collectors.joining("&", basedUrl, ""));

            HttpClient client;

            client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

            String bearerHeader = "Bearer " + account.getBearer();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(encodedURL))
                    .timeout(Duration.ofMinutes(2))
                    //.header("Content-Type", "multipart/form-data;boundary=" + boundary)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", bearerHeader)
                    //.PUT(byteProcessor)
                    .POST(HttpRequest.BodyPublishers.ofString(encodedURL.split(basedUrl)[1]))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(Integer.toString(response.statusCode()).startsWith("20")){
                Logger.logSuccess("Skin changed for account " + account.getName());
            }else{
                Logger.logWarning("Couldn't upload skin...");
                Logger.logDebug("Skin problem: " + response.body());
            }

        }catch(InterruptedException | IOException ex){
            Logger.logError("Something something Exception occurred while updating skin. Account# " + account.getName(), ex);
        }

    }

    private static String encodeValue(String value) {
        try{
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        }catch(UnsupportedEncodingException e){
            Logger.logError("UnsupportedEncodingException occurred while encoding data." , e);
        }
        return null;
    }

    @Override
    public void run(){
        updateSkin(account);
    }

}
