package neil.demo.heapcon;

import java.util.Date;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;
import neil.demo.heapcon.data.MyKey;
import neil.demo.heapcon.data.MyKeyByDate;
import neil.demo.heapcon.data.MyKeyByItem;

/**
 * <p>Initialization of the servers when the client first connects.
 * </p>
 * <p>Ensure all required maps exist now. Hazelcast will create themn
 * on demand when first needed, so demand to force creation. This
 * is very optional.
 * </p>
 * <p>Ensure the test data exists by injecting it if not present.
 * We could load test data from many places, for simplicity here
 * it is generated with a randomizer.
 * </p>
 */
@Configuration
@Slf4j
public class MyInitializer implements CommandLineRunner {

	private static final int ONE_YEAR = 365;
	private static final long ONE_DAY_IN_MS = 24 * 60 * 60 * 1000l;
	
	@Autowired
	private HazelcastInstance hazelcastClient;

	@Override
	public void run(String... args) throws Exception {
		
		// Touch all maps to ensure creation
		for (String mapName : MyConstants.MAP_NAMES) {
			this.hazelcastClient.getMap(mapName);
		}
		
		// Inject test data
		if (this.hazelcastClient.getMap(MyConstants.MY_KEY_MAP_NAME).isEmpty()) {
			int count = this.injectTestData();
			log.info("Test data loaded, {} items", count);
		} else {
			log.info("Skip test data load, maps not empty");
		}
		
	}

	
	/**
	 * 
	 */
	private int injectTestData() {
		Random random = new Random();
		long now = System.currentTimeMillis();
		int count=0;
		
		Map<MyKey, Double> 			myKeyMap
			= this.hazelcastClient.getMap(MyConstants.MY_KEY_MAP_NAME);
		Map<MyKeyByDate, Double> 	myKeyByDateMap
			= this.hazelcastClient.getMap(MyConstants.MY_KEY_BY_DATE_MAP_NAME);
		Map<MyKeyByItem, Double> 	myKeyByItemMap
			= this.hazelcastClient.getMap(MyConstants.MY_KEY_BY_ITEM_MAP_NAME);
		
		// Today back one year
		for (int i=0 ; i < ONE_YEAR ; i++) {
			Date when = new Date(now - i * ONE_DAY_IN_MS);
			
			for (int j=0 ; j< MyConstants.ITEMS.size(); j++) {
				
				MyKey myKey = new MyKey();
				myKey.setItem(MyConstants.ITEMS.get(j));
				myKey.setWhen(when);
				
				MyKeyByDate myKeyByDate = new MyKeyByDate();
				myKeyByDate.setItem(MyConstants.ITEMS.get(j));
				myKeyByDate.setWhen(when);
				
				MyKeyByItem myKeyByItem = new MyKeyByItem();
				myKeyByItem.setItem(MyConstants.ITEMS.get(j));
				myKeyByItem.setWhen(when);
				
				double price = random.nextDouble();
				
				myKeyMap.put(myKey, price);
				count++;
				myKeyByDateMap.put(myKeyByDate, price);
				count++;
				myKeyByItemMap.put(myKeyByItem, price);
				count++;
			}
			
		}

		return count;
	}

	

}
