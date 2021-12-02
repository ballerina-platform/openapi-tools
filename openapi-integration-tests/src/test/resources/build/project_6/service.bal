import ballerina/grpc;

listener grpc:Listener ep = new (9090);

@grpc:ServiceDescriptor {
    descriptor: "ROOT_DESCRIPTOR_GRPC_UNARY"
}
service "" on ep {
    remote function helloWorld(string value) returns string|error {
        return "Hello Client";
    }
}
