package cn.et.food.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.flexible.standard.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.et.food.dao.FoodDaoImpl;
import cn.et.food.utils.LuceneUtils;

@RestController
public class FoodController {
	 
	@Autowired
	FoodDaoImpl dao;
	
	@GetMapping("/searchFood")
	public List<Map<String,String>> searchFood(String keyWord) throws Exception{
		return LuceneUtils.search("foodname", keyWord);
	}
	
	
	/**
	 * 从数据库中查询数据
	 * @return
	 * @throws IOException 
	 */
	@GetMapping("/createIndex")
	public String createIndex() throws IOException{
		try {
			int allCounts = dao.getAllCounts();
			//开始位置
			int start = 0;
			//每次从数据库中取10条数据
			int count = 10;
			//第一次取0-9
			//第二次取10-19...

			while(start!=allCounts){			
				List<Map<String,Object>> foodList = dao.queryFood(start, count);
				for(int i=0;i<foodList.size();i++){						
					Map<String,Object> map = foodList.get(i);					
					Document doc = new Document();
					Field field1 = new Field("foodid", map.get("foodid").toString(),TextField.TYPE_STORED);
					Field field2 = new Field("foodname", map.get("foodname").toString(),TextField.TYPE_STORED);
					Field field3 = new Field("price", map.get("price").toString(),TextField.TYPE_STORED);
					Field field4 = new Field("img", map.get("img").toString(),TextField.TYPE_STORED);
					
					doc.add(field1);
					doc.add(field2);
					doc.add(field3);
					doc.add(field4);
					//写入分词库
					LuceneUtils.write(doc);
					start++;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
		return "1";
	}
}
