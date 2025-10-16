package io.ballerina.openapi;

import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.testng.Assert;
import org.testng.SkipException;

import java.util.ArrayList;
import java.util.List;

public final class TestUtils {

    public static final Filter FILTER = new Filter(new ArrayList<>(), new ArrayList<>());

    public static void compareDiagnosticWarnings(List<Diagnostic> diagnosticList, String... expectedDiagnostics) {
        Assert.assertEquals(diagnosticList.size(), expectedDiagnostics.length);
        for (int i = 0; i < diagnosticList.size(); i++) {
            Assert.assertEquals(diagnosticList.get(i).message(), expectedDiagnostics[i]);
        }
    }

    public static void skipOnWindows() {
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().contains("windows")) {
            throw new SkipException("Skipping this test on Windows.");
        }
    }
}
