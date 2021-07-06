package proj_files.wazzuf_webclient_service;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.ApplicationContext;


@SpringBootApplication
public class WazzufWebclientServiceApplication {

	public static void main(String[] args) {

		ApplicationContext Context = SpringApplication.run(WazzufWebclientServiceApplication.class, args);
	}

};
