import  ballerina/http;

# Link record
type Link record {|
   # link rel
   string rel;
   # link href
   string href;
   #link mediatype
   string[] mediaTypes?;
|};

# Links array
type Links record {|
   # Array links
   Link[] links;
   # link id
   int linkid;
|};

# ReservationReceipt details
type ReservationReceipt record {|
   *Links;
   # Reservation receipt id
   string id;
|};

listener  http:Listener  ep0  = new (443, config  = {host: "petstore.swagger.io"});

 # Description
 service  /payloadV  on  ep0  {
        resource  function  post  pet(@http:Payload ReservationReceipt payload) {
    }
}
