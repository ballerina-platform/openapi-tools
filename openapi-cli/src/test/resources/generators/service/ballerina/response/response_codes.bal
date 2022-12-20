import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service /payloadV on ep0 {
    resource function post pet() returns http:UpgradeRequired|http:PaymentRequired|http:EarlyHints|http:UriTooLong|http:TemporaryRedirect|http:PreconditionFailed|http:UnsupportedMediaType|http:LoopDetected|http:LengthRequired|http:BadGateway|http:ServiceUnavailable|http:Created|http:NotModified|http:AlreadyReported|http:NoContent|http:ProxyAuthenticationRequired|http:Accepted|http:MultiStatus|http:Processing|http:UseProxy|http:NotExtended|http:PreconditionRequired|http:Found|http:MethodNotAllowed|http:PayloadTooLarge|http:Ok|http:IMUsed|http:SeeOther|http:Locked|http:MovedPermanently|http:Gone|http:UnprocessableEntity|http:VariantAlsoNegotiates|http:Forbidden|http:NotAcceptable|http:HttpVersionNotSupported|http:MultipleChoices|http:NotFound|http:FailedDependency|http:MisdirectedRequest|http:Continue|http:InternalServerError|http:NonAuthoritativeInformation|http:PermanentRedirect|http:RangeNotSatisfiable|http:NetworkAuthorizationRequired|http:Conflict|http:NotImplemented|http:SwitchingProtocols|http:BadRequest|http:Unauthorized|http:RequestHeaderFieldsTooLarge|http:PartialContent|http:TooEarly|http:TooManyRequests|http:ExpectationFailed|http:UnavailableDueToLegalReasons|http:InsufficientStorage {
    }
}
