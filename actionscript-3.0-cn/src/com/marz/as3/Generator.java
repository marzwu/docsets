package com.marz.as3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Generator {

	public static void main(String[] args) {
		String pathStr = "actionscript-3.0-cn.docset/Contents/Resources/Documents/";
		File path = new File(pathStr);
		path.mkdirs();

		Connection connection = null;
		try {
			File dbFile = new File(
					"actionscript-3.0-cn.docset/Contents/Resources/docSet.dsidx");
			if (dbFile.exists()) {
				dbFile.delete();
			}

			// create a database connection
			connection = DriverManager
					.getConnection("jdbc:sqlite:actionscript-3.0-cn.docset/Contents/Resources/docSet.dsidx");
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			statement
					.executeUpdate("CREATE TABLE searchIndex(id INTEGER PRIMARY KEY, name TEXT, type TEXT, path TEXT)");
			statement
					.executeUpdate("CREATE UNIQUE INDEX anchor ON searchIndex (name, type, path)");

			parseClasses(pathStr, statement);

			//
			ResultSet rs = statement
					.executeQuery("select path from searchIndex");

			while (rs.next()) {
				String url = rs.getString("path");
				File input = new File(pathStr + url);
				try {
					Document doc = Jsoup.parse(input, "UTF-8",
							"http://example.com/");
					Elements mainright = doc.select("div.mainright");
					doc.select("table.titleTable").remove();
					doc.select("div.maincontainer").remove();
					mainright.remove();
					// doc.empty();

					mainright.select("tr.showHideLinks").remove();
					mainright.select("div.showHideLinks").remove();
					// mainright.select("tr.hideInheritedProperty").attr("class",
					// "");
					mainright.select("tr.hideInheritedProperty").attr("class",
							"");
					mainright.select("tr.hideInheritedMethod")
							.attr("class", "");
					mainright.select("tr[class^=hideInherited]").attr("class",
							"");
					doc.body().append(mainright.toString());

					FileWriter fw = new FileWriter(pathStr + url, false);
					fw.write(doc.toString());
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// statement
			// .executeUpdate("INSERT OR IGNORE INTO searchIndex(name, type, path) VALUES (\"$name\",\"$class\",\"$href\")");
			// statement
			// .executeUpdate("INSERT OR IGNORE INTO searchIndex(name, type, path) VALUES (\"$name\",\"Function\",\"$href\")");

			// Document dom = new HTMLDocumentImpl();
			// Element element = dom.getDocumentElement();
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

	private static void parseClasses(String pathStr, Statement statement)
			throws SQLException {
		File input = new File(pathStr + "all-classes.html");
		try {
			Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

			Elements elements = doc.select("a[href]");

			Iterator<Element> iterator = elements.iterator();
			while (iterator.hasNext()) {
				Element next = iterator.next();
				if (next.attr("name").equals("ftr")) {
					// System.out.println(next.attr("title"));

					String url = next.attr("title");
					url = url.replace('.', '/');
					url += ".html";

					String name = next.text().replace("\u00a0", "").trim();

					statement
							.executeUpdate(String
									.format("INSERT OR IGNORE INTO searchIndex(name, type, path) VALUES (\"%s\",\"Class\",\"%s\")",
											name,
											/* next.attr("title"), */url));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
