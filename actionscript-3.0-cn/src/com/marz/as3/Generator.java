package com.marz.as3;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.webkit.dom.HTMLDocumentImpl;

public class Generator {

	public static void main(String[] args) {
		File path = new File("actionscript-3.0-cn.docset/Contents/Resources/Documents/");
		path.mkdirs();
		
		Connection connection = null;
		try {
			File dbFile = new File("actionscript-3.0-cn.docset/Contents/Resources/docSet.dsidx");
			if(dbFile.exists()){
				dbFile.delete();
			}
			
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:actionscript-3.0-cn.docset/Contents/Resources/docSet.dsidx");
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.
			
			statement.executeUpdate("CREATE TABLE searchIndex(id INTEGER PRIMARY KEY, name TEXT, type TEXT, path TEXT)");
			statement.executeUpdate("CREATE UNIQUE INDEX anchor ON searchIndex (name, type, path)");

			statement.executeUpdate("INSERT OR IGNORE INTO searchIndex(name, type, path) VALUES (\"$name\",\"$class\",\"$href\")");
			statement.executeUpdate("INSERT OR IGNORE INTO searchIndex(name, type, path) VALUES (\"$name\",\"Function\",\"$href\")");
			
//			Document dom = new HTMLDocumentImpl();
//			Element element = dom.getDocumentElement();
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e);
			}
		}
	}

}
