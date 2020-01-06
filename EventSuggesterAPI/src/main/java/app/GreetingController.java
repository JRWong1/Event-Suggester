package app;

import java.util.concurrent.atomic.AtomicLong;
import java.util.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

  private static final String template = "Hello, %s!";
  private final AtomicLong counter = new AtomicLong();

  @RequestMapping("/greeting") //@RequestMaping(method=GET)
  public ArrayList<Greeting> greeting(@RequestParam(value="name", defaultValue="World") String name) {
	  ArrayList<Greeting> greetings = new ArrayList<>();
	  greetings.add(new Greeting(counter.incrementAndGet(), String.format(template, name)));
	  greetings.add(new Greeting(counter.incrementAndGet(), String.format(template, name)));

//    return new Greeting(counter.incrementAndGet(),
//              String.format(template, name));
    
    return greetings;
  }
}