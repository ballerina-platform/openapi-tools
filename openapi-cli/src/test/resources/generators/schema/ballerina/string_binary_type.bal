public type CreateTranscriptionResponse record {|
    string text;
|};

public type CreateTranscriptionRequest record {|
    # The audio file to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    record {byte[] fileContent; string fileName;} file;
    # ID of the model to use. Only `whisper-1` is currently available.
    string model;
    # An optional text to guide the model's style or continue a previous audio segment. The [prompt](/docs/guides/speech-to-text/prompting) should match the audio language.
    string prompt?;
    # The format of the transcript output, in one of these options: json, text, srt, verbose_json, or vtt.
    string response_format = "json";
    # The sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic. If set to 0, the model will use [log probability](https://en.wikipedia.org/wiki/Log_probability) to automatically increase the temperature until certain thresholds are hit.
    decimal temperature = 0;
    # The language of the input audio. Supplying the input language in [ISO-639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) format will improve accuracy and latency.
    string language?;
|};
