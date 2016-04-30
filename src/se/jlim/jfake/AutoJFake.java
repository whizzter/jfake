/*
 * The MIT License
 *
 * Copyright 2016 Jonas Lund.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.jlim.jfake;

import se.jlim.jfake.target.JFakeTarget;
import se.jlim.jfake.target.JPATarget;
import se.jlim.jfake.target.JDBCTarget;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

/**
 * Automatically runs JFake if WEB-INF/jfake.txt exists in a Web Application.
 * The purpose of this is to enable automatic creation of table data in scenarios
 * where drop-n-create is used with JPA and we want to get the data filled in automatically.
 * 
 * @author Jonas Lund
 */
@WebListener
public class AutoJFake implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String autoName = AutoJFake.class.getClass().getName();
		System.out.println("AutoName:" + autoName);
		try (InputStream is = sce.getServletContext().getResourceAsStream("/WEB-INF/auto.jfake")) {

			if (is == null) {
				System.out.println("xfake.xml not found in WEB-INF , not initialized");
				return;
			}

			JFake jf = JFake.fakeFromStream(is);

			String dsString=jf.getProp("@datasource", String.class);
			if (dsString!=null) {
				InitialContext ic = new InitialContext();
				DataSource ds = (DataSource) ic.lookup(dsString);

				JFakeTarget target = new JDBCTarget(ds);
				jf.build(target);
			} else {
				System.out.println("AutoName:" + autoName);
				JFakeTarget target = new JPATarget();
				jf.build(target);
			}

		} catch (NamingException | IOException | SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
