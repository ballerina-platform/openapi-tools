import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service /payloadV on ep0 {
    resource function post pet() returns http:Continue|http:SwitchingProtocols|http:Ok|http:Created|http:Accepted|
    http:NonAuthoritativeInformation|http:NoContent|http:PartialContent|http:MultipleChoices|http:MovedPermanently|
    http:Found|http:SeeOther|http:NotModified|http:UseProxy|http:TemporaryRedirect|http:PermanentRedirect|http:BadRequest|
    http:Unauthorized|http:PaymentRequired|http:Forbidden|http:NotFound|http:MethodNotAllowed|http:NotAcceptable|
    http:ProxyAuthenticationRequired|http:Conflict|http:Gone|http:LengthRequired|http:PreconditionFailed|
    http:PayloadTooLarge|http:UriTooLong|http:UnsupportedMediaType|http:RangeNotSatisfiable|http:ExpectationFailed|
    http:UpgradeRequired|http:TooManyRequests|http:RequestHeaderFieldsTooLarge|http:InternalServerError|
    http:NotImplemented|http:BadGateway|http:ServiceUnavailable|http:HttpVersionNotSupported {
    }
}
