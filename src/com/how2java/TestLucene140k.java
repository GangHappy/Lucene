package com.how2java;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Scanner;
 
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
public class TestLucene140k {
	public static void main(String[] args) throws Exception {
        // 1. ׼�����ķִ���
        IKAnalyzer analyzer = new IKAnalyzer();
        // 2. ����
        Directory index = createIndex(analyzer);
 
        // 3. ��ѯ��
         
        Scanner s = new Scanner(System.in);
         
        while(true){
            System.out.print("�������ѯ�ؼ��֣�");
            String keyword = s.nextLine();
            System.out.println("��ǰ�ؼ����ǣ�"+keyword);
            Query query = new QueryParser( "name", analyzer).parse(keyword);
 
            // 4. ����
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher=new IndexSearcher(reader);
            int numberPerPage = 10;
            ScoreDoc[] hits = searcher.search(query, numberPerPage).scoreDocs;
             
            // 5. ��ʾ��ѯ���
            showSearchResults(searcher, hits,query,analyzer);
            // 6. �رղ�ѯ
            reader.close();
        }
         
    }
 
    private static void showSearchResults(IndexSearcher searcher, ScoreDoc[] hits, Query query, IKAnalyzer analyzer) throws Exception {
        System.out.println("�ҵ� " + hits.length + " ������.");
 
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<span style='color:red'>", "</span>");
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(query));
 
        System.out.println("�ҵ� " + hits.length + " ������.");
        System.out.println("���\tƥ��ȵ÷�\t���");
        for (int i = 0; i < hits.length; ++i) {
            ScoreDoc scoreDoc= hits[i];
            int docId = scoreDoc.doc;
            Document d = searcher.doc(docId);
            List<IndexableField> fields= d.getFields();
            System.out.print((i + 1) );
            System.out.print("\t" + scoreDoc.score);
            for (IndexableField f : fields) {
 
                if("name".equals(f.name())){
                    TokenStream tokenStream = analyzer.tokenStream(f.name(), new StringReader(d.get(f.name())));
                    String fieldContent = highlighter.getBestFragment(tokenStream, d.get(f.name()));
                    System.out.print("\t"+fieldContent);
                }
                else{
                    System.out.print("\t"+d.get(f.name()));
                }
            }
            System.out.println("<br>");
        }
    }
 
    private static Directory createIndex(IKAnalyzer analyzer) throws IOException {
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(index, config);
        String fileName = "140k_products.txt";
        List<Product> products = ProductUtil.file2list(fileName);
        int total = products.size();
        int count = 0;
        int per = 0;
        int oldPer =0;
        for (Product p : products) {
            addDoc(writer, p);
            count++;
            per = count*100/total;
            if(per!=oldPer){
                oldPer = per;
                System.out.printf("�����У��ܹ�Ҫ��� %d ����¼����ǰ��ӽ����ǣ� %d%% %n",total,per);
            }
             
        }
        writer.close();
        return index;
    }
 
    private static void addDoc(IndexWriter w, Product p) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("id", String.valueOf(p.getId()), Field.Store.YES));
        doc.add(new TextField("name", p.getName(), Field.Store.YES));
        doc.add(new TextField("category", p.getCategory(), Field.Store.YES));
        doc.add(new TextField("price", String.valueOf(p.getPrice()), Field.Store.YES));
        doc.add(new TextField("place", p.getPlace(), Field.Store.YES));
        doc.add(new TextField("code", p.getCode(), Field.Store.YES));
        w.addDocument(doc);
    }
}
