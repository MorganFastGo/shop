package com.pinyougou.solrutil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

@Component
public class solrUtil {
	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private SolrTemplate solrtemplate;
	
	public void importData() {
		TbItemExample example=new TbItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");
		List<TbItem> list = itemMapper.selectByExample(example);
		for (TbItem tbItem : list) {
			Map specMap = JSON.parseObject(tbItem.getSpec(), Map.class);
			tbItem.setSpecMap(specMap);
		}
		solrtemplate.saveBeans(list);
		solrtemplate.commit();
		
	}

	public static void main(String[] agrs) {
		ApplicationContext ac = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
		solrUtil solr = (solrUtil) ac.getBean("solrUtil");
		solr.importData();
	}
	
}


