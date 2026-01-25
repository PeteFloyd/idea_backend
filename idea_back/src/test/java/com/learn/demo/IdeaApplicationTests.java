package com.learn.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = IdeaApplication.class)
@ActiveProfiles("test")
class IdeaApplicationTests {

	@Test
	void contextLoads() {
	}

}
