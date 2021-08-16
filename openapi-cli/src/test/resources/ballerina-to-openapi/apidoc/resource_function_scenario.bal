import ballerina/http;

service /payloadV on new http:Listener(9090) {
    # Represents Snowpeak location resource
    resource function get locations() {
    }
}
