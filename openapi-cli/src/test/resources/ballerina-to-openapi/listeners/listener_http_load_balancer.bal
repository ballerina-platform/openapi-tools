import ballerina/http;

// Create an endpoint with port 8080 for the mock backend services.
listener http:Listener backendEP = check new(8080);

// Define the mock backend services, which are called by the load balancer.
service /payloadV on backendEP {
    resource function get .() returns string {
        return "Mock1 resource was invoked.";
    }
}
