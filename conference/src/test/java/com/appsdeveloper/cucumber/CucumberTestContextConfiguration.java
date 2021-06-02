package com.appsdeveloper.cucumber;

import com.appsdeveloper.ConferenceApp;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

@CucumberContextConfiguration
@SpringBootTest(classes = ConferenceApp.class)
@WebAppConfiguration
public class CucumberTestContextConfiguration {}
