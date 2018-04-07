package com.how2java;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
 
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class TestLucene {
	public static void main(String[] args) throws Exception{
		//1.׼�����ķִ�
		IKAnalyzer analyzer = new IKAnalyzer();
		
		//2.����
		List<String> productNames = new ArrayList<>();
        productNames.add("������led����e27�ݿ�ů�����ݵƼ��������������ܵ���תɫ�µ���");
        productNames.add("������led����e14�ݿ��������3W������β���ܵ���ů�ƹ�ԴLamp");
        productNames.add("��ʿ���� LED���� e27���ݿڽ��ܵ�3W���ݵ� Lamp led���ܵ���");
        productNames.add("������ led���� e27�ݿڼ���3wů�����ݵƽ��ܵ�5W����LED����7w");
        productNames.add("������ledС����e14�ݿ�4.5w͸����led���ܵ���������Դlamp����");
        productNames.add("�������ѹ�Ӣ����̨�ƹ���ѧϰ�Ķ����ܵƾ�30508����Դ");
        productNames.add("ŷ������led����������ܵ���e14�ݿ����ݵƳ����������ƹ�Դ");
        productNames.add("ŷ������led���ݽ��ܵ��ݳ�����Դe14e27�����ݿ�С����ů�Ƽ���");
        productNames.add("��ŷ������led���ݽ��ܵ���e27�ݿ����ݼ���led�������Ƴ�����Դ");
        Directory index = createIndex(analyzer, productNames);
        
        // 3. ��ѯ��
        String keyword = "������";
        Query query = new QueryParser("name", analyzer).parse(keyword);
        
        //4. ����
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        int numberPerPage = 1000;
        System.out.printf("��ǰһ����%d������%n",productNames.size());
        System.out.printf("��ѯ�ؼ����ǣ�\"%s\"%n",keyword);
        ScoreDoc[] hits = searcher.search(query, numberPerPage).scoreDocs;
        
        // 5. ��ʾ��ѯ���
        showSearchResults(searcher, hits, query, analyzer);
        // 6. �رղ�ѯ
        reader.close();
	}
	
	 private static void showSearchResults(IndexSearcher searcher, ScoreDoc[] hits, Query query, IKAnalyzer analyzer)
	            throws Exception {
	        System.out.println("�ҵ� " + hits.length + " ������.");
	        System.out.println("���\tƥ��ȵ÷�\t���");
	        
	        //���ӷִʵĸ�����ʾ
	        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<span style='color:red'>", "</span>");
	        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(query));
	        
	        for (int i = 0; i < hits.length; ++i) {
	            ScoreDoc scoreDoc= hits[i];
	            int docId = scoreDoc.doc;
	            Document d = searcher.doc(docId);
	            List<IndexableField> fields = d.getFields();
	            System.out.print((i + 1));
	            System.out.print("\t" + scoreDoc.score);
	            for (IndexableField f : fields) {
	            	TokenStream tokenStream = analyzer.tokenStream(f.name(), new StringReader(d.get(f.name())));
	                String fieldContent = highlighter.getBestFragment(tokenStream, d.get(f.name()));
	                System.out.print("\t" + fieldContent);
	            }
	            System.out.println("<br>");
	        }
	    }
	
	private static Directory createIndex(IKAnalyzer analyzer, List<String> products) throws IOException {
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(index, config);
 
        for (String name : products) {
            addDoc(writer, name);
        }
        writer.close();
        return index;
    }
	
	private static void addDoc(IndexWriter w, String name) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("name", name, Field.Store.YES));
        w.addDocument(doc);
    }
}
