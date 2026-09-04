package com.ajkumarray.margdarshak;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

/**
 * AWS Lambda entry point.
 *
 * <p>The Spring context is built once in the static initializer - at init time, which for
 * SnapStart-enabled functions runs at deploy time and is captured in the snapshot. Each
 * invocation then only restores the snapshot and proxies the request into Spring MVC.
 *
 * <p>Function URLs deliver events in the API Gateway HTTP API v2 payload format, hence
 * {@code getHttpApiV2ProxyHandler}.
 */
public class LambdaHandler implements RequestStreamHandler {

    private static final SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> HANDLER;

    static {
        try {
            // Second arg = active Spring profiles; "lambda" pulls in application-lambda.yml.
            HANDLER = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(MargdarshakApplication.class, "lambda");
            // The library's default binary-content-type list is just octet-stream/jpeg/png/gif,
            // so JS/CSS/fonts get pushed through a text round-trip and silently corrupted
            // (e.g. Swagger UI's JS bundle rendering blank). Force them through as raw
            // base64-encoded bytes instead.
            SpringBootLambdaContainerHandler.getContainerConfig().addBinaryContentTypes("application/javascript",
                    "text/javascript", "text/css", "font/woff", "font/woff2", "image/svg+xml");
        } catch (ContainerInitializationException e) {
            throw new RuntimeException("Could not initialize Spring Boot application on Lambda", e);
        }
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        HANDLER.proxyStream(input, output, context);
    }
}
