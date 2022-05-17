package com.example.democn;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolaController {

	@GetMapping("/")
	public String index() {
		
		System.out.println(" prueba traza");
		
		
		System.out.println(" prueba traza 2");
		
		return "Hola Mundo desde Spring Boot!";
	}

}