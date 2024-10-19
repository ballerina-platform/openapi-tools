import ballerina/http;
import ballerina/openapi;

listener http:Listener httpListener = check new (9000);

final http:Client xeClientSimple = check new ("https://xecdapi.xe.com/v1");

service /convert on httpListener {

    @openapi:ResourceInfo {
        examples: {
            "response": {
                "201": {
                    "examples": {
                        "application/json": {
                            "responseExample01": {
                                "filePath": "example.json"
                            }
                        }
                    }
                }
            },
            "requestBody": {
                "application/json": {
                    "requestExample01": {
                        "value": {
                            "fromCurrancy": "LKR",
                            "toCurrancy": "USD"
                        }
                    }
                }
            }
        }
    }
    resource function post rate(record {} payload) returns record {}? {
        return {};
    }
}
