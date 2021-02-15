package com.github.derinn.util.namemc;

import com.github.derinn.managers.GlobalSettings;
import com.github.derinn.util.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

public class NamemcScraper{

    public static int getSearches(String username){

        String url = "https://namemc.com/name/" + username;
        try{
            Document document = Jsoup.connect(url).get();
            //0 is status, so available later/not available/available
            //1 is searches
            //2 is time of availability
            //3 is time remaining
            //child 1 is the value we want, child 0 is label/title thing
            /*
            description is Status: Available*, Searches: 1 / month
            get between "Searches: " and " / month"
             */

            ArrayList<Element> metaTags = document.getElementsByTag("meta");

            for(Element metaTag : metaTags){
                String content = metaTag.attr("content");
                String name = metaTag.attr("name");

                if(name.equalsIgnoreCase("description")){
                    String contentSearches = content.split("Searches: ")[1].split(" / month")[0];
                    return Integer.parseInt(contentSearches);
                }

            }

            return 0;

        }catch(IOException e){
            e.printStackTrace();
        }

        return 0;

    }

    public static Instant getDropTime(String username){

        String url = "https://namemc.com/name/" + username;

        if(GlobalSettings.isUseNamemcProxy()){
            //url = "https://hosteagle.club/name/" + username + "?__cpo=aHR0cHM6Ly9uYW1lbWMuY29t";
            url = "http://pdiperjuangan-diy.org/wp-includes/error.php?q=https%3A%2F%2Fnamemc.com%2Fname%2F" + username + "&hl=c0";
        }

        try{
            Document document = Jsoup.connect(url).followRedirects(true).get();

            Element dropTimeElement = document.getElementById("availability-time");
            String dateTime = dropTimeElement.attr("datetime");

            return Instant.parse(dateTime);

        }catch(IOException e){
            Logger.logError("Couldn't get drop time from NameMC", e);
        }

        return null;

    }

    /*
    public static void makeGif(String skinID){

        new File("skinframes").mkdir();

        //https://render.namemc.com/skin/3d/body.png?skin=ae4f01b14c48c545&model=classic&width=600&height=800&theta=-0
        for(int i = 0; i < 360; i++){

            String url = "https://render.namemc.com/skin/3d/body.png?skin=" + skinID + "&model=classic&width=600&height=800&theta=-" + i;
            try{

                InputStream in = new BufferedInputStream(new URL(url).openStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while (-1!=(n=in.read(buf)))
                {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();
                byte[] response = out.toByteArray();

                FileOutputStream outputStream = (new FileOutputStream("skinframes/" + skinID + "-" + i + ".png"));
                outputStream.write(response);  // resultImageResponse.body() is where the image's contents are.
                outputStream.close();

            }catch(FileNotFoundException e){
                Logger.logError("FileNotFoundException occurred while saving skin frame.", e);
            }catch(IOException exception){
                Logger.logError("IOException occurred while saving skin frame.", exception);
            }

        }

    }*/

}
