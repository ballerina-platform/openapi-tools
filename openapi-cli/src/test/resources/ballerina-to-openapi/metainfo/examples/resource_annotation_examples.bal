import ballerina/http;
import ballerina/openapi;

listener http:Listener httpListener = check new (9000);

service /convert on httpListener {

    @openapi:ResourceInfo {
        examples: {
            "response": {
                "201": {
                    "examples": {
                        "application/json": {
                            "json01": {
                                "filePath": "example.json"
                            },
                            "json02": {
                                "value": {
                                    "fromCurrency": "EUR",
                                    "toCurrency": "LKR",
                                    "fromAmount": 200,
                                    "toAmount": 60000,
                                    "timestamp": "2024-07-14"
                                }
                            }
                            // "json03": {
                            //     "filePath": "example.json"
                            // },
                        },
                        "application/xml": {
                            "xml1": {
                                "filePath": "example.json"
                            },
                            "xml2": {
                                "value": {
                                    "fromCurrency": "EUR",
                                    "toCurrency": "LKR",
                                    "fromAmount": 200,
                                    "toAmount": 60000,
                                    "timestamp": "2024-07-14"
                                }
                            }
                        }
                    }
                }
            },
            "requestBody": {
                "application/json": {
                    "requestExample01": {
                        "filePath": "example.json"
                    },
                    "requestExample02": {
                        "value": {
                            "fromCurrancy": "LKR",
                            "toCurrancy": "USD" //comment
                        }
                    }
                    // "requestExample03": {
                    //     "filePath": "example.json"
                    // },
                }
            }

        }
    }
    resource function post rate(record {} payload) returns record {}|xml? {
        return {};
    }
}
