/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tsp;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import roerobotyngve.Coordinate;

/**
 *
 * @author Yngve
 */
public class CreateTestSet {

    
    private static final String SAMPLE_CSV_FILE_PATH = "C:\\Users\\Yngve\\Documents\\TSPDatasets\\eli76.csv";
    
    public ArrayList readTestFile() {
        ArrayList<Coordinate> testList = new ArrayList<>();
        try (
                Reader reader = Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE_PATH));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withHeader("number", "Xcoord", "Ycoord")
                        .withDelimiter(';')
                        .withIgnoreHeaderCase()
                        .withTrim());) {
            for (CSVRecord csvRecord : csvParser) {
           
                // Accessing values by the names assigned to each column
                int number = this.strToInt(csvRecord.get("number"));     
                int xCoord = this.strToInt(csvRecord.get("Xcoord"));
                int yCoord = this.strToInt(csvRecord.get("YCoord"));
                   
                   
                Coordinate coord = new Coordinate(xCoord, yCoord);
                testList.add(coord);
                System.out.println("Record No - " + csvRecord.getRecordNumber());
                System.out.println("---------------");
                System.out.println("Number : " + number);
                System.out.println("XCoord : " + xCoord);
                System.out.println("YCoord : " + yCoord);
                System.out.println("---------------\n\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(CreateTestSet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return testList;
    }
    
    
    /**
     * Convert from string to Int 
     * @param str
     * @return Int 
     */
    private int strToInt(String str ){
    int i = 0;
    int num = 0;
    boolean isNeg = false;

    //Check for negative sign; if it's there, set the isNeg flag
    if (str.charAt(0) == '-') {
        isNeg = true;
        i = 1;
    }

    //Process each character of the string;
    while( i < str.length()) {
        num *= 10;
        num += str.charAt(i++) - '0'; //Minus the ASCII code of '0' to get the value of the charAt(i++).
    }

    if (isNeg)
        num = -num;
    return num;
}
}
