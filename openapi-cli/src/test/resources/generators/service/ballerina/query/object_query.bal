import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "virtserver.swaggerhub.com"});

service /lnash94/QueryParam/'1\.0\.0 on ep0 {
    # searches inventory
    #
    # + required_query - 1. required query param with inline object
    # + optional_query - 2. optional query param with inline object
    # + default_query - 3. default query param with inline object
    # + reference_query - 4. reference query param
    # + required_nullable - 5. required query param with inline object and nullable true
    # + optional_nullable - 6. optional query param with inline object
    # + default_nullable - 7. default query param with inline object
    # + reference_nullable - 8. reference_nullable query param won't consider nullable details when it has reference in swagger
    # + return - returns can be any of following types
    # http:Ok (search results matching criteria)
    # http:BadRequest (bad input parameter)
    resource function get inventory(Required_query required_query, Optional_query? optional_query, Manufacturer? reference_query, Required_nullable? required_nullable, Optional_nullable? optional_nullable, Manufacturer? reference_nullable, Default_query default_query = {"limit":"9","id":9}, Default_nullable default_nullable = {"limit":"9","id":9}) returns http:Ok|http:BadRequest {
    }
    # Update inventory
    #
    # + primitive - 14. object query parameter with additional property with primitive value
    # + return - Created
    resource function put inventory(record{|PrimitiveValues...;|}? primitive) returns http:Created {
    }
    # adds an inventory item
    #
    # + required_query - 9. required query param with inline object including additional properties with reference.
    # + optional_query - 10. optional query param with inline object including additional properties integer value.
    # + object_nullable - 11. nullable required query param with inline object including additional properties
    # + skip_nullable - 12. nullable optional query param with inline object including additional properties
    # + add_false - 13. nullable optional query param with inline object including additional properties.
    # + payload - Inventory item to add
    # + return - item created
    resource function post inventory(record{|InventoryItem...;|} required_query, record{|int...;|}? optional_query, record{|InventoryItem...;|}? object_nullable, record{|int...;|}? skip_nullable, Add_false? add_false, @http:Payload InventoryItem payload) returns http:Created {
    }
    # Update inventory
    #
    # + arrayRecord - 15. Query parameter with object array
    # + return - Created
    resource function delete inventory(InventoryItem[]? arrayRecord) returns http:Created {
    }
    # Update inventory
    #
    # + additionalArray - 16. Query parameter with record opens with record array
    # + return - Created
    resource function put inventory02(record{|InventoryItem[]...;|}? additionalArray) returns http:Created {
    }
}
