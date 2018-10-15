package neil.demo.heapcon;

import java.util.Arrays;
import java.util.List;

import neil.demo.heapcon.data.MyKey;
import neil.demo.heapcon.data.MyKeyByDate;
import neil.demo.heapcon.data.MyKeyByItem;

/**
 * <p>Constants to help the application
 * </p>
 */
public class MyConstants {

	public static final String JSESSIONID_MAP_NAME = "jsessionid";
	public static final String MY_KEY_MAP_NAME = MyKey.class.getSimpleName();
	public static final String MY_KEY_BY_DATE_MAP_NAME = MyKeyByDate.class.getSimpleName();
	public static final String MY_KEY_BY_ITEM_MAP_NAME = MyKeyByItem.class.getSimpleName();

	public static final List<String> MAP_NAMES = Arrays.asList(new String[]{
			JSESSIONID_MAP_NAME,
			MY_KEY_MAP_NAME,
			MY_KEY_BY_DATE_MAP_NAME,
			MY_KEY_BY_ITEM_MAP_NAME
	});
	
	// What things are we interested in - today precious metals, tomorrow leisure activities
	public static final List<String> ITEMS = Arrays.asList(new String[]{
			"Gold", "Silver", "Platinum", "Palladium"
	});
	
}
