package cn.et.food.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FoodDaoImpl {
	@Autowired
	JdbcTemplate jdbc;
	
	
	
	
	/**
	 * 从数据库中批量查询数据
	 * @param start 开始的位置
	 * @param count 每次取的条数
	 * @return
	 */
	public List<Map<String,Object>> queryFood(int start,int count){
		String sql = "select * from food limit "+start+","+count;
		List<Map<String, Object>> foodList = jdbc.queryForList(sql);
		return foodList;
	}
	
	/**
	 * 获取数据总数
	 * @return
	 */
	public int getAllCounts(){
		String sql = "select count(*) as counts from food";
		return Integer.parseInt(jdbc.queryForList(sql).get(0).get("counts").toString());
	}
	
	 
}
