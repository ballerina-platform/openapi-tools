import ballerina/http;
import ballerina/http.httpscerr;

function getStatusCodeError(int statusCode) returns
        httpscerr:BadRequestError|httpscerr:UnauthorizedError|httpscerr:PaymentRequiredError|
        httpscerr:ForbiddenError|httpscerr:NotFoundError|httpscerr:MethodNotAllowedError|
        httpscerr:NotAcceptableError|httpscerr:ProxyAuthenticationRequiredError|httpscerr:RequestTimeoutError|
        httpscerr:ConflictError|httpscerr:GoneError|httpscerr:LengthRequiredError|httpscerr:PreconditionFailedError|
        httpscerr:PayloadTooLargeError|httpscerr:URITooLongError|httpscerr:UnsupportedMediaTypeError|
        httpscerr:RangeNotSatisfiableError|httpscerr:ExpectationFailedError|httpscerr:MisdirectedRequestError|
        httpscerr:UnprocessableEntityError|httpscerr:LockedError|httpscerr:FailedDependencyError|
        httpscerr:UpgradeRequiredError|httpscerr:PreconditionRequiredError|httpscerr:TooManyRequestsError|
        httpscerr:RequestHeaderFieldsTooLargeError|httpscerr:UnavailableDueToLegalReasonsError|
        httpscerr:InternalServerErrorError|httpscerr:NotImplementedError|httpscerr:BadGatewayError|
        httpscerr:ServiceUnavailableError|httpscerr:GatewayTimeoutError|httpscerr:HTTPVersionNotSupportedError|
        httpscerr:VariantAlsoNegotiatesError|httpscerr:InsufficientStorageError|httpscerr:LoopDetectedError|
        httpscerr:NotExtendedError|httpscerr:NetworkAuthenticationRequiredError|httpscerr:DefaultStatusCodeError
{
    match statusCode {
        400 => {
            return error httpscerr:BadRequestError("Bad request error");
        }
        401 => {
            return error httpscerr:UnauthorizedError("Unauthorized error");
        }
        402 => {
            return error httpscerr:PaymentRequiredError("Payment required error");
        }
        403 => {
            return error httpscerr:ForbiddenError("Forbidden error");
        }
        404 => {
            return error httpscerr:NotFoundError("Not found error");
        }
        405 => {
            return error httpscerr:MethodNotAllowedError("Method not allowed error");
        }
        406 => {
            return error httpscerr:NotAcceptableError("Not acceptable error");
        }
        407 => {
            return error httpscerr:ProxyAuthenticationRequiredError("Proxy authentication required error");
        }
        408 => {
            return error httpscerr:RequestTimeoutError("Request timeout error");
        }
        409 => {
            return error httpscerr:ConflictError("Conflict error");
        }
        410 => {
            return error httpscerr:GoneError("Gone error");
        }
        411 => {
            return error httpscerr:LengthRequiredError("Length required error");
        }
        412 => {
            return error httpscerr:PreconditionFailedError("Precondition failed error");
        }
        413 => {
            return error httpscerr:PayloadTooLargeError("Payload too large error");
        }
        414 => {
            return error httpscerr:URITooLongError("URI too long error");
        }
        415 => {
            return error httpscerr:UnsupportedMediaTypeError("Unsupported media type error");
        }
        416 => {
            return error httpscerr:RangeNotSatisfiableError("Range not satisfiable error");
        }
        417 => {
            return error httpscerr:ExpectationFailedError("Expectation failed error");
        }
        421 => {
            return error httpscerr:MisdirectedRequestError("Misdirected request error");
        }
        422 => {
            return error httpscerr:UnprocessableEntityError("Unprocessable entity error");
        }
        423 => {
            return error httpscerr:LockedError("Locked error");
        }
        424 => {
            return error httpscerr:FailedDependencyError("Failed dependency error");
        }
        426 => {
            return error httpscerr:UpgradeRequiredError("Upgrade required error");
        }
        428 => {
            return error httpscerr:PreconditionRequiredError("Precondition required error");
        }
        429 => {
            return error httpscerr:TooManyRequestsError("Too many requests error");
        }
        431 => {
            return error httpscerr:RequestHeaderFieldsTooLargeError("Request header fields too large error");
        }
        451 => {
            return error httpscerr:UnavailableDueToLegalReasonsError("Unavailable for legal reasons error");
        }
        500 => {
            return error httpscerr:InternalServerErrorError("Internal server error error");
        }
        501 => {
            return error httpscerr:NotImplementedError("Not implemented error");
        }
        502 => {
            return error httpscerr:BadGatewayError("Bad gateway error");
        }
        503 => {
            return error httpscerr:ServiceUnavailableError("Service unavailable error");
        }
        504 => {
            return error httpscerr:GatewayTimeoutError("Gateway timeout error");
        }
        505 => {
            return error httpscerr:HTTPVersionNotSupportedError("HTTP version not supported error");
        }
        506 => {
            return error httpscerr:VariantAlsoNegotiatesError("Variant also negotiates error");
        }
        507 => {
            return error httpscerr:InsufficientStorageError("Insufficient storage error");
        }
        508 => {
            return error httpscerr:LoopDetectedError("Loop detected error");
        }
        510 => {
            return error httpscerr:NotExtendedError("Not extended error");
        }
        511 => {
            return error httpscerr:NetworkAuthenticationRequiredError("Network authentication required error");
        }
        _ => {
            return error httpscerr:DefaultStatusCodeError("Default error", statusCode = statusCode);
        }
    }
}

