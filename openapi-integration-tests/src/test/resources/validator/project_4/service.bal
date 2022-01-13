import ballerina/http;
import ballerina/io;
import ballerina/openapi;

listener http:Listener ep = new(9090);

@openapi:ServiceInfo{
    contract: "openapi.yaml"
}
isolated service /sysobsapi on ep {
    isolated resource function get applications/[int obsId]/metrics/[string startTime]() returns error? {

    }

    isolated resource function get healthz(http:Caller caller, http:Request req) {

        // Send a response back to the caller.
        error? result = caller->respond("Hello Ballerina!");
        if (result is error) {
            io:println("Error in responding: ", result);
        }
    }
}
