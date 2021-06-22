package org.ballerinalang.ballerina;

/**
 * The extended service for the JsonToBalRecord endpoint.
 *
 * @since 2.0.0
 */
public class OpenAPIConverterRequest {
    private String documentFilePath;

    public OpenAPIConverterRequest(String documentFilePath) {
        this.documentFilePath = documentFilePath;
    }

    public String getDocumentFilePath() {
        return documentFilePath;
    }

    public void setDocumentFilePath(String documentFilePath) {
        this.documentFilePath = documentFilePath;
    }
}
