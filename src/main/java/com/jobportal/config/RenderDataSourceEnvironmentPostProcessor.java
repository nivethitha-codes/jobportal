package com.jobportal.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Render injects DATABASE_URL as postgres://user:pass@host/db.
 * Spring/Hikari need jdbc:postgresql://... plus separate credentials.
 * Linked Render DBs often do not set DATABASE_USERNAME / DATABASE_PASSWORD.
 */
public class RenderDataSourceEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        String raw = firstNonBlank(
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("spring.datasource.url"));

        if (raw == null || raw.isBlank() || raw.contains("${")) {
            return;
        }

        Map<String, Object> props = new HashMap<>();
        boolean onRender = "true".equalsIgnoreCase(environment.getProperty("RENDER"))
                || (raw.contains("render.com") || raw.contains("dpg-"));

        try {
            if (raw.startsWith("postgres://") || raw.startsWith("postgresql://")) {
                parsePostgresUri(raw, onRender, props);
            } else if (raw.startsWith("jdbc:postgresql:")) {
                props.put("spring.datasource.url", ensureSsl(raw, onRender));
            }
        } catch (Exception ignored) {
            if (raw.startsWith("postgres://") || raw.startsWith("postgresql://")) {
                String converted = "jdbc:postgresql://" + raw.substring(raw.indexOf("://") + 3);
                props.put("spring.datasource.url", ensureSsl(converted, onRender));
            }
        }

        String username = firstNonBlank(
                environment.getProperty("DATABASE_USERNAME"),
                environment.getProperty("PGUSER"));
        String password = firstNonBlank(
                environment.getProperty("DATABASE_PASSWORD"),
                environment.getProperty("PGPASSWORD"));

        if (username != null) {
            props.put("spring.datasource.username", username);
        }
        if (password != null) {
            props.put("spring.datasource.password", password);
        }

        if (!props.isEmpty()) {
            environment.getPropertySources()
                    .addFirst(new MapPropertySource("renderDatasource", props));
        }
    }

    private void parsePostgresUri(String raw, boolean onRender, Map<String, Object> props) {
        URI uri = URI.create(raw);
        String userInfo = uri.getUserInfo();
        if (userInfo != null) {
            int idx = userInfo.indexOf(':');
            if (idx >= 0) {
                props.put("spring.datasource.username", decode(userInfo.substring(0, idx)));
                props.put("spring.datasource.password", decode(userInfo.substring(idx + 1)));
            } else {
                props.put("spring.datasource.username", decode(userInfo));
            }
        }

        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String db = uri.getPath() == null ? "" : uri.getPath();
        if (db.startsWith("/")) {
            db = db.substring(1);
        }
        int q = db.indexOf('?');
        if (q >= 0) {
            db = db.substring(0, q);
        }

        String jdbc = "jdbc:postgresql://" + host + ":" + port + "/" + db;
        String query = uri.getQuery();
        if (query != null && !query.isBlank()) {
            jdbc += "?" + query;
        }
        props.put("spring.datasource.url", ensureSsl(jdbc, onRender));
        props.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
    }

    private String ensureSsl(String jdbcUrl, boolean onRender) {
        if (!onRender || jdbcUrl.contains("sslmode=")) {
            return jdbcUrl;
        }
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank() && !value.contains("${")) {
                return value;
            }
        }
        return null;
    }
}
