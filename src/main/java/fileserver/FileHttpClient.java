package fileserver;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;

@Client("http://localhost:8080")
public interface FileHttpClient {

    @Get("/downloadFileBySystemFile")
    HttpResponse<byte[]> downloadFileBySystemFile();
}
