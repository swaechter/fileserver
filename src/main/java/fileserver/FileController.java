package fileserver;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.SystemFile;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;

@Controller("/")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    // Http client to access ourselves/this controller
    private final FileHttpClient fileHttpClient;

    // Define a static file to store the current uploaded file
    private final File currentFile;

    // This file is static so we have to save the uploaded file name in a separate variable
    private String currentFileName;

    public FileController(FileHttpClient fileHttpClient) {
        this.fileHttpClient = fileHttpClient;
        this.currentFile = new File("temporary_file.tmp");
    }

    @Post("/uploadFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public HttpResponse<?> uploadFile(CompletedFileUpload file) throws Exception {
        try (OutputStream outputStream = new FileOutputStream(currentFile)) {
            currentFileName = file.getFilename();
            IOUtils.copy(file.getInputStream(), outputStream);
            logger.info("File uploaded with hash " + getSha2HashFromFile(currentFile));
            return HttpResponse.ok("File uploaded with hash " + getSha2HashFromFile(currentFile));
        }
    }

    @Get("/downloadFileBySystemFile")
    public SystemFile downloadFileBySystemFile() throws Exception {
        logger.info("Letting user download a file via downloadFileBySystemFile with the hash " + getSha2HashFromFile(currentFile));
        return new SystemFile(currentFile).attach(currentFileName);
    }

    @Get("/downloadFileByHttpClient")
    public HttpResponse<?> downloadFileByHttpClient() throws Exception {
        byte[] data = fileHttpClient.downloadFileBySystemFile().body();
        logger.info("Letting user download a file via downloadFileByHttpClient with the hash " + getSha2HashFromByteArray(data));
        return HttpResponse.ok(getSha2HashFromByteArray(data));
    }

    private String getSha2HashFromFile(File file) throws Exception {
        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] fileBlob = IOUtils.toByteArray(inputStream);
            return getSha2HashFromByteArray(fileBlob);
        }
    }

    private String getSha2HashFromByteArray(byte[] data) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hash = messageDigest.digest(data);
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toLowerCase();
    }
}
