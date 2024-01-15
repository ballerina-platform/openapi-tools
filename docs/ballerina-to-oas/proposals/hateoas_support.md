# Proposal: Map Ballerina HATEOAS Links to OpenAPI Specification

_Owners_: [@SachinAkash01](https://github.com/SachinAkash01)  
_Reviewers_: [@lnash94](https://github.com/lnash94) [@TharmiganK](https://github.com/TharmiganK)    
_Created_: 2023/10/25  
_Updated_: 2023/10/31  
_Issue_: [#4788](https://github.com/ballerina-platform/ballerina-library/issues/4788)

## Summary
This proposal is to introduce the capabilities to support HATEOAS (Hypermedia As The Engine Of Application State) links 
in the generated OpenAPI specification of the Ballerina OpenAPI tool. Note that in the rest of the proposal HATEOAS is 
referred to as Hypermedia constraint.

## Goals
- Enhance the Ballerina OpenAPI tool by adding support for Hypermedia constraints in the generated OpenAPI specification.

## Motivation
OpenAPI is a widely used standard for documenting APIs. It allows developers to specify the structure and data types 
expected in API requests and responses.

Hypermedia constraints are one of the key principles in REST though it is often ignored. This principle emphasizes the 
interconnection of resources, offering API users an experience similar to navigating the web. The existing tool for 
generating OpenAPI specification from Ballerina code lacks support for including these Hypermedia constraints in the 
OpenAPI specification. This means that when developers use Hypermedia constraints in their Ballerina code, this 
information is not properly reflected in the OpenAPI specification.

Overall, improving the OpenAPI tool to support Hypermedia constraints of the Ballerina programming language in the 
generated OpenAPI specification is a worthwhile investment that will improve the overall developer experience.

## Description
There are two ways of generating links between the resources in Ballerina, manually defining links between resources and
automatically generating links between resources.  The feature implementation focuses on integrating Ballerina 
Hypermedia constraints into generated OpenAPI specifications to accurately document the API.  In this proposal we will 
be addressing the automatic linking between the resources using `ResourceConfig` annotation in Ballerina.

Currently in manual linking approach, we are generating the response component schema by including `*http:Links`. 
There will be no changes to this behaviour of generating the OpenAPI specification.

**Sample Ballerina Code (Manually defining links between resources):**
```ballerina
import ballerina/http;

public type Link record {
    string rel?;
    string href;
    string types?;
    string methods?;
};

public type Location record {|
    *http:Links;
    string name;
    string id;
    string address;
|};

service /snowpeak on new http:Listener(9090) {
    resource function get locations() returns Location {
        return {
            name: "Alps",
            id: "l100",
            address: "Switzerland",
            _links: {
                room: {
                    href: "/snowpeak/locations/{id}/rooms",
                    methods: ["GET"]
                }
            }
        };
    }
}
```

**OpenAPI Specification (response component schema including the `*http:Links`):**
```yaml
Location:
  - required:
    - address
    - id
    - name
    type: object
    properties:
      name:
        type: string
        description: Name of the location
      id:
        type: string
        description: Unique identification
      address:
        type: string
        description: Address of the location
      links: 
        type: array
        items: 
          type: object
          properties:
            rel: 
              type: string
              default: "room"
            href: 
              type: string
              default: "/snowpeak/locations/{id}/rooms"
            method: 
              type: string
              default: "GET"
```

Following is the proposed way of reflecting Ballerina Hypermedia constraints using `ResourceConfig` annotation into 
OpenAPI specification.

**Sample Ballerina Code:**
```ballerina
service /snowpeak on new http:Listener(port) {

    @http:ResourceConfig {
        name: "Locations",
        linkedTo: [ {name: "Rooms", relation: "room", method: "get"} ]
    }
    resource function get locations() returns @http:Cache rep:Locations|rep:SnowpeakInternalError {
       // some logic
    }

    @http:ResourceConfig {
        name: "Rooms"
    }
    resource function get locations/[string id]/rooms(string startDate, string endDate) 
                returns rep:Rooms|rep:SnowpeakInternalError {
        // some logic
    }
}
```

**Generated OpenAPI Specification:**
```yaml
paths:
  /locations:
    get:
      operationId: getLocations
      responses:
        "200":
          description: ok
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Location'
          links:
            room:
              operationId: getLocationsIdRooms
  /locations/{id}/rooms:
    get:
      operationId: getLocationsIdRooms
      parameters:
```

> [!NOTE]  
> Since OpenAPI doesn't support HATEOAS directly, there is no proper and direct way to reflect resource name 
`@http:ResourceConfig { name: "Locations"}` in OpenAPI specification. This might be a problem when we generate 
Ballerina service stub from OAS. 

## Testing
- Unit Testing: Evaluate the individual components and core logic of the Ballerina OpenAPI tool, focusing on functions, 
methods, and modules to ensure correctness and Hypermedia constraint handling.
- Integration Testing: Assess the interaction and collaborations between various modules and components of the tool, 
verifying the seamless integration of Hypermedia constraints into the OpenAPI specification.
