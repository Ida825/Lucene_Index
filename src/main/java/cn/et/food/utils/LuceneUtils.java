package cn.et.food.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.parser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;



public class LuceneUtils {
	//index文件库地址
	static String dir = "D:\\index";
	//定义分词器IKAnalyzer
	static IKAnalyzer analyzer = new IKAnalyzer();
	
	/**
	 * 创建索引库
	 * @throws IOException
	 */
	public static void write(Document doc) throws IOException{
		//获取索引库的存储目录
		Directory directory = FSDirectory.open(new File(dir));
		//关联lucene版本和当前分词器
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
		//传入目录和分词器
		IndexWriter writer = new IndexWriter(directory, config);
	
		//将对象写入索引库
		writer.addDocument(doc);		
		//提交事务
		writer.commit();
		//关流
		writer.close();		
	}

	/**
	 * 搜索
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws org.apache.lucene.queryparser.classic.ParseException 
	 * @throws InvalidTokenOffsetsException 
	 */
	public static List<Map<String,String>> search(String field,String val) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException, InvalidTokenOffsetsException{
		//获取索引库的存储目录
		Directory directory = FSDirectory.open(new File(dir));
		DirectoryReader reader = DirectoryReader.open(directory);
		//搜索类
		IndexSearcher searcher = new IndexSearcher(reader);
		//构建查询解析器  用于指定查询的属性名和分词器
		QueryParser parser = new QueryParser(Version.LUCENE_47, field, analyzer);
		//开始搜索
		Query query = parser.parse(val);
		
		
		//查询出结果后加高亮(前后缀处理：默认<B></B>)
		SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<font color='red'>","</font>");
		//将高亮搜索的词添加到高亮处理器中
		Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));
		
		//获取搜索的结果 指定返回的document个数
		ScoreDoc[] hits = searcher.search(query,null,10).scoreDocs;
		List<Map<String,String>> list = new ArrayList();
		for(int i=0;i<hits.length;i++){
			int id = hits[i].doc;
			Document hitDoc = searcher.doc(hits[i].doc);
			//将document再转成map集合
			Map map = new HashMap();
			map.put("foodid", hitDoc.get("foodid"));
			//给foodname加高亮
			String foodname = hitDoc.get("foodname");
			//将查询结果和搜索的词匹配  匹配到以后加前缀后缀处理
			TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), id, "foodname", analyzer);
			//传入的第二个参数是查询的值
			TextFragment[] frag = highlighter.getBestTextFragments(tokenStream, foodname, false, 200);
			
			String foodnameHigh = "";
			for(int j=0;j<frag.length;j++){
				if((frag[j] != null) && (frag[j].getScore()>0)){
					foodnameHigh = frag[j].toString();
				}
			}
			
			
			map.put("foodname",foodnameHigh );
			map.put("price", hitDoc.get("price"));
			//map.put("img", hitDoc.get("img"));
			list.add(map);
		}
		reader.close();
		directory.close();
		
		return list;
	}
	
	
}
