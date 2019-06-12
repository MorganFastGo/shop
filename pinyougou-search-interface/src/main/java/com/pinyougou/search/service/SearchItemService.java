package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface SearchItemService {
	Map<String,Object> search(Map searchMap);
	
	public void saveItemList(List list);
}
