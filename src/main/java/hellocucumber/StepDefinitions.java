package hellocucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StepDefinitions {
	
	private String today = "";
	private String answer = "";
	
	@Given("TOday is Sunday")
	public void t_oday_is_sunday() {
	    // Write code here that turns the phrase above into concrete actions
	    today = "Sunday";
	}
	@When("I ask")
	public void i_ask() {
	    // Write code here that turns the phrase above into concrete actions
	   answer = isItFriday.isItFriday(today);
	}
	@Then("told not {string}")
	public void told_not(String string) {
	    // Write code here that turns the phrase above into concrete actions
	   assertEquals(string,answer);
	}

}

class isItFriday {
 static String isItFriday(String today)
	{
		return "Friday";
	}
}

