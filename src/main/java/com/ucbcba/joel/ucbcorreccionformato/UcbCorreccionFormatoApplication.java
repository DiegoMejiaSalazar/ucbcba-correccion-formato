package com.ucbcba.joel.ucbcorreccionformato;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.ucbcba.joel.ucbcorreccionformato.upload_download_file.property.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class
})
public class UcbCorreccionFormatoApplication {

	public static void main(String[] args) {
		SpringApplication.run(UcbCorreccionFormatoApplication.class, args);
	}

}
