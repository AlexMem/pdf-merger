package ru.seloid.pdf.merger.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

public class Merger {
    private static final String EMPTY_STRING = "";
    private static final String RESULT_FILE_DEFAULT_NAME_TEMPLATE = "%s\\result_%s.pdf";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static String merge(final String directoryPath) throws Exception {
        return merge(directoryPath, EMPTY_STRING);
    }

    public static String merge(final String directoryPath, final String resultFilePath) throws Exception {
        try (final Stream<Path> filePaths = Files.list(Paths.get(directoryPath))) {
            return merge(filePaths.sorted(Comparator.comparing(Path::getFileName)), resultFilePath);
        }
    }

    public static String merge(final Collection<String> fileNames) throws Exception {
        return merge(fileNames, EMPTY_STRING);
    }

    public static String merge(final Collection<String> fileNames, final String resultFilePath) throws Exception {
        Objects.requireNonNull(fileNames, "Empty file list");
        final Stream<Path> filePaths = fileNames.stream().map(Paths::get);
        return merge(filePaths, resultFilePath);
    }

    private static String merge(final Stream<Path> filePaths, final String resultFilePath) throws Exception {
        final String inResultFilePath = !StringUtils.isBlank(resultFilePath) ? resultFilePath : createResultFilePath();
        if (!inResultFilePath.endsWith(".pdf")) {
            throw new Exception("Bad file format, .pdf required");
        }
        final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        filePaths.filter(file -> !Files.isDirectory(file))
                 .map(Path::toFile)
                 .forEach(source -> {
                     try {
                         pdfMergerUtility.addSource(source);
                     } catch (FileNotFoundException e) {
                         throw new RuntimeException(e);
                     }
                 });
        pdfMergerUtility.setDestinationFileName(inResultFilePath);
        pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        return inResultFilePath;
    }

    public static String createResultFilePath() {
        final String currentDirectory = Paths.get(EMPTY_STRING).toAbsolutePath().toString();
        return createResultFilePath(currentDirectory);
    }

    public static String createResultFilePath(final String directory) {
        return String.format(RESULT_FILE_DEFAULT_NAME_TEMPLATE, directory, LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }
}
