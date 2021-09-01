package twophasecommit;

import java.io.*;

/**
 * The LogManager has responsibility for
 * reading and writing to logs
 */
public class LogManager {
    private final static String PATH = "logs/";

    /**
     * Writes to log
     *
     * @param filename the filename
     * @param line     the line to be written
     */
    public static void writeLog(String filename, String line){
        System.out.println("LOG, WRITE: <" + line + ">\n");

        //Creates directory if it does not exist
        File directory = new File(PATH);
        if (!directory.exists())
            directory.mkdir();


        try (
                //Initializes the writer
                FileWriter fileWriter = new FileWriter(PATH + filename + ".txt", true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                PrintWriter writer = new PrintWriter(bufferedWriter))
        {
            writer.println("<" + line + ">");
        } catch (IOException e){
            System.out.println("COULD NOT WRITE TO LOG: " + filename);
            e.printStackTrace();
        }
    }

    /**
     * Read last line from log
     *
     * @param filename the filename
     * @return the last line of the log
     */
    public static String readLog(String filename){
        String lastLine = null;

        try (
                //Initializes the reader
                FileReader fileReader = new FileReader(PATH + filename + ".txt");
                BufferedReader reader = new BufferedReader(fileReader))
        {

            //Reads log line by line
            String line = reader.readLine();
            while(line != null){
                lastLine = line;
                line = reader.readLine();
            }
        } catch (IOException e){
            System.out.println("COULD NOT READ LOG: " + filename);
            e.printStackTrace();
        }

        System.out.println("\nLOG, READ: " + lastLine + "\n");

        //Returns the last line
        return lastLine;
    }
}