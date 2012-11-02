package edu.berkeley.path.beats.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SchemaTest {
	private static Logger logger = Logger.getLogger(SchemaTest.class);

	@Test
	public void compareSchemas() throws ParserConfigurationException, SAXException, IOException {
		ClassLoader classLoader = SchemaTest.class.getClassLoader();
		logger.info("Loading XML schema");
		XMLSchema xmlschema = new XMLSchema(classLoader.getResourceAsStream("sirius.xsd"));
		logger.info("Loading DB schema");
		DBSchema dbschema = new DBSchema(new FileInputStream("data" + File.separator + "sirius-db-schema.xml"));
		logger.info("Loading matching file");
		MatchingSchema mschema = new MatchingSchema(classLoader.getResourceAsStream("matches.xml"));
		logger.info("Validating DB schema");
		dbschema.validate(mschema);
		logger.info("Validating XML schema");
		xmlschema.validate(mschema);
	}

	private static abstract class Schema {
		protected org.w3c.dom.Document doc;

		public Schema(InputStream is) throws ParserConfigurationException, SAXException, IOException {
			DocumentBuilder docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = docbuilder.parse(is);
		}

		public abstract void validate(MatchingSchema mschema);
	}

	private static class MatchingSchema {
		private Set<Pair> xmlelems;
		private Set<Pair> dbelems;
		public MatchingSchema(InputStream is) throws ParserConfigurationException, SAXException, IOException {
			DocumentBuilder docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			org.w3c.dom.Document doc = docbuilder.parse(is);
			xmlelems = new HashSet<Pair>();
			dbelems = new HashSet<Pair>();
			for (Node parent_node = doc.getDocumentElement().getFirstChild(); null != parent_node; parent_node = parent_node.getNextSibling()) {
				if (Node.ELEMENT_NODE == parent_node.getNodeType() && "group".equals(parent_node.getNodeName())) {
					process_group((Element) parent_node);
				}
			}
		}

		public boolean isValidDBPair(String table, String column) {
			return dbelems.contains(new Pair(table, column));
		}

		public boolean isValidXMLPair(String parent, String child) {
			return xmlelems.contains(new Pair(parent, child));
		}

		private void process_group(Element group) {
			String table_name = group.getAttribute("table");
			String elem_name = group.getAttribute("element");
			for (Node node = group.getFirstChild(); null != node; node = node.getNextSibling()) {
				if (Node.ELEMENT_NODE == node.getNodeType() && "match".equals(node.getNodeName())) {
					Element match = (Element) node;
					xmlelems.add(new Pair(elem_name, match.getAttribute("child")));
					dbelems.add(new Pair(table_name, match.getAttribute("column")));
				}
			}
		}

		private static class Pair {
			String parent;
			String child;
			public Pair(String parent, String child) {
				this.parent = parent;
				this.child = child;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((child == null) ? 0 : child.hashCode());
				result = prime * result
						+ ((parent == null) ? 0 : parent.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Pair other = (Pair) obj;
				if (child == null) {
					if (other.child != null)
						return false;
				} else if (!child.equals(other.child))
					return false;
				if (parent == null) {
					if (other.parent != null)
						return false;
				} else if (!parent.equals(other.parent))
					return false;
				return true;
			}
		}
	}

	private static class XMLSchema extends Schema {
		XMLSchema(InputStream is) throws ParserConfigurationException, SAXException, IOException {
			super(is);
		}

		@Override
		public void validate(MatchingSchema mschema) {
			for (Node node = doc.getDocumentElement().getFirstChild(); null != node; node = node.getNextSibling()) {
				if (Node.ELEMENT_NODE == node.getNodeType() && node.getNodeName().endsWith(":element"))
					validate_element((Element) node, mschema);
			}
		}

		private void validate_element(Element elem, MatchingSchema mschema) {
			String name = elem.getAttribute("name");
			NodeList elist = elem.getElementsByTagName("xs:element");
			NodeList alist = elem.getElementsByTagName("xs:attribute");
			for (int i = 0; i < elist.getLength(); ++i) {
				String child_name = ((Element) elist.item(i)).getAttribute("ref");
				if (!mschema.isValidXMLPair(name, child_name))
					logger.error("XML: parent=" + name + ", child=" + child_name);
			}
			for (int i = 0; i < alist.getLength(); ++i) {
				String attr_name = ((Element) alist.item(i)).getAttribute("name");
				if (!mschema.isValidXMLPair(name, attr_name))
					logger.error("XML: parent=" + name + ", attribute=" + attr_name);
			}
		}
	}

	private class DBSchema extends Schema {
		public DBSchema(InputStream is) throws ParserConfigurationException, SAXException, IOException {
			super(is);
			ignored_columns = new HashSet<String>();
			for (String colname : new String[] {"created", "modified", "created_by", "modified_by", "modstamp"})
				ignored_columns.add(colname);
		}

		private Set<String> ignored_columns;

		@Override
		public void validate(MatchingSchema mschema) {
			for (Node node = doc.getDocumentElement().getFirstChild(); null != node; node = node.getNextSibling()) {
				if (Node.ELEMENT_NODE == node.getNodeType() && "table".equals(node.getNodeName()))
					validate_table((Element) node, mschema);
			}
		}

		private void validate_table(Element elem, MatchingSchema mschema) {
			String table_name = elem.getAttribute("name");
			for (Node node = elem.getFirstChild(); null != node; node = node.getNextSibling()) {
				if (Node.ELEMENT_NODE == node.getNodeType() && "column".equals(node.getNodeName())) {
					String column_name = ((Element) node).getAttribute("name");
					if (!ignored_columns.contains(column_name) && !mschema.isValidDBPair(table_name, column_name))
						logger.error("DB: table=" + table_name + ", column=" + column_name);
				}
			}
		}
	}
}
