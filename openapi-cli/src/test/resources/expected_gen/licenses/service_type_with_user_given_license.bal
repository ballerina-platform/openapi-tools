// Copyright (c) 2023 All Rights Reserved.

import ballerina/http;

type OASServiceType service object {
    *http:Service;
    resource function get pets(int? 'limit) returns Pets|ErrorDefault;
    resource function post pets() returns http:Created|ErrorDefault;
    resource function get pets/[string petId]() returns Dog|ErrorDefault;
};
