package org.japprove.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.japprove.config.ApprovalTestingConfiguration;
import org.japprove.exceptions.BaselineCandidateCreationFailedException;
import org.japprove.exceptions.BaselineCandidateNotFoundException;
import org.japprove.exceptions.BaselineCreationFailedException;
import org.japprove.exceptions.BaselineNotFoundException;
import org.japprove.exceptions.CopyingFailedException;
import org.japprove.exceptions.DiffingFailedException;
import org.japprove.exceptions.FileCreationFailedException;
import org.japprove.files.JsonFile;
import org.japprove.files.TextFile;

/**
 * An implementation of the {@link BaselineRepository} with a text file based approach.
 */
public class BaselineRepositoryImpl implements BaselineRepository {

    static final String TXT_EXTENSION = ".txt";
    private String baselineDirectory;
    private String baselineCandidateDirectory;

    public BaselineRepositoryImpl(ApprovalTestingConfiguration config) {
        baselineDirectory = config.getBaselineDirectory();
        baselineCandidateDirectory = config.getBaselineCandidateDirectory();
    }

    @Override
    public void createBaselineCandidate(String data, String name)
            throws BaselineCandidateCreationFailedException {
        TextFile baselineCandidate =
                new TextFile(baselineCandidateDirectory + name + TXT_EXTENSION);
        try {
            baselineCandidate.create();
            baselineCandidate.writeData(data);
        } catch (IOException | FileCreationFailedException e) {
            throw new BaselineCandidateCreationFailedException(baselineCandidate.getName());
        }
    }

    @Override
    public void createBaselineCandidate(JsonNode data, String name)
            throws BaselineCandidateCreationFailedException {
        JsonFile baselineCandidate =
                new JsonFile(baselineCandidateDirectory + name + TXT_EXTENSION);
        try {
            baselineCandidate.create();
            baselineCandidate.writeData(data);
        } catch (FileCreationFailedException | FileNotFoundException | JsonProcessingException e) {
            throw new BaselineCandidateCreationFailedException(baselineCandidate.getName());
        }
    }

