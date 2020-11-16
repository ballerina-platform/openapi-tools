//import ballerina/http;
//import ballerina/log;
//import ballerina/openapi;
//
//listener http:Listener ep0 = new(9091, config = {host: "localhost"});
//
//@openapi:ServiceInfo {
//        contract: "modules/openapi-validator-off/resources/openapi_validator_off.yaml",
//        failOnErrors: false
//}
//@http:ServiceConfig {
//        basePath: "/api/v1"
//}
//service openapi_validator_off on ep0{
//    @http:ResourceConfig {
//        methods:["GET"],
//        path:"/{param1}/{param3}"
//    }
//    resource function test2Params (http:Caller caller, http:Request req,  string param1,  string param3) returns error? {
//        string msg = "Hello, " + param1 + " " + param3 ;
//        var result = caller->respond(<@untainted> msg);
//        if (result is error) {
//            log:printError("Error sending response", result);
//        }
//    }
//}

# Calculates the value of the 'a' raised to the power of 'b'.
# ```ballerina
# float aPowerB = math:pow(3.2, 2.4);
# ```
#
# + a - Base value
# + b - Exponential value
# + return - Calculated exponential value
public isolated function pow(float a, float b) returns float {
    return 0;
}