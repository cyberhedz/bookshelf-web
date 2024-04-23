package cloudcode.helloworld.web;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.bigquery.FieldValueList;


@RestController
public final class HelloWorldController {


  /**
   * Create an endpoint for the landing page
   * @return the BigQuery analytics results string to the web
   */


  @GetMapping("/")
  public String helloWorld() throws Exception {
    /* Connect to bigquery and write a select SQL to fetch Title, Theme and Summary fields from the table `bookshelf.bookshelf_theme` if you have executed the codelab 1 of this series, if not just directly use records from gdelt-bq.internetarchivebooks.1920 table */

 
String query = "SELECT  BookMeta_Title || ' (' || Themes || ') ' as summary  from gdelt-bq.internetarchivebooks.1920 limit 10 ";


    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
    QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder(query)
            .setUseLegacySql(false)
            .build();
    // Create a job ID so that we can safely retry.
    JobId jobId = JobId.of(UUID.randomUUID().toString());
    Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());
    // Wait for the query to complete.
    queryJob = queryJob.waitFor();
    // Check for errors
    if (queryJob == null) {
      throw new RuntimeException("Job no longer exists");
    } else if (queryJob.getStatus().getError() != null) {
      throw new RuntimeException(queryJob.getStatus().getError().toString());
    }
    // Get the results.
    TableResult result = queryJob.getQueryResults();
    String responseString = "";
    // Print all pages of the results.
    for (FieldValueList row : result.iterateAll()) {
      responseString += row.get("summary").getStringValue() + ".         \n";
      System.out.printf("%s\n", row.get("summary").getStringValue());
    }
    return responseString;
  }
}