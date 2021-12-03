import ballerina/grpc;

listener grpc:Listener ep = new (9090);

@grpc:ServiceDescriptor {descriptor: ROOT_DESCRIPTOR_HEALTH, descMap: getDescriptorMapHealth()}
service "HealthServer" on ep {

    remote function registerPatient(Patient value) returns boolean|error? {
        return;
    }
    remote function getPatientInfo(string value) returns Patient|error? {
        return;
    }
}
