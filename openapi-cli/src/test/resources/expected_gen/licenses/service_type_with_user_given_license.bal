// Copyright (c) 2021 All Rights Reserved.

import ballerina/http;

type OASServiceType service object {
    *http:Service;
    resource function get pets(int? 'limit) returns Pets|http:Response;
    resource function post pets() returns http:Created|http:Response;
    resource function get pets/[string petId]() returns Pets|http:Response;
};
