package neil.demo.heapcon.data;

import com.hazelcast.core.PartitionAware;

/**
 * <p>Extend {@link MyKey} with routing information for Hazelcast.
 * Make the routing by the item String's first character.
 * </p>
 * <p>This means for the three keys "{@code [milk,2018-10-18]}",
 * "{@code [milk,2018-10-19]}" and "{@code [beer,2018-10-19]}" the
 * items containing milk are kept together.
 * </p>
 * <p>See {@link MyKeyByDate} which does the reverse.
 * </p> 
 */
@SuppressWarnings("serial")
public class MyKeyByItem extends MyKey implements PartitionAware<String> {

	@Override
	public String getPartitionKey() {
		// Assume no empty string
		return this.item.substring(0, 1);
	}

}
