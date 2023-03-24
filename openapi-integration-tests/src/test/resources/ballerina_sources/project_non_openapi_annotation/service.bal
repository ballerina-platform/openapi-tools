import ballerina/http;

# A service representing a network-accessible API
# bound to port `9090`.
@display {
	label: "SampleRest2",
	id: "SampleRest2-a0419d61-26b8-48ad-8696-3a16c9958522"
}
service / on new http:Listener(9090) {

    # A resource for generating greetings
    # + name - the input string name
    # + return - string name with hello message or error
    resource function get greeting(string name) returns string|error {
        // Send a response back to the caller.
        if name is "" {
            return error("name should not be empty!");
        }
        return "Hello, " + name;
    }
}
