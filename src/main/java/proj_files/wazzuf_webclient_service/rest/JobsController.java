package proj_files.wazzuf_webclient_service.rest;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.count;
import static org.apache.spark.sql.functions.lower;
import static org.apache.spark.sql.functions.regexp_extract;
import static org.apache.spark.sql.functions.trim;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import proj_files.wazzuf_webclient_service.dao.JobDAO;
import proj_files.wazzuf_webclient_service.dao.impl.JobDAOImpl;
import proj_files.wazzuf_webclient_service.pojo.Job;
import proj_files.wazzuf_webclient_service.service.SparkServices;
import proj_files.wazzuf_webclient_service.service.impl.SparkServicesImpl;

@RestController
@RequestMapping("/jobs")
public class JobsController {
	@Autowired
	private ApplicationContext applicationContext;

	private SparkServices sparkServices;
	private JobDAO jobDAO;

	@PostConstruct
	private void init() {
		jobDAO = applicationContext.getBean(JobDAOImpl.class);
		sparkServices= applicationContext.getBean(SparkServicesImpl.class);
		System.out.println(jobDAO);
	}

	@GetMapping(path = "/title/{title}", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public List<Job> findTitle(@PathVariable String title) {
		Job j = new Job();
		j.setTitle(title);
		return jobDAO.find(j).collectAsList();
	}

	@GetMapping(path = "/country/{country}", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public List<Job> findCountry(@PathVariable String country) {
		Job j = new Job();
		j.setCountry(country);
		return jobDAO.find(j).collectAsList();
	}

	@GetMapping(path = "/", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public ResponseEntity<List<Job>> jobs() {

		return ResponseEntity.ok(jobDAO.findAll().collectAsList());
	}
	@GetMapping(path = "/head/{n}", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public ResponseEntity<List<Job>> jobsHead(@PathVariable int n) {
		return ResponseEntity.ok(jobDAO.findAll().collectAsList().subList(0, n));
	}
	@GetMapping(path = "/head", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public ResponseEntity<Job> jobsHead() {

		return ResponseEntity.ok(jobDAO.findAll().first());
	}

	@GetMapping(path = "/summary", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public ResponseEntity<List<JsonObject>> summary() {
		Dataset<Row> s = jobDAO.findAll().summary();
		return ResponseEntity.ok(rowToJson(s));
	}

	@GetMapping(path = "/structure", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public ResponseEntity<Object> structure() {
		return ResponseEntity.ok(jobDAO.findAll().schema().fields());
	}

	@GetMapping(path = "/describe", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public ResponseEntity<List<JsonObject>> describe() {
		Dataset<Row> s = jobDAO.findAll().describe();

		return ResponseEntity.ok(rowToJson(s));
	}

	@GetMapping(path = "/clean", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public ResponseEntity<List<Job>> clean() {
		return ResponseEntity.ok(jobDAO.findAll().dropDuplicates().distinct().collectAsList());
	}

	@GetMapping(path = "/jobs_per_company", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public ResponseEntity<List<JsonObject>> jobsPerCompany() {
		Dataset<Row> s = jobDAO.findAll().withColumn("company_norm", trim(lower(col("company")))).groupBy("company_norm").agg(count("company_norm").alias("count"))
				.sort(col("count").desc());
		return ResponseEntity.ok(rowToJson(s));
	}
	@GetMapping(path = "/jobs_popularity", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public ResponseEntity<List<JsonObject>> jobsPopularity() {
		Dataset<Row> s = jobDAO.findAll().withColumn("title_norm", trim(lower(col("title")))).groupBy("title_norm").agg(count("title_norm").alias("count"))
				.sort(col("count").desc());
		return ResponseEntity.ok(rowToJson(s));
	}
	@GetMapping(path = "/area_popularity", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public ResponseEntity<List<JsonObject>> areaPopularity() {
		Dataset<Row> s = jobDAO.findAll().withColumn("location_norm", trim(lower(col("location")))).groupBy("location_norm").agg(count("location_norm").alias("count"))
				.sort(col("count").desc());
		return ResponseEntity.ok(rowToJson(s));
	}
	@GetMapping(path = "/skills", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public ResponseEntity<List<JsonObject>> skills() {
		Dataset<Row> s = jobDAO.findAll().selectExpr("explode(split(Skills,',')) as skill").withColumn("skill", trim(lower(col("skill")))).groupBy("skill").count().alias("count").sort(col("count").desc());
		return ResponseEntity.ok(rowToJson(s));
	}
	@GetMapping(path = "/years_factorized", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public ResponseEntity<List<JsonObject>> years_fact() {
		Dataset<Job> j = jobDAO.findAll();
		Dataset<Row> s = j.toDF().withColumn("yearsExp_factored", regexp_extract(col("yearsExp"), "([0-9]*)", 1).cast("int"));
		return ResponseEntity.ok(rowToJson(s));
	}

	@GetMapping(path = "/kmeans", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public String kmeans() {
		return sparkServices.kmeans();
	}


	private Gson gson = new Gson();
	private List<JsonObject> rowToJson(Dataset<Row> s){
		return s.toJSON().collectAsList().stream().map(m->gson.fromJson(m,JsonObject.class)).collect(Collectors.toList());
	}
}
