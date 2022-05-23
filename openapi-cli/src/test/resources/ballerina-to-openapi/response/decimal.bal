import ballerina/http;

service /payloadV on new http:Listener(9090) {
    resource function get fareCalculator(string flightNo, int noOfPassengers) returns decimal {
        return 1.0;
    }
}
