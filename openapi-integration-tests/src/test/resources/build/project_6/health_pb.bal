import ballerina/grpc;
import ballerina/protobuf.types.wrappers;

public isolated client class HealthServerClient {
    *grpc:AbstractClientEndpoint;

    private final grpc:Client grpcClient;

    public isolated function init(string url, *grpc:ClientConfiguration config) returns grpc:Error? {
        self.grpcClient = check new (url, config);
        check self.grpcClient.initStub(self, ROOT_DESCRIPTOR_HEALTH, getDescriptorMapHealth());
    }

    isolated remote function registerPatient(Patient|ContextPatient req) returns boolean|grpc:Error {
        map<string|string[]> headers = {};
        Patient message;
        if req is ContextPatient {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("HealthServer/registerPatient", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <boolean>result;
    }

    isolated remote function registerPatientContext(Patient|ContextPatient req) returns wrappers:ContextBoolean|grpc:Error {
        map<string|string[]> headers = {};
        Patient message;
        if req is ContextPatient {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("HealthServer/registerPatient", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <boolean>result, headers: respHeaders};
    }

    isolated remote function getPatientInfo(string|wrappers:ContextString req) returns Patient|grpc:Error {
        map<string|string[]> headers = {};
        string message;
        if req is wrappers:ContextString {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("HealthServer/getPatientInfo", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <Patient>result;
    }

    isolated remote function getPatientInfoContext(string|wrappers:ContextString req) returns ContextPatient|grpc:Error {
        map<string|string[]> headers = {};
        string message;
        if req is wrappers:ContextString {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("HealthServer/getPatientInfo", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <Patient>result, headers: respHeaders};
    }
}

public client class HealthServerPatientCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendPatient(Patient response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextPatient(ContextPatient response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class HealthServerBooleanCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendBoolean(boolean response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextBoolean(wrappers:ContextBoolean response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public type ContextPatient record {|
    Patient content;
    map<string|string[]> headers;
|};

public type Patient record {|
    string id = "";
    string name = "";
    Patient_Gender gender = MALE;
    string disease = "";
|};

public enum Patient_Gender {
    MALE,
    FEMALE,
    OTHER
}

const string ROOT_DESCRIPTOR_HEALTH = "0A0C6865616C74682E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F229B010A0750617469656E74120E0A0269641801200128095202696412120A046E616D6518022001280952046E616D6512270A0667656E64657218032001280E320F2E50617469656E742E47656E646572520667656E64657212180A076469736561736518042001280952076469736561736522290A0647656E64657212080A044D414C451000120A0A0646454D414C45100112090A054F5448455210023281010A0C4865616C746853657276657212370A0F726567697374657250617469656E7412082E50617469656E741A1A2E676F6F676C652E70726F746F6275662E426F6F6C56616C756512380A0E67657450617469656E74496E666F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A082E50617469656E74620670726F746F33";

public isolated function getDescriptorMapHealth() returns map<string> {
    return {"google/protobuf/wrappers.proto": "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F62756622230A0B446F75626C6556616C756512140A0576616C7565180120012801520576616C756522220A0A466C6F617456616C756512140A0576616C7565180120012802520576616C756522220A0A496E74363456616C756512140A0576616C7565180120012803520576616C756522230A0B55496E74363456616C756512140A0576616C7565180120012804520576616C756522220A0A496E74333256616C756512140A0576616C7565180120012805520576616C756522230A0B55496E74333256616C756512140A0576616C756518012001280D520576616C756522210A09426F6F6C56616C756512140A0576616C7565180120012808520576616C756522230A0B537472696E6756616C756512140A0576616C7565180120012809520576616C756522220A0A427974657356616C756512140A0576616C756518012001280C520576616C756542570A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A057479706573F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33", "health.proto": "0A0C6865616C74682E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F229B010A0750617469656E74120E0A0269641801200128095202696412120A046E616D6518022001280952046E616D6512270A0667656E64657218032001280E320F2E50617469656E742E47656E646572520667656E64657212180A076469736561736518042001280952076469736561736522290A0647656E64657212080A044D414C451000120A0A0646454D414C45100112090A054F5448455210023281010A0C4865616C746853657276657212370A0F726567697374657250617469656E7412082E50617469656E741A1A2E676F6F676C652E70726F746F6275662E426F6F6C56616C756512380A0E67657450617469656E74496E666F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A082E50617469656E74620670726F746F33"};
}
