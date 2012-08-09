package test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zj.w3x.ProcessHandlerDefaultImpl;
import com.zj.w3x.W3X;

public class MainTest {
	
	public static Logger logger = LoggerFactory.getLogger(MainTest.class);
	
	@Test
	public void testAll() throws IOException, ClassNotFoundException, SQLException {
		W3X xxx = new W3X();
		xxx.addUrls(Arrays.asList(W3X.DEFAULT_URLS));
		xxx.setProcessHandler(new ProcessHandlerDefaultImpl());
		xxx.start();
	}
}
