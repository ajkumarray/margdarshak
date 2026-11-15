package com.ajkumarray.margdarshak;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.ajkumarray.margdarshak.service.UserService;
import com.ajkumarray.margdarshak.util.SpringContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AWS Lambda entry point.
 *
 * <p>The Spring context is built once in the static initializer - at init time, which for
 * SnapStart-enabled functions runs at deploy time and is captured in the snapshot. Each
 * invocation then only restores the snapshot and proxies the request into Spring MVC.
 *
 * <p>Function URLs deliver events in the API Gateway HTTP API v2 payload format, hence
 * {@code getHttpApiV2ProxyHandler}. The only other event is the daily EventBridge schedule
 * (template.yaml), which runs the refresh-token cleanup instead of an HTTP request.
 */
public class LambdaHandler implements RequestStreamHandler {

    private static final SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> HANDLER;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        try {
            /*
             * Second arg = active Spring profiles; "lambda" pulls in application-lambda.yml.
             */
            HANDLER = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(MargdarshakApplication.class, "lambda");
            /*
             * The library's default binary-content-type list is just octet-stream/jpeg/png/gif,
             * so JS/CSS/fonts get pushed through a text round-trip and silently corrupted
             * (e.g. Swagger UI's JS bundle rendering blank). Force them through as raw
             * base64-encoded bytes instead.
             */
            SpringBootLambdaContainerHandler.getContainerConfig().addBinaryContentTypes("application/javascript",
                    "text/javascript", "text/css", "font/woff", "font/woff2", "image/svg+xml");
            /*
             * The default ApacheCombinedServletLogFormatter reads the API Gateway request context
             * unconditionally; on a servlet ERROR dispatch to /error (any 403/404/500) that
             * attribute is absent, so it NPEs and the real response is replaced with a 502.
             */
            HANDLER.setLogFormatter((request, response, securityContext) ->
                    request.getMethod() + " " + request.getRequestURI() + " " + response.getStatus()
                            + System.lineSeparator());
        } catch (ContainerInitializationException e) {
            throw new RuntimeException("Could not initialize Spring Boot application on Lambda", e);
        }
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        byte[] event = input.readAllBytes();
        if (isScheduledEvent(event)) {
            int removed = SpringContextHolder.getBean(UserService.class).cleanupRefreshTokens();
            context.getLogger().log("Refresh token cleanup removed " + removed + " rows" + System.lineSeparator());
            output.write(("{\"removed\":" + removed + "}").getBytes(StandardCharsets.UTF_8));
            return;
        }
        HANDLER.proxyStream(new ByteArrayInputStream(event), output, context);
    }

    private static boolean isScheduledEvent(byte[] event) {
        try {
            return "aws.events".equals(OBJECT_MAPPER.readTree(event).path("source").asText());
        } catch (IOException e) {
            return false;
        }
    }
}
