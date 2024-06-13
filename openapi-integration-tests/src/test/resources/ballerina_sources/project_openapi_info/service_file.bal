import ballerina/http;
import ballerina/openapi;

@openapi:ServiceInfo {
    version: "1.0.0",
    title: "Pet store",
    description: "API system description",
    email: "sumudu@abc.com",
    contactName: "sumudu",
    contactURL: "http://mock-api-contact",
    termsOfService: "http://mock-api-doc",
    licenseName: "ABC",
    licenseURL: "http://abc.com"
}
service /info on new http:Listener(9090) {
    resource function get pet() {

    }
}
