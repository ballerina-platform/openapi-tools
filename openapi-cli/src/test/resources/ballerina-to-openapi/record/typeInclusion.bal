import  ballerina/http;

type Link record {|
   string rel;
   string href;
   string[] mediaTypes?;
|};

type Links record {|
   Link[] links;
   int linkid;
|};

type ReservationReceipt record {|
   *Links;
   string id;
|};

listener  http:Listener  ep0  = new (443, config  = {host: "petstore.swagger.io"});

 service  /v2  on  ep0  {
        resource  function  post  pet(@http:Payload ReservationReceipt payload) {
    }
}
