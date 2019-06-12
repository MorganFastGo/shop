package com.pinyougou.search.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.SearchItemService;

@RestController
@RequestMapping("/search")
public class searchController {
	
	@Reference
	private SearchItemService searchItemService;
	
	@RequestMapping("/searchItem")
	public Map searchItem(@RequestBody Map searchMap) {
		
		return searchItemService.search(searchMap);
			
	}
	
	
}
