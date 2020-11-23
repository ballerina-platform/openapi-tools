import ballerina/config;
import ballerina/io;
import ballerina/system;
import ballerina/stringutils;
import ballerina/test;

const BAL_EXEC_PATH = "bal_exec_path";
const OPENAPI_PROJECT = "tests/resources/openapi-validator";
const UTF_8 = "UTF-8";

@test:Config {}
public function testOpenapiValidatorOff() {
    system:Process|error execResult = system:exec(config:getAsString(BAL_EXEC_PATH), {}, OPENAPI_PROJECT, "build",
    "openapi-validator-off");
    string[] logLines = getLogLinesFromExecResult(execResult);
    string msg = "Couldn't find a Ballerina service resource for the path '/{param1}/{param2}' which is documented in the OpenAPI contract";
    validateLog(logLines[0],"warning","openapi-validator-off.bal:14:1", msg);
}

@test:Config {}
public function testOpenapiValidatorOn() {
    system:Process|error execResult = system:exec(config:getAsString(BAL_EXEC_PATH), {}, OPENAPI_PROJECT, "build",
    "openapi-validator-on");
    string[] logLines = getLogLinesFromExecResult(execResult);
    string msg = "Couldn't find a Ballerina service resource for the path '/{param1}/{param2}' which is documented in the OpenAPI contract";
    validateLog(logLines[0],"error","openapi-validator-on.bal:13:9:",msg);


}
// getting the log lines from execution results
function getLogLinesFromExecResult(system:Process|error execResult) returns string[] {
    system:Process result = checkpanic execResult;
    int waitForExit = checkpanic result.waitForExit();
    int exitCode = checkpanic result.exitCode();
    io:ReadableByteChannel readableResult = result.stderr();
    io:ReadableCharacterChannel sc = new (readableResult, UTF_8);
    string outText = checkpanic sc.read(100000);
    string[] logLines = stringutils:split(outText, "\n");
    return logLines;
}

function validateLog(string log, string logLevel, string logLocation, string logMsg) {
    test:assertTrue(stringutils:contains(log, logLevel));
    test:assertTrue(stringutils:contains(log, logLocation));
    test:assertTrue(stringutils:contains(log, logMsg));
}
