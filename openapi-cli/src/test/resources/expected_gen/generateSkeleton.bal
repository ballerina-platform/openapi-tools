service openapipetstore on ep0, ep1 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/pets"
    }
    resource function listPets (http:Caller caller, http:Request req) returns error? {

    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/pets"
    }
    resource function resource_post_pets (http:Caller caller, http:Request req) returns error? {

    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/pets/{petId}"
    }
    resource function showPetById (http:Caller caller, http:Request req,  string petId) returns error? {

    }

}