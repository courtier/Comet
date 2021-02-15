package com.github.derinn.discord;

import com.github.derinn.util.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class WebhookTest{

    public static void sendSnipeHook(){

        File file = new File("webhooks.txt");
        if(file.exists()){

            try{

                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String webhookUrl = bufferedReader.readLine();
                if(webhookUrl == null){
                    return;
                }

                String color = "839275";

                DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject()
                        .setTitle("Boob")
                        .setColor(color);

                do{

                    webhookUrl = webhookUrl.replaceAll(" ", "");

                    DiscordWebhook webhook = new DiscordWebhook();
                    webhook.addEmbed(embedObject);

                    webhook.setUrl(webhookUrl);

                    webhook.execute();

                }while((webhookUrl = bufferedReader.readLine()) != null);


            }catch(IOException e){
                Logger.logError("IOException while sending webhook.", e);
            }

        }else{
            Logger.logWarning("Couldn't find webhooks.txt, not sending webhook...");
        }

    }

}
