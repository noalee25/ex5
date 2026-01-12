package main;

import parser.Parser;
import parser.ParserException;
import parser.ParsedLine;
import validator.ValidationException;
import validator.Validator;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Main entry point for the s-Java verifier.
 * 
 * Usage: java main.Sjavac <source_file.sjava>
 * 
 * Exit codes:
 * 0 - Code is valid
 * 1 - Code has syntax/semantic errors
 * 2 - IO or usage errors
 */
public class Sjavac {
    
    private static final String SJAVA_EXTENSION = ".sjava";
    private static final int CODE_VALID = 0;
    private static final int CODE_INVALID = 1;
    private static final int CODE_IO_ERROR = 2;

    /**
     * the main manager of the program
     * @param args
     */
    public static void main(String[] args) {
        try {
            // step 1: validate command line arguments
            if (args.length != 1) {
                throw new InvalidUsageException(
                    "Usage: java main.Sjavac <source_file.sjava>");
            }
            
            String filename = args[0];
            
            // step 2: validate file extension
            if (!filename.endsWith(SJAVA_EXTENSION)) {
                throw new InvalidFileException(
                    "File must have " + SJAVA_EXTENSION + " extension");
            }
            
            // step 3: check file exists and is readable
            File sourceFile = new File(filename);
            if (!sourceFile.exists()) {
                throw new InvalidFileException(
                    "File does not exist: " + filename);
            }
            if (!sourceFile.isFile()) {
                throw new InvalidFileException(
                    "Not a file: " + filename);
            }
            if (!sourceFile.canRead()) {
                throw new InvalidFileException(
                    "Cannot read file: " + filename);
            }
            
            // step 4: parse the file
            Parser parser = new Parser(sourceFile);
            List<ParsedLine> parsedLines = parser.parseFile();
            
            // step 5: validate the parsed code
            Validator validator = new Validator(parsedLines);
            validator.validate();
            
            // step 6: if we reach here, code is valid
            System.out.println(CODE_VALID);
            
        } catch (InvalidUsageException e) {
            // usage error (wrong number of arguments)
            System.err.println("Error: " + e.getMessage());
            System.out.println(CODE_IO_ERROR);
            
        } catch (InvalidFileException e) {
            // file error (doesn't exist, wrong extension...)
            System.err.println("Error: " + e.getMessage());
            System.out.println(CODE_IO_ERROR);
            
        } catch (IOException e) {
            // IO error reading file
            System.err.println("IO Error: " + e.getMessage());
            System.out.println(CODE_IO_ERROR);
            
        } catch (ParserException e) {
            // syntax error during parsing
            System.err.println("Syntax Error: " + e.getMessage());
            System.out.println(CODE_INVALID);
            
        } catch (ValidationException e) {
            // semantic error during validation
            System.err.println("Validation Error: " + e.getMessage());
            System.out.println(CODE_INVALID);
            
        } catch (Exception e) {
            // unexpected error
            System.err.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println(CODE_IO_ERROR);
        }
    }
}