    @Override
    public boolean removeBaselineCandidate(String name) {
        try {
            return getFile(name, baselineCandidateDirectory).delete();
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    @Override
    public List<String> getBaselineCandidateNames() {
        File directory = new File(baselineCandidateDirectory);
        List<String> baselineCandidateNames = new ArrayList<>();
        if (directory.exists() && directory.listFiles() != null) {
            for (File file : directory.listFiles()) {
                baselineCandidateNames.add(file.getName().replace(TXT_EXTENSION, ""));
            }
        }
        return baselineCandidateNames;
    }

    @Override
    public JsonNode getContentOfJsonBaseline(String baseline) throws BaselineNotFoundException {
        return this.getJsonBaseline(baseline).readData();
    }

    @Override
    public String getContentOfTextBaseline(String baseline) throws BaselineNotFoundException {
        try {
            return this.getTextBaseline(baseline).readData();
        } catch (IOException e) {
            throw new BaselineNotFoundException(baseline);
        }
    }

    @Override
    public void copyBaselineCandidateToBaseline(String baselineCandidateName)
            throws BaselineCandidateNotFoundException, BaselineCreationFailedException,
            CopyingFailedException {
        TextFile baselineCandidate;
        try {
            baselineCandidate = getFile(baselineCandidateName, baselineCandidateDirectory);
        } catch (FileNotFoundException e) {
            throw new BaselineCandidateNotFoundException(baselineCandidateName);
        }
        TextFile baseline;
        try {
            baseline = getFile(baselineCandidateName, baselineDirectory);
        } catch (FileNotFoundException e) {
            try {
                baseline = createBaseline(baselineCandidateName);
            } catch (BaselineCreationFailedException ex) {
                throw new BaselineCreationFailedException(baselineCandidateName);
            }
        }
        try {
            copyFiles(baselineCandidate, baseline);
        } catch (IOException e) {
            throw new CopyingFailedException(
                    "Cannot copy content of " + baselineCandidateName + " to the baseline");
        }
    }

    @Override
    public String getDifferences(String baselineCandidateName)
            throws BaselineCandidateNotFoundException, BaselineNotFoundException {
        TextFile baselineCandidateFile;
        try {
            baselineCandidateFile = getFile(baselineCandidateName, baselineCandidateDirectory);
        } catch (FileNotFoundException e) {
            throw new BaselineCandidateNotFoundException(baselineCandidateName);
        }
        TextFile baseline;
        try {
            baseline = getFile(baselineCandidateName, baselineDirectory);
        } catch (FileNotFoundException e) {
            throw new BaselineNotFoundException(baselineCandidateName);
        }
        return formatDifferences(computeDifferences(baselineCandidateFile, baseline));
    }

    @Override
    public boolean baselineExists(String baselineCandidateName) {
        try {
            getFile(baselineCandidateName, baselineDirectory);
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public File getBaselineCandidateAsFile(String name) throws IOException {
        return getFile(name, baselineCandidateDirectory);
    }

    @Override
    public File getBaselineAsFile(String name) throws IOException {
        return getFile(name, baselineDirectory);
    }

    private List<String> computeDifferences(TextFile revisedFile, TextFile originalFile) {
        try {
            List<String> original = readFileLineByLine(originalFile.getPath());
            List<String> revised = readFileLineByLine(revisedFile.getPath());
            Patch<String> patch = DiffUtils.diff(original, revised);
            return UnifiedDiffUtils
                    .generateUnifiedDiff("Baseline", "toApprove", original, patch, 0);
        } catch (DiffException | IOException e) {
            throw new DiffingFailedException("Cannot compute differences! " + e);
        }
    }

    private List<String> readFileLineByLine(String fileName) throws IOException {
        List<String> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = reader.readLine()) != null) {
            result.add(line);
        }
        reader.close();
        return result;
    }

    private String formatDifferences(List<String> differences) {
        StringBuilder builder = new StringBuilder();
        for (String difference : differences) {
            builder.append(difference);
            builder.append("\n");
        }
        return builder.toString();
    }

    private TextFile getTextBaseline(String baselineName) throws BaselineNotFoundException {
        TextFile baseline = new TextFile(baselineDirectory + baselineName + TXT_EXTENSION);
        if (!baseline.exists()) {
            throw new BaselineNotFoundException(baselineName);
        }
        return baseline;
    }

    private JsonFile getJsonBaseline(String baselineName) throws BaselineNotFoundException {
        JsonFile baseline = new JsonFile(baselineDirectory + baselineName + TXT_EXTENSION);
        if (!baseline.exists()) {
            throw new BaselineNotFoundException(baselineName);
        }
        return baseline;
    }

    private TextFile createBaseline(String baselineName) throws BaselineCreationFailedException {
        TextFile baseline = new TextFile(baselineDirectory + baselineName + TXT_EXTENSION);
        try {
            baseline.create();
        } catch (FileCreationFailedException e) {
            throw new BaselineCreationFailedException(baseline.getName());
        }
        return baseline;
    }

    private TextFile getFile(String baselineCandidateName, String directoryPath)
            throws FileNotFoundException {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.listFiles() != null) {
            for (File file : directory.listFiles()) {
                if ((file.getPath().equals(directoryPath + baselineCandidateName))
                        || (file.getPath()
                        .equals(directoryPath + baselineCandidateName + TXT_EXTENSION))) {
                    return new TextFile(file.getPath());
                }
            }
        }
        throw new FileNotFoundException(baselineCandidateName);
    }

    private void copyFiles(TextFile from, TextFile to) throws IOException {
        FileInputStream inputStream = new FileInputStream(from);
        FileOutputStream outputStream = new FileOutputStream(to);
        inputStream.getChannel().transferTo(0, from.length(), outputStream.getChannel());
        inputStream.close();
        outputStream.close();
    }
}
