package io.github.code.visual;

import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;


@SpringBootTest(classes = {MyNonWebTests.class})
public class MyNonWebTests extends BaseTest {

    @Test
    public void greetingShouldReturnDefaultMessage() {
        // ...
        System.out.println("true = " + true);
    }
}
