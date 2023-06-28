public type Mailing_AddContact_body record {
    string name?;
};

public type Mailing_AddContact_body_1 record {
    string name?;
};

public type Mailing_AddContact_body_2 record {|
    string name?;
    int...;
|};

public type MailingViewModel record {|
    string? name?;
    string? email?;
    boolean optPhoneNumber?;
    string? phoneNumber?;
    string? motivoRecusa?;
|};

public type Mailing_AddContact_body_3 record {|
    string name?;
    MailingViewModel...;
|};

public type HistoricoSimulacaoViewModel record {|
    string? email?;
    string? data?;
    string? step?;
    string? descricao?;
|};
