import ballerina/http;

listener  http:Listener ep = new (80, config  = {host: "petstore.openapi.io"});

 service  /v1  on ep {
        resource  function  get  pets(int?  'limit)  returns  Pets|http:Response {
    }
}
