package neil.demo.heapcon.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.Partition;

import lombok.extern.slf4j.Slf4j;
import neil.demo.heapcon.MyConstants;
import neil.demo.heapcon.data.MyKey;

/**
 * <p>A controller handling the subset of data requests,
 * "{@code data/mykey}", "{@code data/mykeybydate}" and "{@code data/mykeybyitem}".
 * All are similar, so the code tries to exploit thias.
 * </p>? 
 */
@Controller
@RequestMapping("data")
@Slf4j
public class DataController {

    @Autowired
    private HazelcastInstance hazelcastClient;

    /**
     * <p>All three page mappings can be handled by the same method
     * </p>
     */
    @GetMapping("/mykey")
    public ModelAndView myKey(HttpSession httpSession, HttpServletRequest httpServletRequest) {
        log.info("myKey(), session={}", httpSession.getId());
        return this.dataInfo(MyConstants.MY_KEY_MAP_NAME);
    }
	@GetMapping("/mykeybydate")
    public ModelAndView myKeyByDate(HttpSession httpSession, HttpServletRequest httpServletRequest) {
        log.info("myKeyByDate(), session={}", httpSession.getId());
        return this.dataInfo(MyConstants.MY_KEY_BY_DATE_MAP_NAME);
    }
    @GetMapping("/mykeybyitem")
    public ModelAndView myKeyByItem(HttpSession httpSession, HttpServletRequest httpServletRequest) {
        log.info("myKeyByItem(), session={}", httpSession.getId());
        return this.dataInfo(MyConstants.MY_KEY_BY_ITEM_MAP_NAME);
    }

    /**
     * <p>Examine every key in a Hazelcast map, to determine which partition is
     * in and which member (server) is hosting that partition.
     * </p>
     * <p><b>This is just for demo purposes</b></p>
     * <p>The "{@code keySet()}" operation returns every key. If the map is so
     * big it has to be spread across several members then it's likely it will
     * swamp the caller to retrieve all keys at once.
     * </p>
     * <p>Determining the partition and member for each uses a lot of CPU
     * </p>
     * <p>This coding isn't defensive for the scenarios where members join
     * or leave, the size of the arrays used will be wrong and out of bounds
     * exceptions will likely be thrown.
     * </p>
     * 
     * @param mapName The Hazelcast map to inspect.
     * @return The page to render, "data"
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private ModelAndView dataInfo(String mapName) {
    	int partitions = this.hazelcastClient.getPartitionService().getPartitions().size();
    	List<Member> members = new ArrayList<>(this.hazelcastClient.getCluster().getMembers());
    	
    	ModelAndView modelAndView = new ModelAndView("data");
    	
    	modelAndView.addObject("mapname", mapName);
    	
    	Map<?,?> map = this.hazelcastClient.getMap(mapName);
    	
    	// This is unwise to do, keySet() could be huge
    	Collection<?> keySet = map.keySet();
    	
    	TreeSet[] keys = new TreeSet[MyConstants.ITEMS.size()];
    	for (int i = 0 ; i < keys.length ; i++) {
    		keys[i] = new TreeSet();
    	}
    	int[][] memberUsage = new int[MyConstants.ITEMS.size()][members.size()];
    	int[][] partitionUsage = new int[MyConstants.ITEMS.size()][partitions];

        List<String> columns = new ArrayList<>();
        columns.add("Item");
        columns.add("Partitions Used");
        columns.add("Members Used");
        modelAndView.addObject("columns", columns);
        List<List<String>> data = new ArrayList<>();
        TreeMap<String, List<List<String>>> detail
        	= new TreeMap<>(); 
               
    	try {
        	for (Object key : keySet) {
        		// Base type is MyKey even if MyKeyByItem or MyKeyByDate
        		MyKey myKey = (MyKey) key;    		
        		
        		// Hazelcast partition info, which member has this key
        		Partition partition = this.hazelcastClient.getPartitionService().getPartition(myKey);
        		
        		// Which local array to save in
        		int i = MyConstants.ITEMS.indexOf(myKey.getItem());
        		int j = members.indexOf(partition.getOwner());
        		
        		keys[i].add(myKey);
        		memberUsage[i][j]++;
        		partitionUsage[i][partition.getPartitionId()]++;
        	}
        	
        	for (int i=0 ; i< MyConstants.ITEMS.size(); i++) {
        		List<String> datum = new ArrayList<>();
        		datum.add(MyConstants.ITEMS.get(i));
        		int count = 0;
        		for (int j=0 ; j<partitions ; j++) {
        			if (partitionUsage[i][j] > 0) {
        				count++;
        			}
        		}
        		datum.add(String.valueOf(count));
        		count = 0;
        		for (int j=0 ; j<members.size() ; j++) {
        			if (memberUsage[i][j] > 0) {
        				count++;
        			}
        		}
        		datum.add(String.valueOf(count));
        		data.add(datum);
        	}
            modelAndView.addObject("data", data);
            
            for (int i = 0 ; i<MyConstants.ITEMS.size(); i++) {
            	String key = MyConstants.ITEMS.get(i);
            	
            	List<List<String>> value = new ArrayList<>();
            	List<String> line0 = new ArrayList<>();
            	List<String> line1 = new ArrayList<>();
            	List<String> line2 = new ArrayList<>();
            	value.add(line0);
            	value.add(line1);
            	value.add(line2);

            	line0.addAll(Arrays.asList(new String[] {"Item","Date","Price","Partition","Member"}));
            	
            	MyKey myKey1 = (MyKey) keys[i].first();
            	Partition partition = this.hazelcastClient.getPartitionService().getPartition(myKey1);
            	line1.addAll(Arrays.asList(new String[] {
            			myKey1.getItem(),
            			myKey1.getWhen().toString(),
            			map.get(myKey1).toString(),
            			Integer.toString(partition.getPartitionId()),
            			partition.getOwner().toString()
            			}));
            	
            	keys[i].remove(myKey1);
            	MyKey myKey2 = (MyKey) keys[i].first();
            	partition = this.hazelcastClient.getPartitionService().getPartition(myKey1);
            	line2.addAll(Arrays.asList(new String[] {
            			myKey2.getItem(),
            			myKey2.getWhen().toString(),
            			map.get(myKey2).toString(),
            			Integer.toString(partition.getPartitionId()),
            			partition.getOwner().toString()
            			}));

            	detail.put(key, value);
            }
            modelAndView.addObject("detail", detail);

    	} catch (Exception e) {
    		// See Javadoc above, can occur if members join/leave while this calculation runs
    		log.error("dataInfo", e);
    	}

    	return modelAndView;
	}

}
