package com.thred.datingapp.common.config;


import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import jakarta.annotation.PostConstruct;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

@Configuration
public class P6SpyConfig implements MessageFormattingStrategy {

    private static final String PACKAGE_PREFIX = "com.thred.datingapp";
    private static final String REPOSITORY_KEYWORD = "repository";
    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(this.getClass().getName());
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category,
                                String prepared, String sql, String url) {
        if (category.equals("commit")) {
            return String.format("[%s] | %d ms | Transaction committed", category.trim(), elapsed);
        }
        if (category.equals("rollback")) {
            return String.format("[%s] | %d ms | Transaction rolled back", category.trim(), elapsed);
        }
        return String.format("[%s] | %d ms | %s", category, elapsed,
                formatSql(category, sql));
    }

    private String stackTrace() {
        return Stream.of(new Throwable().getStackTrace())
                .filter(t -> t.toString().startsWith(PACKAGE_PREFIX)
                        && t.toString().contains(REPOSITORY_KEYWORD))
                .map(t -> {
                    // StackTraceElement에서 className만 가져와서 변경
                   String className =  t.getClassName()
                           .replace("com.thred.datingapp.community.repository.", "");
                    // 새로운 StackTraceElement 객체를 반환
                    return new StackTraceElement(className, t.getMethodName(), t.getFileName(), t.getLineNumber());
                })  // 해당 부분 제거
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }

    private String formatSql(String category, String sql) {
        if (sql != null && !sql.trim().isEmpty() && Category.STATEMENT.getName().equals(category)) {
            String trimmedSql = sql.trim();
            String firstWord = trimmedSql.split("\\s+")[0].toLowerCase(Locale.ROOT);

            return stackTrace() + (firstWord.startsWith("create") || firstWord.startsWith("alter")
                    || firstWord.startsWith("comment")
                    ? FormatStyle.DDL.getFormatter().format(sql)
                    : FormatStyle.BASIC.getFormatter().format(sql));
        }

        // sql이 없거나 비어 있으면 기본 메시지 출력
        return stackTrace() + "No SQL statement executed";
    }

}

