package edu.hanyang.submit;

import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.*;
import org.apache.lucene.analysis.tokenattributes.*;
import org.tartarus.snowball.ext.*;

import edu.hanyang.indexer.Tokenizer;

public class TinySETokenizer implements Tokenizer {
	public SimpleAnalyzer analyzer;
	public PorterStemmer stemmer;
	public List<String> result; 

	public void setup() {
		analyzer = new SimpleAnalyzer();
		stemmer = new PorterStemmer();
		result = new ArrayList<String>();
	}

	public List<String> split(String text) {
		result.clear();
		try {
			TokenStream stream = analyzer.tokenStream(null, new StringReader(text));
			stream.reset();
			CharTermAttribute term = stream.getAttribute(CharTermAttribute.class);
			
			while (stream.incrementToken()) {
				stemmer.setCurrent(term.toString());
				stemmer.stem();
				result.add(stemmer.getCurrent());
				System.out.println(stemmer.getCurrent());
			}
			stream.close();
		}catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public void clean() {
		analyzer.close();
	}

}