package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.SearchItemService;

@Service(timeout=500000)
public class SearchItemServiceImpl implements SearchItemService {
	
	@Autowired
	private SolrTemplate solrTemplate;
	
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public Map<String,Object> search(Map searchMap) {
		
		String keywords = ((String) searchMap.get("keywords")).replaceAll(" ", "");
		searchMap.put("keywords", keywords);
		Map map = new HashMap();
		//高亮显示结果
		map.putAll(itemList(searchMap));
		 
		//根据分类分组结果
		List categoryList = categoryList(searchMap);
		map.put("categoryList", categoryList);
		
		//如果传递的参数包含分类，则传递的分类显示品牌和规格，否则按分类的第一个选项来
		
		if(categoryList!=null&&categoryList.size()>0) {
			if(!"".equals(searchMap.get("category"))) {
			Long categoryId = (Long)redisTemplate.boundHashOps("categoryList").get(searchMap.get("category"));
			 map.putAll(brandListAndSpecList (categoryId)); 
			}else {
			Long categoryId = (Long) redisTemplate.boundHashOps("categoryList").get(categoryList.get(0));
			map.putAll(brandListAndSpecList (categoryId)); 
			}
		}
		return map;
	}

	private Map brandListAndSpecList( Long categoryId) {
		Map map = new HashMap();
		List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(categoryId);
		map.put("brandList", brandList);
		 
		 List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(categoryId);
		 map.put("specList", specList);
		 
		 return map;
	}

	//返回分类的结果
	private List categoryList(Map searchMap) {
		List list = new ArrayList();
		Query query=new SimpleQuery("*:*");
		//根据keywords添加查询条件
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//设置分组条件
		GroupOptions options = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(options);
		
		//得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		
		//根据列得到分组结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//得到分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//得到分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		for(GroupEntry<TbItem> entry:content){
			list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中	
		}
		return list;

		
	}
	
	
	//返回高亮显示的结果，
	private Map itemList(Map searchMap) {
		Map map = new HashMap();
		HighlightQuery query=new SimpleHighlightQuery();
		
		HighlightOptions hightLightOptions =new HighlightOptions().addField("item_title");//设置高亮的域（可多值）
		hightLightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
		hightLightOptions.setSimplePostfix("</em>");//高亮后缀
		query.setHighlightOptions(hightLightOptions );//添加高亮选项
		
		//1.添加关键字查询条件，查询item_keywords域
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//2.添加分类查询
		String category  = (String) searchMap.get("category");
		if(category!=null&&category.length()>0) {
			Criteria criteria2 = new Criteria("item_category").is(category);
			query.addCriteria(criteria2);
		}
		
		
		//3.添加品牌查询
		String brand  = (String) searchMap.get("brand");
		if(brand!=null&&brand.length()>0) {
			Criteria criteria3 = new Criteria("item_brand").is(brand);
			query.addCriteria(criteria3);
		}
		//4.根据规格查询
		
		if(searchMap.get("spec")!=null) {
			Map<String,String> specMap = (Map) searchMap.get("spec");
			Set<String> keySet = specMap.keySet();
			for (String key : keySet) {
				Criteria criteria4 = new Criteria("item_spec_"+key).is(specMap.get(key));
				query.addCriteria(criteria4);
				}
		}
	
		//5.添加价格查询条件
		if(!"".equals(searchMap.get("price"))){
			String[] prices =((String) searchMap.get("price")).split("-");
			if(!"0".equals(prices[0])) {
				Criteria criteria5 = new Criteria("item_price").greaterThanEqual(prices[0]);
				query.addCriteria(criteria5);
			}else if("0".equals(prices[0])){
				Criteria criteria5 = new Criteria("item_price").greaterThan(0);
				query.addCriteria(criteria5);
			}
			if(!"*".equals(prices[1])) {
				Criteria criteria6 = new Criteria("item_price").lessThanEqual(prices[1]);
				query.addCriteria(criteria6);
			}
			
		
		}
		
		//6.分页功能
		Integer currentPage= (Integer)searchMap.get("currentPage");
		if(currentPage==null) {
			currentPage=1;
		}
		Integer rows = (Integer)searchMap.get("rows");
		if(rows==null) {
			rows=40;
		}
		query.setOffset((currentPage-1)*rows);
		query.setRows(rows);
	
		//根据出入的域和升降关键字排序
		String sortValue = (String) searchMap.get("sortValue");
		String sortField = (String) searchMap.get("sortField");
		if(!"".equals(sortValue)&&!"".equals(sortField)) {
			if("ASC".equals(sortValue)) {
				Sort sort = new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}else {
				Sort sort = new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
			
		}
	
		
		
		//获取高亮的集合，
		HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);	
		
		List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted();
		for (HighlightEntry<TbItem> highlightEntry : highlighted) {//获取高亮的集合获取域集合
			TbItem item = highlightEntry.getEntity();
			List<Highlight> highlights = highlightEntry.getHighlights();//从高亮集合中获取域集合
			for (Highlight highlight : highlights) {
				List<String> snipplets = highlight.getSnipplets();//从域对象中获取值集合
				for (String str : snipplets) {
					if(str!=null&&str.length()>0) {
						item.setTitle(str);
					}
				}
			}
		
		}
		System.out.println(highlightPage.getTotalPages());
		map.put("itemList", highlightPage.getContent());
		map.put("totalPages", highlightPage.getTotalPages());
		map.put("totalElements", highlightPage.getTotalElements());
		return map;
	}
	
	public void saveItemList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

}
