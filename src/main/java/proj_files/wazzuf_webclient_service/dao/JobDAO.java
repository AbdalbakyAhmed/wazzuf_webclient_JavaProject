package proj_files.wazzuf_webclient_service.dao;

import java.io.Serializable;

import org.apache.spark.sql.Dataset;

import proj_files.wazzuf_webclient_service.pojo.Job;


public interface JobDAO extends Serializable{
    Dataset<Job> findAll();
    Dataset<Job> find(Job j);
    Boolean addJob(Job j);
    Boolean removeJob(Job j);
}
