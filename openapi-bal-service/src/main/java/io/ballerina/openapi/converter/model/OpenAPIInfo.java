package io.ballerina.openapi.converter.model;

import java.util.Locale;

import static io.ballerina.openapi.converter.Constants.SPLIT_PATTERN;

public class OpenAPIInfo {
    private String title;
    private String version;
    private String contractPath;

    public OpenAPIInfo() {
        this.title = null;
        this.version = null;
        this.contractPath = null;
    }

    public OpenAPIInfo(String title, String version) {
        this.title = title;
        this.version = version;
    }

    public String getTitle() {
        return normalizedTitle(this.title);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    // Generate Title
    private static String normalizedTitle(String serviceName) {
        String[] splits = (serviceName.replaceFirst("/", "")).split(SPLIT_PATTERN);
        StringBuilder stringBuilder = new StringBuilder();
        String title = serviceName;
        if (splits.length > 1) {
            for (String piece : splits) {
                if (piece.isBlank()) {
                    continue;
                }
                stringBuilder.append(piece.substring(0, 1).toUpperCase(Locale.ENGLISH) + piece.substring(1));
                stringBuilder.append(" ");
            }
            title = stringBuilder.toString().trim();
        } else if (splits.length == 1 && !splits[0].isBlank()) {
            stringBuilder.append(splits[0].substring(0, 1).toUpperCase(Locale.ENGLISH) + splits[0].substring(1));
            title = stringBuilder.toString().trim();
        }
        return title;
    }
    public String getContractPath() {
        return contractPath;
    }

    public void setContractPath(String contractPath) {
        this.contractPath = contractPath;
    }

}
