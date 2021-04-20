public type Pet record  {
    int  id;
    string  name;
    string  tag?;
};

public type Dog record  {
    Pet  pets?;
    boolean  bark;
};