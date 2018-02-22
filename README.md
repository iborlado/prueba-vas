# prueba-vas
I created a Spring Boot project called prueba-vas.
The service has three HTTP endpoints:

* GET /prueba-vas/{date}
  - Date is the name of the file to process (YYYYMMDD).
  - This endpoint returns content of file with json format.
  - If there are errors in formatting, the service returns blank body and status "400 Bad Request".

* GET /prueba-vas/metrics/{date}
  - Date is the name of the file to calculate metrics.
  - If there are lines with missing fields, the service will only increase nRowsMissingFields counter (not nRowsFieldsErrors).
  - For word ranking: the service counts lines with correct format (json) and message_type = "MSG".
  - Only checks content of status_code and message-status for nRowsFieldsErrors counter.
  - Service doesn´t ckeck order of the fields.
  - nRowsFieldsErrors = calls with error + messages with error + message_type <> "CALL" or "MSG" + incorrect json format.
  - The service calculates number or average call duration by country.
  - e.g. http://localhost:8080/prueba-vas/metrics/20180131
{
    "nRowsMissingFields": 0,
    "nMessagesBlankContent": 4,
    "nRowsFieldsErrors": 7,
    "nCallsByCountry": null,
    "relationshipOkKoCalls": {
        "KO": 5,
        "OK": 17
    },
    "averageCallByCountry": null,
    "wordOcurrenceRanking": {
        "HELLO": 12,
        "ARE": 1,
        "FINE": 1,
        "YOU": 1,
        "NOT": 0
    }
}

* GET /prueba-vas/kpis/{dates}
  - Dates are the name of the files to calculate kpis. Filenames must be concatenated with "-" character.
  - Duration of json process is the different between "before" and "after" call to method that process the file (expressed in milliseconds).
  - The service is still not calculate number of different origins and different destinations by country.
  - e.g. http://localhost:8080/prueba-vas/kpis/20180201-20180202
{
    "nProcessedFiles": 2,
    "nRows": 16,
    "nCalls": 7,
    "nMessages": 5,
    "nDifferentOrigin": 0,
    "nDifferentDestination": 0,
    "durationJsonProcess": {
        "MCP_20180201.json": 273,
        "MCP_20180202.json": 134
    }
}
