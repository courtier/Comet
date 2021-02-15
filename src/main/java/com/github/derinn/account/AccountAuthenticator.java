package com.github.derinn.account;

import com.github.derinn.util.Logger;
import com.github.derinn.util.StopProgram;

import javax.json.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

//had to change from Authenticator to AccountAuthenticator cause of java.net.Authenticator
public class AccountAuthenticator implements Runnable{

    private final Account account;
    private final CountDownLatch countDownLatch;

    AccountAuthenticator(Account account, CountDownLatch countDownLatch){
        this.account = account;
        this.countDownLatch = countDownLatch;
    }

    void authenticateAccount(Account account){

        String url = "https://authserver.mojang.com/authenticate";

        String clientToken = UUID.randomUUID().toString();

        //SettingsManager.getSettings().get(account).setClientToken(clientToken);

        String accountName = account.getName();
        String accountPass = account.getPassword();

        JsonObjectBuilder payload = Json.createObjectBuilder()
                .add("agent", Json.createObjectBuilder().add("name", "Minecraft").add("version", 1))
                .add("username", accountName)
                .add("password", accountPass)
                .add("clientToken", clientToken);

        String payloadString = payload.build().toString();

        try{

            HttpClient client;

            client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payloadString))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 403){

                Logger.logError("Credentials were incorrect. " + response.statusCode());
                Logger.logWarning("Removing account from the pool: " + accountName);
                removeAccount();
                return;

            }else if(response.statusCode() == 401){

                Logger.logError("This IP has been rate-limited, wait a while (around 10 minutes) before trying again. " + response.statusCode());
                Logger.logWarning("Removing account from the pool: " + accountName);
                removeAccount();
                return;

            }

            if(response.body() == null){
                Logger.logError("Response body is null.");
                Logger.logWarning("Removing account from the pool: " + accountName);
                removeAccount();
                return;
            }

            if(response.body().contains("Request blocked.")){
                Logger.logError("Can't authenticate, got rate-limited.");
                StopProgram.gracefulQuit();
            }

            JsonReader reader = Json.createReader(new StringReader(response.body()));
            JsonObject responseJson = reader.readObject();

            String accessToken;

            try{
                accessToken = responseJson.getString("accessToken");
            }catch(NullPointerException ex){
                Logger.logError("Could not get access token, removing account from pool. Response: " + response.statusCode());
                removeAccount();
                return;
            }

            account.setBearer(accessToken);
            if(responseJson.getJsonObject("selectedProfile").containsKey("id")){
                account.setUuid(responseJson.getJsonObject("selectedProfile").getString("id"));
            }else{
                Logger.logError("Found blocking account, but Comet is not running in blocking mode. Removing account from pool.");
                removeAccount();
                return;
            }

            Logger.logDebug("Authenticated, validating token.");

            url = "https://api.mojang.com/user/security/challenges";

            String bearerHeader = "Bearer " + accessToken;

            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(2))
                    .header("Authorization", bearerHeader)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int[] questionIDs = new int[3];

            if(response.body().contains("question")){

                if(account.getSecQuestions().isEmpty()){
                    Logger.logError("Account has sec questions but no answers found, removing account from pool.");
                    removeAccount();
                    return;
                }

                Logger.logDebug("Using security questions.");
                reader = Json.createReader(new StringReader(response.body()));
                //System.out.println(responseString);
                JsonArray jsonQuestions = reader.readArray();
                for(int i = 0; i < 3; i++){
                    questionIDs[i] = jsonQuestions.getJsonObject(i).getJsonObject("answer").getInt("id");
                    Logger.logInfo(accountName + " - Question " + (i + 1) + ": " + jsonQuestions.getJsonObject(i).getJsonObject("question").getString("question"));
                }

                JsonArrayBuilder answersArray = Json.createArrayBuilder()
                        .add(Json.createObjectBuilder().add("id", questionIDs[0]).add("answer", account.getSecQuestions().get(0)))
                        .add(Json.createObjectBuilder().add("id", questionIDs[1]).add("answer", account.getSecQuestions().get(1)))
                        .add(Json.createObjectBuilder().add("id", questionIDs[2]).add("answer", account.getSecQuestions().get(2)));

                payloadString = answersArray.build().toString();

                //System.out.println(payloadString);

                url = "https://api.mojang.com/user/security/location";

                request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofMinutes(2))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", bearerHeader)
                        .POST(HttpRequest.BodyPublishers.ofString(payloadString))
                        .build();

                response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if(Integer.toString(response.statusCode()).startsWith("20")){
                    Logger.logDebug("Validated token and used security questions.");
                }else{
                    Logger.logError("Couldn't answer security questions on account: " + account.getName() + " - Response: " + response.statusCode());
                    Logger.logDebug(account.getName() + " - Body: " + response.body());
                    removeAccount();
                    return;
                }

            }

            String message = "Authorized account: " + account.getName() + " - UUID: " + account.getUuid();
            Logger.logSuccess(message);
            AccountHandler.processedAccounts.getAndIncrement();
            countDownLatch.countDown();

        }catch(IOException | InterruptedException ex){
            Logger.logError("IOException error.", ex);
            removeAccount();
        }

    }

    private void removeAccount(){
        AccountHandler.removeAccounts.add(account);
        AccountHandler.processedAccounts.getAndIncrement();
        countDownLatch.countDown();
    }

    @Override
    public void run(){
        authenticateAccount(account);
    }

}
