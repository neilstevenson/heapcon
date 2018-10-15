package neil.demo.heapcon.data;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>The data we will store is the prices for things at various
 * ponts in time, so a time series.
 * </p>
 * <p>We are using {@link com.hazelcast.core.IMap IMap} which is
 * a <i>key-value</i> store. We wish the key to be the pairing
 * of the item name and the date of the price. The value will
 * be the price itself.
 * </p>
 * <p>So the price of milk on the 19th October is 1 Euro, the
 * key would be "{@code [milk,2018-10-19]}" stored in this
 * object, and the value would be "{@code 1.00}" stored
 * in a {@code java.lang.Double} or similar.
 */
@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyKey implements Serializable, Comparable<MyKey> {
	
	public String item;
	public Date   when;

	/**
	 * <p>Reverse sort by item them by time, only needed for printing on screen,
	 * not used by Hazelcast.
	 * </p>
	 */
	@Override
	public int compareTo(MyKey that) {
		int byItem = this.item.compareTo(that.getItem());
		return byItem==0 ? 
				-1 * this.when.compareTo(that.getWhen()) : -1 * byItem;
	}

}
