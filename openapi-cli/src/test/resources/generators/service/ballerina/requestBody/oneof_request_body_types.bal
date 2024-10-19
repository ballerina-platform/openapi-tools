public type pet_body Dog|Cat;

public type Bird record {
    string name?;
    boolean isFly?;
};

public type Cat record {
    string name?;
    string kind?;
};

public type pet_name_body (string|int|decimal)[];

public type pet_body_1 Dog|Bird;

public type Dog record {
    string name?;
    string age?;
};

public type pet02_body string|int|decimal;
