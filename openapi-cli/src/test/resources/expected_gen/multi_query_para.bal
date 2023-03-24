import  ballerina/http;

listener  http:Listener  ep0 = new (80, config = {host: "petstore.swagger.io"});

 service  /api  on  ep0  {
        resource  function  get  pets(string[]?  tags, int?  'limit)  returns  Pet[]|http:Response {
    }
}
