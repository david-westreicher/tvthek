package tvthek;

import org.apache.pivot.wtk.DesktopApplicationContext;

public class Main {

	public static void main(String[] args) {
		if (args.length == 0)
			DesktopApplicationContext.main(GUI.class, new String[] {
					"--width=300", "--height=200" });
		else
			new Parser(args[0]).parse();
	}

}