type CustomHeaders record {|
    string header1;
    string[] header2;
|};

service /payloadV on new http:Listener(9090) {

    resource function get statusCodeError(int statusCode) returns
        httpscerr:BadRequestError|httpscerr:UnauthorizedError|httpscerr:PaymentRequiredError|
        httpscerr:ForbiddenError|httpscerr:NotFoundError|httpscerr:MethodNotAllowedError|
        httpscerr:NotAcceptableError|httpscerr:ProxyAuthenticationRequiredError|httpscerr:RequestTimeoutError|
        httpscerr:ConflictError|httpscerr:GoneError|httpscerr:LengthRequiredError|httpscerr:PreconditionFailedError|
        httpscerr:PayloadTooLargeError|httpscerr:URITooLongError|httpscerr:UnsupportedMediaTypeError|
        httpscerr:RangeNotSatisfiableError|httpscerr:ExpectationFailedError|httpscerr:MisdirectedRequestError|
        httpscerr:UnprocessableEntityError|httpscerr:LockedError|httpscerr:FailedDependencyError|
        httpscerr:UpgradeRequiredError|httpscerr:PreconditionRequiredError|httpscerr:TooManyRequestsError|
        httpscerr:RequestHeaderFieldsTooLargeError|httpscerr:UnavailableDueToLegalReasonsError|
        httpscerr:InternalServerErrorError|httpscerr:NotImplementedError|httpscerr:BadGatewayError|
        httpscerr:ServiceUnavailableError|httpscerr:GatewayTimeoutError|httpscerr:HTTPVersionNotSupportedError|
        httpscerr:VariantAlsoNegotiatesError|httpscerr:InsufficientStorageError|httpscerr:LoopDetectedError|
        httpscerr:NotExtendedError|httpscerr:NetworkAuthenticationRequiredError|httpscerr:DefaultStatusCodeError
    {
        return getStatusCodeError(statusCode);
    }

    resource function post statusCodeError(@http:Payload anydata payload, int statusCode,
            @http:Header CustomHeaders headers) returns httpscerr:DefaultStatusCodeError|error|httpscerr:InternalServerErrorError {

        return error httpscerr:DefaultStatusCodeError("Default error", statusCode = statusCode,
            body = payload, headers = headers);
    }
}
