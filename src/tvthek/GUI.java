package tvthek;

import java.awt.Desktop;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ListItem;

import tvthek.Parser.Source;
import tvthek.Parser.Video;
import tvthek.Parser.VideoInformation;

public class GUI implements Application {
	private Window window = null;
	private TextInput url;
	private PushButton start;
	private ListView result;
	private ArrayList<String> links;

	@Override
	public void startup(Display display, Map<String, String> properties)
			throws Exception {
		BXMLSerializer wtkxSerializer = new BXMLSerializer();
		window = (Window) wtkxSerializer.readObject(getClass().getResource(
				"gui.bxml"));
		url = (TextInput) wtkxSerializer.getNamespace().get("url");
		result = (ListView) wtkxSerializer.getNamespace().get("result");
		result.getListViewSelectionListeners().add(
				new ListViewSelectionListener() {

					@Override
					public void selectedRangesChanged(ListView arg0,
							Sequence<Span> arg1) {
					}

					@Override
					public void selectedRangeRemoved(ListView arg0, int arg1,
							int arg2) {
					}

					@Override
					public void selectedRangeAdded(ListView arg0, int arg1,
							int arg2) {
					}

					@Override
					public void selectedItemChanged(ListView arg0, Object arg1) {
						int i = result.getFirstSelectedIndex();
						if (links != null && links.getLength() > i)
							try {
								openWebpage(new URL(links.get(i)));
							} catch (MalformedURLException e) {
								e.printStackTrace();
							}
					}
				});
		start = (PushButton) wtkxSerializer.getNamespace().get("pushButton");
		start.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button arg0) {
				start.setEnabled(false);
				result.setListData(new ArrayList<ListItem>());
				new Thread(new Runnable() {
					@Override
					public void run() {
						runThreaded();
					}
				}).start();
			}
		});
		window.open(display);
	}

	public void runThreaded() {
		final List<ListItem> list = new ArrayList<ListItem>();
		links = new ArrayList<String>();
		String urlStr = url.getText();
		try {
			VideoInformation vi = new Parser(urlStr).parse();
			if (vi != null) {
				if (vi.playlist != null && vi.playlist.videos != null) {
					for (Video v : vi.playlist.videos) {
						list.add(new ListItem(v.title));
						for (Source s : v.sources) {
							if (s.isValid()) {
								links.add(s.src);
								break;
							}
						}
					}
				} else {
					throw new RuntimeException("got no playlist");
				}
			} else {
				throw new RuntimeException("got no VideoInformation");
			}
		} catch (Exception e) {
			System.err.println("Error while trying to parse " + urlStr);
			e.printStackTrace();
		} finally {
			ApplicationContext.queueCallback(new Runnable() {
				@Override
				public void run() {
					start.setEnabled(true);
					result.setListData(list);
				}
			});
		}
	}

	@Override
	public boolean shutdown(boolean optional) {
		if (window != null) {
			window.close();
		}
		return false;
	}

	@Override
	public void suspend() {
	}

	@Override
	public void resume() {
	}

	public static void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop()
				: null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void openWebpage(URL url) {
		try {
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

}