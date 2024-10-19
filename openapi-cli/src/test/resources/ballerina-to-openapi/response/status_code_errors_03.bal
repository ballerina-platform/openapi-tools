import ballerina/http;
import ballerina/http.httpscerr;

type Error distinct error;

type DefaultErrorInfo record {|
    string timeStamp;
    string message;
|};

type DefaultErrorDetails record {|
    *httpscerr:ErrorDetail;
    DefaultErrorInfo body;
|};

type UserApiErrorInfo record {|
    *DefaultErrorInfo;
    string userId;
|};

type UserApiErrorDetails record {|
    *httpscerr:ErrorDetail;
    UserApiErrorInfo body;
|};

type UserNotFoundError Error & httpscerr:NotFoundError & error<UserApiErrorDetails>;

type UserNameAlreadyExistError Error & httpscerr:ConflictError & error<UserApiErrorDetails>;

type BadUserError Error & httpscerr:BadRequestError & error<UserApiErrorDetails>;

type DefaultError Error & httpscerr:DefaultStatusCodeError & error<DefaultErrorDetails>;

type User record {|
    readonly int id;
    *UserWithoutId;
|};

type UserWithoutId record {|
    string name;
    int age;
|};

service /payloadV on new http:Listener(9000) {

    resource function get users/[int id]() returns User|UserNotFoundError? {
        return;
    }

    resource function post users(@http:Payload readonly & UserWithoutId user)
            returns User|UserNameAlreadyExistError|BadUserError? {
        return;
    }

    resource function get test1() returns http:Response|httpscerr:DefaultStatusCodeError? {
        return;
    }

    resource function get test2() returns http:Response|DefaultError? {
        return;
    }

    resource function 'default [string... path]() returns httpscerr:NotFoundError? {
    }
}
