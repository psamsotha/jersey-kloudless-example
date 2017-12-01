package com.example.jersey.resource;


import com.example.jersey.auth.AccessTokenManager;
import com.example.jersey.error.ErrorResponse;
import com.kloudless.KClient;
import com.kloudless.model.File;
import org.glassfish.jersey.client.oauth2.TokenResult;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.spi.ExecutorServiceProvider;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;


@Path("upload")
public class UploadResource {

    @Inject
    private AccessTokenManager tokenManager;

    @Context
    private HttpServletRequest request;


    @Inject
    private ExecutorServiceProvider executorServiceProvider;


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public void uploadFile(@Suspended AsyncResponse asyncResponse,
                           @FormDataParam("scope") String scope,
                           @FormDataParam("file") java.io.File file,
                           @FormDataParam("file") FormDataContentDisposition fdcd) throws Exception {

        final HttpSession session = request.getSession(false);
        if (session == null) {
            asyncResponse.resume(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }
        final String sessionId = session.getId();


        final Map<String, TokenResult> tokens = this.tokenManager.getTokensForSession(sessionId);
        if (tokens == null) {
            final ErrorResponse response = new ErrorResponse(500, "No files uploaded", new Date());
            asyncResponse.resume(Response.serverError()
                    .entity(response)
                    .type(MediaType.APPLICATION_JSON)
                    .build());
            return;
        }

        final String[] services = scope.split("\\s+");

        final ExecutorService executorService = this.executorServiceProvider.getExecutorService();

        executorService.submit(new UploadTask(services, file, fdcd.getFileName(), executorService, asyncResponse, tokens));
    }


    private static class UploadTask implements Runnable {

        private final String[] services;
        private final java.io.File file;
        private final ExecutorService executorService;
        private final Map<String, TokenResult> tokens;
        private final AsyncResponse asyncResponse;
        private final String fileName;

        UploadTask(String[] services,
                   java.io.File file,
                   String fileName,
                   ExecutorService executorService,
                   AsyncResponse asyncResponse,
                   Map<String, TokenResult> tokens) {

            this.services = services;
            this.file = file;
            this.fileName = fileName;
            this.asyncResponse = asyncResponse;
            this.executorService = executorService;

            this.tokens = Collections.unmodifiableMap(tokens);
        }

        @Override
        public void run() {
            final CountDownLatch latch = new CountDownLatch(this.services.length);

            final List<String> successfulUploads = new CopyOnWriteArrayList<>();
            final List<String> failedUploads = new CopyOnWriteArrayList<>();

            for (String service: this.services) {
                TokenResult tokenResult = tokens.get(service);

                if (tokenResult == null) {
                    failedUploads.add(service);
                    latch.countDown();
                    continue;
                }

                this.executorService.submit(() -> {
                    try {
                        KClient storageClient = new KClient(
                                tokenResult.getAccessToken(),
                                tokenResult.getAllProperties().get("account_id").toString(),
                                null);

                        File newFile = storageClient.uploadFile("root", fileName, false, file.getAbsolutePath());

                        successfulUploads.add(service);
                        latch.countDown();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        failedUploads.add(service);
                        latch.countDown();
                    }
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            asyncResponse.resume(Response.ok(new UploadResult(successfulUploads, failedUploads))
                    .type(MediaType.APPLICATION_JSON)
                    .build());
            file.delete();
        }
    }


    public static class UploadResult {

        private List<String> successfulServices = new ArrayList<>();
        private List<String> failedServices = new ArrayList<>();

        UploadResult(List<String> successfulServices,
                     List<String> failedServices) {
            this.successfulServices = successfulServices;
            this.failedServices = failedServices;
        }

        public List<String> getSuccessfulServices() {
            return this.successfulServices;
        }

        public List<String> getFailedServices() {
            return this.failedServices;
        }
    }
}
