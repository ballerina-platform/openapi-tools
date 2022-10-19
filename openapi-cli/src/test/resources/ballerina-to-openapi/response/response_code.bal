import ballerina/http;

service /payloadV on new http:Listener(9090) {
    resource function post pet() returns http:Continue|http:SwitchingProtocols|http:Processing|http:EarlyHints|http:Ok|http:Created|http:Accepted|http:NonAuthoritativeInformation|http:NoContent|http:PartialContent|http:MultiStatus|http:AlreadyReported|http:IMUsed|http:MultipleChoices|http:MovedPermanently|http:Found|http:SeeOther|http:NotModified|http:UseProxy|http:TemporaryRedirect|http:PermanentRedirect|http:BadRequest|http:Unauthorized|http:PaymentRequired|http:Forbidden|http:NotFound|http:MethodNotAllowed|http:NotAcceptable|http:ProxyAuthenticationRequired|http:Conflict|http:Gone|http:LengthRequired|http:PreconditionFailed|http:PayloadTooLarge|http:UriTooLong|http:UnsupportedMediaType|http:RangeNotSatisfiable|http:ExpectationFailed|http:MisdirectedRequest|http:UnprocessableEntity|http:Locked|http:FailedDependency|http:TooEarly|http:UpgradeRequired|http:PreconditionRequired|http:TooManyRequests|http:RequestHeaderFieldsTooLarge|http:UnavailableDueToLegalReasons|http:InternalServerError|http:NotImplemented|http:BadGateway|http:ServiceUnavailable|http:HttpVersionNotSupported|http:VariantAlsoNegotiates|http:InsufficientStorage|http:LoopDetected|http:NotExtended|http:NetworkAuthorizationRequired {
        return <http:Continue> {};
    }
}
