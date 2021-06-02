package io.ballerina.generators.client;

import org.testng.Assert;
import org.testng.annotations.Test;

import static io.ballerina.generators.BallerinaClientGenerator.generatePathWithPathParameter;

public class PathParameterTests {
    @Test(description = "Generate Client for path parameter has parameter name as key word - unit tests for method")
    public void generatePathWithPathParameterTests() {
        Assert.assertEquals(generatePathWithPathParameter("/v1/v2"), "/v1/v2");
        Assert.assertEquals(generatePathWithPathParameter("/v1/{version}/v2/{name}"),
                "/v1/${'version}/v2/${name}");
        Assert.assertEquals(generatePathWithPathParameter("/v1/{version}/v2/{limit}"),
                "/v1/${'version}/v2/${'limit}");
        Assert.assertEquals(generatePathWithPathParameter("/v1/{age}/v2/{name}"), "/v1/${age}/v2/${name}");
    }
}
