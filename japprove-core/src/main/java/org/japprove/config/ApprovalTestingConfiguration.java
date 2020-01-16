package org.japprove.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A configuration class for approval testing. The information is read from the japprove.properties
 * file in the resources directory.
 */
public class ApprovalTestingConfiguration {

    private static final String APPROVAL_TESTING_PROPERTIES = "src"
            + File.separator
            + "test"
            + File.separator
            + "resources"
            + File.separator
            + "japprove.properties";
    private static final String DEFAULT_IDEA_DIFF = "C:"
            + File.separator
            + "Program Files"
            + File.separator
            + "JetBrains"
            + File.separator
            + "IntelliJ IDEA Community Edition 2019.2.3"
            + File.separator
            + "bin"
            + File.separator
            + "idea64 diff";
    private static final String DEFAULT_BASELINE_DIRECTORY = "baselines" + File.separator;
    private static final String DEFAULT_BASELINE_CANDIDATE_DIRECTORY = "build"
            + File.separator
            + "baselineCandidates"
            + File.separator;
    private static final Logger LOGGER = LogManager.getLogger(ApprovalTestingConfiguration.class);
    private String baselineDirectory;
    private String baselineCandidateDirectory;
    private String diffTool;

    public ApprovalTestingConfiguration() {
        loadProperties();
    }

    private void loadProperties() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(APPROVAL_TESTING_PROPERTIES));
            if (props.getProperty("baselineDirectory") == null) {
                baselineDirectory = DEFAULT_BASELINE_DIRECTORY;
            } else {
                baselineDirectory = props
                        .getProperty("baselineDirectory")
                        .replace("\\", File.separator)
                        .replace("/", File.separator);
            }
            if (props.getProperty("toApproveDirectory") == null) {
                baselineCandidateDirectory = DEFAULT_BASELINE_CANDIDATE_DIRECTORY;
            } else {
                baselineCandidateDirectory = props
                        .getProperty("baselineCandidateDirectory")
                        .replace("\\", File.separator)
                        .replace("/", File.separator);
            }
            if (props.getProperty("diffTool") == null) {
                diffTool = DEFAULT_IDEA_DIFF;
            } else {
                diffTool = props
                        .getProperty("diffTool")
                        .replace("\\", File.separator)
                        .replace("/", File.separator);
            }
            LOGGER.info("Loading properties from: " + APPROVAL_TESTING_PROPERTIES);
        } catch (IOException e) {
            baselineDirectory = DEFAULT_BASELINE_DIRECTORY;
            baselineCandidateDirectory = DEFAULT_BASELINE_CANDIDATE_DIRECTORY;
            diffTool = DEFAULT_IDEA_DIFF;
            LOGGER.info("Using default properties");
        }
    }

    /**
     * Returns the path to the baseline directory.
     *
     * @return the path to the baseline directory.
     */
    public String getBaselineDirectory() {
        return baselineDirectory;
    }

    /**
     * Returns the path to the baselineCandidate directory.
     *
     * @return the path to the baselineCandidate directory.
     */
    public String getBaselineCandidateDirectory() {
        return baselineCandidateDirectory;
    }

    /**
     * Returns the path to the diff tool.
     *
     * @return the path to the diff tool.
     */
    public String getDiffTool() {
        return diffTool;
    }
}