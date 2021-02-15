package com.github.derinn.discord;

import com.github.derinn.account.Account;
import com.github.derinn.managers.GlobalSettings;
import com.github.derinn.util.Logger;
import com.github.derinn.util.namemc.NamemcScraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class WebhookTing{

    private static final ArrayList<String> verbs = new ArrayList<>(Arrays.asList("Snatched", "Sniped", "Nabbed", "Got", "Grabbed", "Jacked", "Bagged", "Racked"));

    public static boolean sendSnipeHook(String desiredName, Account snipedAccount, int cometManagerID){

        File file = new File("webhooks.txt");
        if(file.exists()){

            try{

                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String webhookUrl = bufferedReader.readLine();
                if(webhookUrl == null){
                    return false;
                }

                String title;
                int rndVerb = new Random().nextInt(verbs.size());
                String randVerb = verbs.get(rndVerb);
                title = "Comet " + randVerb + " ";
                title += desiredName + "!";

                String thumbnail;
                String uuid = snipedAccount.getUuid();
                if(GlobalSettings.getWebhookThumbnail() == null){
                    thumbnail = "https://visage.surgeplay.com/full/" + uuid;
                }else{
                    thumbnail = GlobalSettings.getWebhookThumbnail();
                }

                String color = "839275";

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss");
                Date dropDate = Date.from(GlobalSettings.getCometManagers().get(cometManagerID).getDropTime());

                DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject()
                        .setTitle(title)
                        .setUrl("https://namemc.com/name/" + desiredName)
                        .addField("Name", "```" + desiredName + "```", true)
                        .addField("Searches", "```" + NamemcScraper.getSearches(desiredName) + "```", true)
                        .addField("Dropped At", "```" + simpleDateFormat.format(dropDate) + "```", true)
                        .setThumbnail(thumbnail)
                        //color.decode was throwing unsatisfiedlinkerror on linux or something
                        .setColor(color);

                String username = "Comet", logoURL = "https://i.imgur.com/aroRnZk.png";
                String url;

                do{

                    webhookUrl = webhookUrl.replaceAll(" ", "");

                    if(webhookUrl.contains(";")){
                        String[] webhookParts = webhookUrl.split(";");
                        url = webhookParts[0];
                        if(webhookParts.length >= 3){
                            username = webhookParts[1];
                            logoURL = webhookParts[2];
                        }
                        if(webhookParts[webhookParts.length - 1].equalsIgnoreCase("spoiler")){
                            embedObject.addField("Account Details", "||" + snipedAccount.getName() + ":" + snipedAccount.getPassword() + "||", false);
                        }
                    }else{
                        url = webhookUrl;
                    }

                    DiscordWebhook webhook = new DiscordWebhook();

                    webhook.addEmbed(embedObject);

                    webhook.setAvatarUrl(logoURL);
                    webhook.setUsername(username);

                    webhook.setUrl(url);

                    webhook.execute();

                    embedObject.removeFields("Account Details");

                }while((webhookUrl = bufferedReader.readLine()) != null);

                return true;

            }catch(IOException e){
                Logger.logError("IOException while sending webhook.", e);
                return false;
            }

        }else{
            Logger.logWarning("Couldn't find webhooks.txt, not sending webhook...");
        }

        return false;

    }

}
