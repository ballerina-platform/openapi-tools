import ballerina/http;
import ballerina/openapi;

@openapi:ServiceInfo {
    title: "Mock file",
    description: "API system description",
    email: "sumudu@abc.com",
    contactName: "sumudu",
    contactURL: "http://mock-api-contact",
    termsOfService: "http://mock-api-doc",
    licenseName: "ABC",
    licenseURL: "http://abc.com"
}
service /titleBase on new http:Listener(9090) {
    resource function get title() returns string {
        return "Hello, World!";
    }
}
