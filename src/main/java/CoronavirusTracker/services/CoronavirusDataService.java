package CoronavirusTracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import CoronavirusTracker.models.LocationStats;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CoronavirusDataService {

    //attributes
    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private List<LocationStats> allStats = new ArrayList<>();
    private String lastDate;

    //methods
    @Scheduled(cron = "0 59 23 * * *")
    @PostConstruct
    public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>();

        // creating client, request and handling response
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());

        // getting headers to check last date
        CSVParser dataCSV = new CSVParser(csvBodyReader, CSVFormat.DEFAULT.withHeader());
        List<String> headers = dataCSV.getHeaderNames();
        this.lastDate = headers.get(headers.size() - 1);

        // getting and iterating all records
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        System.out.println("Data retrieved");
        for (CSVRecord record : records) {
            LocationStats locationStat = new LocationStats();

            locationStat.setState(record.get(0));
            locationStat.setCountry(record.get(1));
            locationStat.setLatestTotalCases(Integer.parseInt(record.get(record.size() - 1)));
            newStats.add(locationStat);
        }
        this.setAllStats(newStats);
    }

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    public void setAllStats(List<LocationStats> allStats) {
        this.allStats = allStats;
    }

    public String getLastDate() {
        return this.lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

}