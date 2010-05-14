package com.toraleap.lyrics;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.universalchardet.UniversalDetector;

public class LyricsParser {
	HashMap<Long, String> Lyrics = new HashMap<Long, String>();
	ArrayList<Long> LyricsTime = new ArrayList<Long>();
	int current;
	long offset = 0;
	public String encoding;
	public String lyricsPath;
	
	public LyricsParser(String lyrics) {
		Pattern pattern, patternOffset, patternTimestamp;
		
		// Lyrics offset tag
		patternOffset = Pattern.compile("\\[offset:(\\d+)\\]", Pattern.CASE_INSENSITIVE);
		Matcher matcherOffset = patternOffset.matcher(lyrics);
		if (matcherOffset.find()) offset = Long.valueOf(matcherOffset.group(1));
		
		// Lyrics timestamp tag
		pattern = Pattern.compile("^(\\[[0-9:\\.\\[\\]]+\\])+(.*)$", Pattern.MULTILINE);
		
		// Split every timestamp tag
		patternTimestamp = Pattern.compile("\\[(\\d+):([0-9\\.]+)\\]");
		Matcher matcher = pattern.matcher(lyrics);
		while(matcher.find()) {
			Matcher matcherTimestamp = patternTimestamp.matcher(matcher.group(1));
			while(matcherTimestamp.find()) {
				String content = matcher.group(2).trim();
				if (Preference.skipBlank && content.length() == 0) continue;
				long Timestamp = Long.valueOf(matcherTimestamp.group(1)) * 60000 + (long)(Float.valueOf(matcherTimestamp.group(2)) * 1000);
				LyricsTime.add(Timestamp);
				Lyrics.put(Timestamp, content);			
			}
		}
		
		// Sort timestamp tag
		Collections.sort(LyricsTime);
	}
	
	public static LyricsParser FromMediaFile(String mediaPath) {
		return FromLyricsFile(mediaPath.substring(0, mediaPath.lastIndexOf(".")) + ".lrc");
	}
	
	public static LyricsParser FromLyricsFile(String lyricsPath) {
        BufferedInputStream in;
		try {
			in = new BufferedInputStream(new FileInputStream(lyricsPath));
	        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);         
	        byte[] temp = new byte[1024];      
	        int size = 0;      
	        while ((size = in.read(temp)) != -1) {
	            out.write(temp, 0, size);      
	        }      
	        in.close();      
	        byte[] content = out.toByteArray();
	        String encoding;
	        if (Preference.charset.equalsIgnoreCase("auto")) {
		        UniversalDetector detector = new UniversalDetector(null);
		        detector.handleData(content, 0, content.length);
		        detector.dataEnd();
		        encoding = detector.getDetectedCharset();
	        } else {
	        	encoding = Preference.charset;
	        }
			LyricsParser lyricsParser = new LyricsParser(new String(content, 0, content.length, encoding));
			lyricsParser.encoding = encoding;
			lyricsParser.lyricsPath = lyricsPath;
	        return lyricsParser;
		} catch (IOException e) {
			return new LyricsParser("");
		}
	}
	
	public String getLyrics(long timestamp) {
		for (int i = LyricsTime.size() - 1; i >= 0; i--) {
			if (LyricsTime.get(i) - Preference.offset < timestamp) {
				return Lyrics.get(LyricsTime.get(i));
			}
		}
		return null;
	}
	
	public LyricsContext getLyricsContext(long timestamp) {
		String prev = "", curr = "", next = "";
		for (int i = LyricsTime.size() - 1; i >= 0; i--) {
			if (LyricsTime.get(i) - Preference.offset < timestamp) {
				if (i > 0) prev = Lyrics.get(LyricsTime.get(i-1));
				curr = Lyrics.get(LyricsTime.get(i));
				if (i < LyricsTime.size() - 1) next = Lyrics.get(LyricsTime.get(i+1));
				return new LyricsContext(i, prev, curr, next);
			}
		}
		return null;
	}
	
	public static class Preference {
		public static boolean doubleLine = true;
		public static boolean skipBlank = true;
		public static long offset = 0;
		public static String charset = "auto";
	}
	
	public class LyricsContext {
		public int id = 0;
		public String prev = "";
		public String curr = "";
		public String next = "";
		
		public LyricsContext(int id, String prev, String curr, String next) {
			this.id = id;
			this.prev = prev;
			this.curr = curr;
			this.next = next;
		}
	}
}
