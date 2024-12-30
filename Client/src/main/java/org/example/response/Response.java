package org.example.response;

import java.io.Serializable;
import java.util.List;

public class Response implements Serializable {
    ResponseType responseType;
    List<CountryRank> partialCountryResults;
    FileWrapper finalParticipantsRankingFile;
    FileWrapper finalCountriesRankingFile;

    public Response(
            ResponseType responseType,
            List<CountryRank> partialCountryResults,
            FileWrapper finalParticipantsRankingFile,
            FileWrapper finalCountriesRankingFile
    ) {
        this.responseType = responseType;
        this.partialCountryResults = partialCountryResults;
        this.finalParticipantsRankingFile = finalParticipantsRankingFile;
        this.finalCountriesRankingFile = finalCountriesRankingFile;
    }

    public List<CountryRank> getPartialCountryResults() {
        return partialCountryResults;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public FileWrapper getFinalParticipantsRankingFile() {
        return finalParticipantsRankingFile;
    }

    public FileWrapper getFinalCountriesRankingFile() {
        return finalCountriesRankingFile;
    }
}
