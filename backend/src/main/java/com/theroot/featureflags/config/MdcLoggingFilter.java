package com.theroot.featureflags.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that enriches the Mapped Diagnostic Context (MDC) with
 * request-scoped contextual information for structured logging.
 * <p>
 * Populates MDC with:
 * <ul>
 *   <li>{@code traceId} — a unique request identifier (UUID fallback if Micrometer Tracing is not active)</li>
 *   <li>{@code requestMethod} — HTTP method (GET, POST, ...)</li>
 *   <li>{@code requestUri} — the request path</li>
 *   <li>{@code queryString} — the raw query string (if present)</li>
 *   <li>{@code statusCode} — the HTTP response status code</li>
 *   <li>{@code durationMs} — request processing time in milliseconds</li>
 * </ul>
 * <p>
 * MDC is always cleaned up in a {@code finally} block to prevent context leaks.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MdcLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Ensure a traceId exists (Micrometer Tracing may set one, but we provide a fallback)
        if (MDC.get("traceId") == null) {
            MDC.put("traceId", UUID.randomUUID().toString().replace("-", ""));
        }

        MDC.put("requestMethod", request.getMethod());
        MDC.put("requestUri", request.getRequestURI());

        String qs = request.getQueryString();
        if (qs != null) {
            MDC.put("queryString", qs);
        }

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("statusCode", String.valueOf(response.getStatus()));
            MDC.put("durationMs", String.valueOf(duration));

            // Log a summary of every HTTP request
            log.info("{} {} -> {} ({} ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);

            // Clean up MDC to prevent context leaks on pooled threads
            MDC.remove("traceId");
            MDC.remove("requestMethod");
            MDC.remove("requestUri");
            MDC.remove("queryString");
            MDC.remove("statusCode");
            MDC.remove("durationMs");
        }
    }
}
