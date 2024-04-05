import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    # Description
    #
    # + payload - parameter description
    # + return - Success
    resource function post api/HistoricoSimulacao/AddHistorico(@http:Payload ApiHistoricosimulacaoAddhistoricoRequest|ApiHistoricosimulacaoAddhistoricoRequest_1|ApiHistoricosimulacaoAddhistoricoRequest_2 payload) returns http:Ok {
    }
    # Description
    #
    # + payload - parameter description
    # + return - Success
    resource function put api/Mailing/AddContact(@http:Payload Mailing_AddContact_body|Mailing_AddContact_body_1 payload) returns http:Ok {
    }
    # Description
    #
    # + payload - parameter description
    # + return - Success
    resource function post api/Mailing/AddContact(@http:Payload Mailing_AddContact_body_2|Mailing_AddContact_body_3 payload) returns http:Ok {
    }
}
