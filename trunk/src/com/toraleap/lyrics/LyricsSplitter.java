package com.toraleap.lyrics;

import android.graphics.Paint;

public final class LyricsSplitter {
	private LyricsSplitter() {}
	
	public static String split(String line, float dip) {
		if (!Preference.splitLyrics) return line;
		if (measureString(line, dip) <= 294)
			return line;
		else {
			int half = line.length() / 2;
			for (int i = 0; i < half / 2; i++) {
				int pos = half - i;
				char c = line.charAt(pos);
				if (c == ' ' || c == '¡¡') {
					return line.substring(0, pos).trim() + "\n" + line.substring(pos + 1, line.length()).trim();
				} else if (c == '(' || c == '<' || c == '[' || c == '{' || c == '£¨' || c == '¡¾' || c == '¡¼' || c == '¡¸' || c == '/') {
					return line.substring(0, pos).trim() + "\n" + line.substring(pos, line.length()).trim();
				} else if (c == ')' || c == '>' || c == ']' || c == '}' || c == '£©' || c == '¡¿' || c == '¡½' || c == '¡¹' || c == ',' || c == '£¬' || c == '¡£') {
					return line.substring(0, pos + 1).trim() + "\n" + line.substring(pos + 1, line.length()).trim();
				}
				pos = half + i + 1;
				c = line.charAt(pos);
				if (c == ' ' || c == '¡¡') {
					return line.substring(0, pos).trim() + "\n" + line.substring(pos + 1, line.length()).trim();
				} else if (c == '(' || c == '<' || c == '[' || c == '{' || c == '£¨' || c == '¡¾' || c == '¡¼' || c == '¡¸' || c == '/') {
					return line.substring(0, pos).trim() + "\n" + line.substring(pos, line.length()).trim();
				} else if (c == ')' || c == '>' || c == ']' || c == '}' || c == '£©' || c == '¡¿' || c == '¡½' || c == '¡¹' || c == ',' || c == '£¬' || c == '¡£') {
					return line.substring(0, pos + 1).trim() + "\n" + line.substring(pos + 1, line.length()).trim();
				}
			}
			return line.substring(0, half) + "\n" + line.substring(half, line.length());
		}
	}
	
	private static float measureString(String line, float dip) {
		Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextSize(dip2px(dip));
		return mTextPaint.measureText(line);
	}
	
	private static float dip2px(float px) {
		return px * Preference.density;
	}
	
	public static class Preference {
		public static boolean splitLyrics = true;
		public static float density = 0;
	}
}
