package com.github.derinn.util;

import com.github.derinn.managers.GlobalSettings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

public class Logger{

    private static final ArrayList<String> logsForComet = new ArrayList<>();

    private static void saveLog(String input){
        if(GlobalSettings.isSaveLogs()){
            logsForComet.add(removeCodes(input));
        }
    }

    public static void logError(String input){
        saveLog(input);
        System.out.println(ansi().fg(RED).a("[ERROR] " + input).reset());
    }

    public static void logError(String input, Exception exception){
        saveLog(input);
        System.out.println(ansi().fg(RED).a("[ERROR] " + input + " Error: " + exception.getMessage() + " - " + Arrays.toString(exception.getStackTrace())).reset());
    }

    public static void logWarning(String input){
        saveLog(input);
        System.out.println(ansi().fg(YELLOW).a("[WARNING] " + input).reset());
    }

    public static void logSuccess(String input){
        saveLog(input);
        System.out.println(ansi().fg(GREEN).a("[SUCCESS] " + input).reset());
    }

    public static void logInfo(String input){
        saveLog(input);
        System.out.println(ansi().fg(CYAN).a("[INFO] " + input).reset());
    }

    public static void logDebug(String input){
        saveLog(input);
        if(GlobalSettings.isDebugMode()){
            System.out.println(ansi().fg(BLUE).a("[DEBUG] " + input).reset());
        }
    }

    public static String cleanCodes(String input){
        String output = input;
        output = output.replaceAll("/g/", ansi().fgGreen() + "");
        output = output.replaceAll("/r/", ansi().fgRed() + "");
        output = output.replaceAll("/b/", ansi().fgBlue() + "");
        output = output.replaceAll("/y/", ansi().fgYellow() + "");
        output = output.replaceAll("/m/", ansi().fgMagenta() + "");
        output = output.replaceAll("/br/", ansi().fgBrightRed() + "");
        output = output.replaceAll("/bb/", ansi().fgBrightBlue() + "");
        output = output.replaceAll("/c/", ansi().fgCyan() + "");
        output = output.replaceAll("/rs/", ansi().reset() + "");
        return output;
    }

    public static String removeCodes(String input){
        String output = input;
        output = output.replaceAll("/g/", "");
        output = output.replaceAll("/r/", "");
        output = output.replaceAll("/b/", "");
        output = output.replaceAll("/y/", "");
        output = output.replaceAll("/m/", "");
        output = output.replaceAll("/br/", "");
        output = output.replaceAll("/bb/", "");
        output = output.replaceAll("/c/", "");
        output = output.replaceAll("/rs/", "");
        return output;
    }

    public static void logCustom(String input){
        saveLog(input);
        System.out.println(cleanCodes(input));
    }

    public static void logArt(String input){
        saveLog(input);
        System.out.print(ansi().fg(MAGENTA).a(input).reset());
    }

    public static void logInput(String input){
        saveLog(input);
        System.out.print(ansi().fg(MAGENTA).a("[INPUT] " + input).reset());
    }

    public static void saveLogsToFile(String name){

        try{

            if(!new File("cometlogs").exists()){
                if(!new File("cometlogs").mkdir()){
                    Logger.logError("Couldn't create folder to save logs");
                    return;
                }
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy-HH-mm-ss");

            File file = new File("cometlogs", name + simpleDateFormat.format(Date.from(Instant.now())) + ".txt");

            if(!file.createNewFile()){
                Logger.logError("Couldn't create file to save logs");
                return;
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

            for(String line : logsForComet){
                bufferedWriter.write(line + System.lineSeparator());
            }

            bufferedWriter.close();

            Logger.logSuccess("Saved logs.");

        }catch(IOException ex){
            Logger.logError("Error while saving logs", ex);
        }

        logsForComet.clear();
    }

}
