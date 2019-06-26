package bowen;

import java.util.Arrays;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MyTask {
	private static final String TO_EMAIL1 = "bowen_xu@comcast.com";
	private static final String TO_EMAIL2 = "nextvm@comcast.com";
	
	private static final String URL1 = "https://xvevmms.vmail.comcast.net/";
	private static final String URL2 = "https://xvevmapi.vmail.comcast.net/";
	private static final String URL3 = "https://web-po-bvip.vmail.comcast.net/";
	
	@Scheduled(fixedRate = 1000*60*60*24*7) //trigger every week
    public void work() {
//		List<String> recipients = Arrays.asList(TO_EMAIL1, TO_EMAIL2);
		List<String> recipients = Arrays.asList(TO_EMAIL1);
		List<String> urls = Arrays.asList(URL1, URL2, URL3);
		Cert cert = new Cert(recipients, urls);
		cert.run();
    }
}
