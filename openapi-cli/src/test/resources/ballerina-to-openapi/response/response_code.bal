import ballerina/http;

service /payloadV on new http:Listener(9090) {
    resource function post pet() returns http:Continue|http:SwitchingProtocols|http:Ok|http:Created|http:Accepted|http:NonAuthoritativeInformation|http:NoContent|
    http:ResetContent|http:PartialContent|http:MultipleChoices|http:MovedPermanently|http:Found|http:SeeOther|http:NotModified|http:UseProxy|http:TemporaryRedirect|
    http:PermanentRedirect|http:BadRequest|http:Unauthorized|http:PaymentRequired|http:Forbidden|http:NotFound|http:MethodNotAllowed|http:NotAcceptable|
    http:ProxyAuthenticationRequired|http:RequestTimeout|http:Conflict|http:Gone|http:LengthRequired|http:PreconditionFailed|http:PayloadTooLarge|
    http:UriTooLong|http:UnsupportedMediaType|http:RangeNotSatisfiable|http:ExpectationFailed|http:UpgradeRequired|http:TooManyRequests|
    http:RequestHeaderFieldsTooLarge|http:InternalServerError|http:NotImplemented|http:BadGateway|http:ServiceUnavailable|http:GatewayTimeout|
    http:HttpVersionNotSupported {
        return <http:Accepted> {};
    }
}