package test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zj.w3x.ProcessHandlerDefImpl;
import com.zj.w3x.W3X;

public class MainTest {
	
	public static Logger logger = LoggerFactory.getLogger(MainTest.class);
	
	@Test
	public void testAll() throws IOException, ClassNotFoundException, SQLException {
		W3X x = new W3X();
		x.addUrls(Arrays.asList(new String[] { W3X.DEFAULT_URL_TODAY, W3X.DEFAULT_URL_YESTERDAY, W3X.DEFAULT_URL_BEFORE_YESTERDAY }));
		x.setProcessHandler(new ProcessHandlerDefImpl());
		x.start();
	}
}
