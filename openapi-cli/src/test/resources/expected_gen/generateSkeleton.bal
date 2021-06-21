
import  ballerina/http;

listener  http:Listener  ep0  = new (80, config  = {host: "petstore.openapi.io"});

 service  /v1  on  ep0  {
        resource  function  get  pets(int?  'limit)  returns  Pets|http:Response {
    }
        resource  function  post  pets()  returns  http:Created|http:Response {
    }
        resource  function  get  pets/[string  petId]()  returns  Pets|http:Response {
    }
}
