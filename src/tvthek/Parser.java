package tvthek;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.google.gson.Gson;

public class Parser {

	private String site;

	public Parser(String site2) {
		this.site = site2;
	}

	public VideoInformation parse() {
		try {
			URL url = new URL(site);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // Cast
																				// shouldn't
																				// fail
			HttpURLConnection.setFollowRedirects(true);
			// allow both GZip and Deflate (ZLib) encodings
			// conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			conn.setRequestProperty(
					"User-agent",
					"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/30.0.1599.114 Chrome/30.0.1599.114 Safari/537.36");
			String encoding = conn.getContentEncoding();
			InputStream inStr = null;

			// create the appropriate stream wrapper based on
			// the encoding type
			if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
				inStr = new GZIPInputStream(conn.getInputStream());
			} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
				inStr = new InflaterInputStream(conn.getInputStream(),
						new Inflater(true));
			} else {
				inStr = conn.getInputStream();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(inStr));

			String line;
			int count = 0;
			while ((line = in.readLine()) != null)
				if (line.contains("<div class=\"jsb_ jsb_VideoPlaylist\" data-jsb=")
						|| count > 0 && count < 1) {
					count++;
					String json = line.substring(line.indexOf("{"),
							Math.min(line.lastIndexOf("}") + 1, line.length()));
					json = json.replaceAll("&quot;", "\"");
					json = json.replaceAll("&amp;", "&");
					json = json.replaceAll("\\\\/", "/");
					// System.out.println(json);
					VideoInformation vi = new Gson().fromJson(json,
							VideoInformation.class);
					System.out.println(vi);
					return vi;
				}
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public class VideoInformation {
		@Override
		public String toString() {
			return playlist.toString();
		}

		public Playlist playlist;
	}

	public class Playlist {
		@Override
		public String toString() {
			String ret = "";
			for (Video v : videos)
				ret += v.toString();
			return ret;
		}

		public Video[] videos;
	}

	public class Video {
		@Override
		public String toString() {
			String ret = "";
			for (Source s : sources)
				if (s.isValid())
					ret += s.toString() + "\n";
			return title + "\n" + ret + "\n";
		}

		public String title;
		public Source[] sources;
	}

	public class Source {
		public String delivery;
		public String quality_string;
		public String src;
		public String protocol;

		public boolean isValid() {
			return quality_string.equals("hoch")
					&& protocol.equals("http")
					&& (delivery.equals("progressive") || delivery
							.equals("hls"));
		}

		@Override
		public String toString() {
			return src;
		}
	}
}
