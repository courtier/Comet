package com.github.derinn.util.file;

import com.github.derinn.account.Account;
import com.github.derinn.util.Logger;

import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SuccessfulSnipeFileThing{

    public static void saveToFile(Account account, String name){
        try{

            File file = new File("snipes.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            ZonedDateTime dateNow = ZonedDateTime.now();
            String dateString = DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm:ss.SSS a").format(dateNow);
            writer.write(account.getName() + ":" + account.getPassword() + " - " + name + " - " + dateString + "\n");
            Logger.logSuccess("Saved account to snipes.txt");

            writer.close();

            File accountsFile = new File("accounts.txt");
            File tempAccountsfile = new File("tempaccounts.txt");

            writer = new BufferedWriter(new FileWriter(tempAccountsfile, true));

            BufferedReader reader = new BufferedReader(new FileReader(accountsFile));

            String lineToRemove = account.getName() + ":" + account.getPassword();
            String currentLine;

            while((currentLine = reader.readLine()) != null){
                String trimmedLine = currentLine.trim();
                if(trimmedLine.contains(lineToRemove)) continue;
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            writer.close();
            reader.close();
            if(accountsFile.delete()){
                if(tempAccountsfile.renameTo(accountsFile)){
                    Logger.logSuccess("Removed account from accounts.txt");
                    return;
                }
            }

            Logger.logWarning("Couldn't remove account from accounts.txt");

        }catch(Exception ex){
            Logger.logError("Error occurred while saving snipes.txt or accounts.txt.", ex);
        }

    }

}
