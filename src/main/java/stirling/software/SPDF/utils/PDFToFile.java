package stirling.software.SPDF.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import io.github.pixee.security.Filenames;

import stirling.software.SPDF.utils.ProcessExecutor.ProcessExecutorResult;

public class PDFToFile {
    private static final Logger logger = LoggerFactory.getLogger(PDFToFile.class);
    private static final int TIMEOUT = 180; // 180 seconds or 3 minutes
    private static final int NUM_PROCESSES = 4;

    public ResponseEntity<byte[]> processPdfToHtml(MultipartFile inputFile)
            throws IOException, InterruptedException {
        if (!"application/pdf".equals(inputFile.getContentType())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Get the original PDF file name without the extension
        String originalPdfFileName = Filenames.toSimpleFileName(inputFile.getOriginalFilename());
        String pdfBaseName = originalPdfFileName;
        if (originalPdfFileName.contains(".")) {
            pdfBaseName = originalPdfFileName.substring(0, originalPdfFileName.lastIndexOf('.'));
        }

        Path tempInputFile = null;
        Path tempOutputDir = null;
        byte[] fileBytes;
        String fileName = "temp.file";

        try {
            // Save the uploaded file to a temporary location
            tempInputFile = Files.createTempFile("input_", ".pdf");
            inputFile.transferTo(tempInputFile);

            // Prepare the output directory
            tempOutputDir = Files.createTempDirectory("output_");

            // Run the pdftohtml command with complex output
            List<String> command =
                    new ArrayList<>(
                            Arrays.asList(
                                    "pdftohtml", "-c", tempInputFile.toString(), pdfBaseName));

            ProcessExecutorResult returnCode =
                    ProcessExecutor.getInstance(ProcessExecutor.Processes.PDFTOHTML)
                            .runCommandWithOutputHandling(command, tempOutputDir.toFile());

            // Get output files
            List<File> outputFiles = Arrays.asList(tempOutputDir.toFile().listFiles());

            // Return output files in a ZIP archive
            fileName = pdfBaseName + "ToHtml.zip";
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
                for (File outputFile : outputFiles) {
                    ZipEntry entry = new ZipEntry(outputFile.getName());
                    zipOutputStream.putNextEntry(entry);
                    try (FileInputStream fis = new FileInputStream(outputFile)) {
                        IOUtils.copy(fis, zipOutputStream);
                    } catch (IOException e) {
                        logger.error("Exception writing zip entry", e);
                    }
                    zipOutputStream.closeEntry();
                }
            } catch (IOException e) {
                logger.error("Exception writing zip", e);
            }
            fileBytes = byteArrayOutputStream.toByteArray();

        } finally {
            // Clean up the temporary files
            if (tempInputFile != null) Files.deleteIfExists(tempInputFile);
            if (tempOutputDir != null) FileUtils.deleteDirectory(tempOutputDir.toFile());
        }

        return WebResponseUtils.bytesToWebResponse(
                fileBytes, fileName, MediaType.APPLICATION_OCTET_STREAM);
    }

    public ResponseEntity<byte[]> processPdfToOfficeFormat(
            MultipartFile inputFile, String outputFormat, String libreOfficeFilter)
            throws IOException, InterruptedException {

        if (!"application/pdf".equals(inputFile.getContentType())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String originalPdfFileName = Filenames.toSimpleFileName(inputFile.getOriginalFilename());

        if (originalPdfFileName == null || "".equals(originalPdfFileName.trim())) {
            originalPdfFileName = "output.pdf";
        }
        String pdfBaseName = originalPdfFileName;
        if (originalPdfFileName.contains(".")) {
            pdfBaseName = originalPdfFileName.substring(0, originalPdfFileName.lastIndexOf('.'));
        }

        List<String> allowedFormats =
                Arrays.asList("doc", "docx", "odt", "ppt", "pptx", "odp", "rtf", "xml", "txt:Text");
        if (!allowedFormats.contains(outputFormat)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Path tempInputFile = null;
        Path tempOutputDir = null;
        byte[] fileBytes;
        String fileName = "temp.file";

        try {
            tempInputFile = Files.createTempFile("input_", ".pdf");
            inputFile.transferTo(tempInputFile);

            tempOutputDir = Files.createTempDirectory("output_");

            List<String> command =
                    new ArrayList<>(
                            Arrays.asList(
                                    "soffice",
                                    "--headless",
                                    "--nologo",
                                    "--infilter=" + libreOfficeFilter,
                                    "--convert-to",
                                    outputFormat,
                                    "--outdir",
                                    tempOutputDir.toString(),
                                    tempInputFile.toString()));
            logger.info("Executing command: " + command);
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            Future<?> processFuture =
                    scheduler.submit(
                            () -> {
                                try {
                                    process.waitFor();
                                } catch (Exception e) {
                                    logger.error(e.getMessage());
                                    Thread.currentThread().interrupt();
                                }
                            });

            try {
                processFuture.get(4, TimeUnit.MINUTES); // 等待4分钟
            } catch (TimeoutException e) {
                process.destroy(); // 超时则杀死进程
                throw new RuntimeException("Conversion process took too long and was terminated.");
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } finally {
                scheduler.shutdown();
            }

            List<File> outputFiles = Arrays.asList(tempOutputDir.toFile().listFiles());

            if (outputFiles.size() == 1) {
                File outputFile = outputFiles.get(0);
                if ("txt:Text".equals(outputFormat)) {
                    outputFormat = "txt";
                }
                fileName = pdfBaseName + "." + outputFormat;
                fileBytes = FileUtils.readFileToByteArray(outputFile);
            } else {
                fileName = pdfBaseName + "To" + outputFormat + ".zip";
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
                    for (File outputFile : outputFiles) {
                        ZipEntry entry = new ZipEntry(outputFile.getName());
                        zipOutputStream.putNextEntry(entry);
                        try (FileInputStream fis = new FileInputStream(outputFile)) {
                            IOUtils.copy(fis, zipOutputStream);
                        } catch (IOException e) {
                            logger.error("Exception writing zip entry", e);
                        }

                        zipOutputStream.closeEntry();
                    }
                } catch (IOException e) {
                    logger.error("Exception writing zip", e);
                }

                fileBytes = byteArrayOutputStream.toByteArray();
            }

        } finally {
            Files.deleteIfExists(tempInputFile);
            if (tempOutputDir != null) FileUtils.deleteDirectory(tempOutputDir.toFile());
        }
        logger.info("fileBytes=" + fileBytes.length);
        return WebResponseUtils.bytesToWebResponse(
                fileBytes, fileName, MediaType.APPLICATION_OCTET_STREAM);
    }
}
