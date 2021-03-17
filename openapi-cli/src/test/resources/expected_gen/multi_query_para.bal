import  ballerina/http;

listener  http:Listener  ep0  = new (9090, config  = {host: localhost});

 service  /api  on  ep0  {
        resource  function  get  pets(string[]?  tags, int?  'limit)  returns  Pet[]|Error {
    }
}
