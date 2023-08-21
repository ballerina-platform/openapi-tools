public type VaccineCountryCoverage record {
    string country?;
    # One of
    SimpleVaccineTimeline|FullVaccineTimeline timeline?;
};

# Covid-19 Vaccine timeline briefly
public type SimpleVaccineTimeline record {
    decimal date?;
};

# Descriptive Covid-19 vaccine timeline
#
# + fullvaccinetimelinelist - Descriptive Covid-19 vaccine timeline
public type FullVaccineTimeline record {
    record  {| decimal total?; decimal daily?; decimal totalPerHundred?; decimal dailyPerMillion?; string date?;|} []
    fullvaccinetimelinelist;
};
