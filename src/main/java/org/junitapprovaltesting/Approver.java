package org.junitapprovaltesting;


import org.junit.jupiter.api.Assertions;
import org.junitapprovaltesting.model.TextFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * The {@link Approver} is able to approve data by comparing the data with a baseline.
 */
public class Approver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Approver.class);
    private static final String IDEA_DIFF =
            "C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2018.3.3\\bin\\idea64 diff";
    private static final String BASELINE = "baseline.txt";
    private static final String TO_APPROVE = "toApprove.txt";

    /**
     * Approve a list of strings by comparing the data with a baseline.
     *
     * @param data a list of strings
     */
    public void approve(List<String> data) {

        TextFile baseline = this.createTextFile(BASELINE);
        TextFile toApprove = this.createTextFile(TO_APPROVE);

        try {
            toApprove.writeData(data);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
            Assertions.fail(e.getMessage());
        }

        try {
            if (!toApprove.equals(baseline)) {
                this.callDiffer(toApprove, baseline);
                LOGGER.info("Approve? (y/n)");

                String input = this.readUserInput();

                if (input.equals("y")) {
                    try {
                        baseline.writeData(data);
                    } catch (FileNotFoundException e) {
                        LOGGER.error(e.getMessage());
                        Assertions.fail(e.getMessage());
                    }
                } else {
                    Assertions.fail("Not approved");
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            Assertions.fail(e.getMessage());
        } finally {
            if (toApprove.delete()) {
                LOGGER.info("Delete " + toApprove.getPath());
            }
        }
    }

    private String readUserInput() {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        scanner.close();
        return input;
    }

    private void callDiffer(File toApprove, File baseline) {
        String cmd = IDEA_DIFF + " " + toApprove.getPath() + " " + baseline.getPath();
        LOGGER.info("Call " + IDEA_DIFF);
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            Assertions.fail(e.getMessage());
        }
    }

    private TextFile createTextFile(String path) {
        TextFile textFile = new TextFile(path);
        try {
            if (textFile.createNewFile()) {
                LOGGER.info("Create " + path);
            } else {
                LOGGER.info("Use existing " + path);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            Assertions.fail(e.getMessage());
        }
        return textFile;
    }
}