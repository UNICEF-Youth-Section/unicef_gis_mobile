package edu.mit.mobile.android.utils;
/*
 * Copyright (C) 2010 MIT Mobile Experience Lab
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamUtils {
	/**
	 * Read an InputStream into a String until it hits EOF.
	 *  
	 * @param in
	 * @return the complete contents of the InputStream
	 * @throws IOException
	 */
	static public String inputStreamToString(InputStream in) throws IOException{
		final int bufsize = 8196;
		final char[] cbuf = new char[bufsize];

		final StringBuffer buf = new StringBuffer(bufsize);

		final InputStreamReader in_reader = new InputStreamReader(in);

		for (int readBytes = in_reader.read(cbuf, 0, bufsize);
			readBytes > 0;
			readBytes = in_reader.read(cbuf, 0, bufsize)) {
			buf.append(cbuf, 0, readBytes);
		}

		return buf.toString();
	}
}