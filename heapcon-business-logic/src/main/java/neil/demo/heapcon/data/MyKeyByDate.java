package neil.demo.heapcon.data;

import java.util.Date;

import com.hazelcast.core.PartitionAware;

/**
 * <p>Extend {@link MyKey} with routing information for Hazelcast.
 * Make the routing by the item String.
 * </p>
 * <p>This means for the three keys "{@code [milk,2018-10-18]}",
 * "{@code [milk,2018-10-19]}" and "{@code [beer,2018-10-19]}" the
 * items for the 19th October are kept together.
 * </p>
 * <p>See {@link MyKeyByItem} which does the reverse.
 * </p> 
 */
@SuppressWarnings("serial")
public class MyKeyByDate extends MyKey implements PartitionAware<Date> {

	@Override
	public Date getPartitionKey() {
		return this.when;
	}

}
