package se.fishtank.css.selectors.dom2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.*;

import se.fishtank.css.selectors.NodeSelectorException;
import se.fishtank.css.selectors.Selector;
import se.fishtank.css.selectors.generic.NodeHelper;
import se.fishtank.css.util.Assert;

public class DOMNodeHelper implements NodeHelper<Node> {


	/** The root node (document or element). */
	private final Node root;

	private final boolean caseSensitive;

	/**
	 * Create a new instance.
	 * 
	 * @param root The root node. Must be a document or element node.
	 */
	public DOMNodeHelper(final Node root) {
		Assert.notNull(root, "root is null!");
		short nodeType = root.getNodeType();
		Assert.isTrue(nodeType == Node.DOCUMENT_NODE ||
				nodeType == Node.ELEMENT_NODE, "root must be a document or element node!");
		this.root = root;

		Document doc = (root instanceof Document) ? (Document) root : root.getOwnerDocument();
		caseSensitive = !doc.createElement("a").isEqualNode(doc.createElement("A"));
	}


	@Override
	public String getValue(final Node node) {
		return node.getNodeValue().trim();
	}

	@Override
	public boolean hasAttribute(final Node node, final String name) {
		NamedNodeMap map = node.getAttributes();
		return map.getNamedItem(name)!=null;
	}

	@Override
	public String getAttribute(final Node node, final String name) {
		NamedNodeMap map = node.getAttributes();
		return map.getNamedItem(name).getNodeValue().trim();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Index getIndexInParent(final Node node, final boolean byType) {
		String type = byType ? getName(node) : Selector.UNIVERSAL_TAG;

		List<? extends Node> children;
		Node parent = node.getParentNode();
		if (parent==null)
			children = Collections.EMPTY_LIST;
		else
			children = getChildNodes(parent, type);

		return new Index(children.indexOf(node), children.size());
	}

	@Override
	public Collection<? extends Node> getDescendentNodes(final Node node) {
		NodeList nodes;
		if (node.getNodeType() == Node.DOCUMENT_NODE)
			nodes = ((Document) node).getElementsByTagName(Selector.UNIVERSAL_TAG);
		else if (node.getNodeType() == Node.ELEMENT_NODE)
			nodes = ((Element) node).getElementsByTagName(Selector.UNIVERSAL_TAG);
		else
			throw new RuntimeException(new NodeSelectorException("Only document and element nodes allowed!"));

		return convert(nodes);
	}

	@Override
	public List<? extends Node> getChildNodes(final Node node) {
		return getChildNodes(node, "*");
	}

	@SuppressWarnings("unchecked")
	protected List<? extends Node> getChildNodes(final Node node, final String withName) {
		if (node==null)
			return Collections.EMPTY_LIST;

		if (Selector.UNIVERSAL_TAG.equals(withName))
			return convert(node.getChildNodes());

		ArrayList<Node> result = new ArrayList<Node>();

		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (nameMatches(n, withName))
				result.add(n);
		}

		return result;
	}

	@Override
	public boolean isEmpty(final Node node) {
		return node.getChildNodes().getLength()==0;
	}

	@Override
	public String getName(final Node n) {
		return n.getNodeName();
	}

	@Override
	public Node getNextSibling(final Node node) {
		Index index = getIndexInParent(node, false);
		if (index.index < (index.size-1))
			return getChildNodes(node.getParentNode()).get(index.index+1);
		else
			return null;
	}

	@Override
	public boolean nameMatches(final Node n, final String name) {
		if (name.equals(Selector.UNIVERSAL_TAG))
			return true;

		if (caseSensitive)
			return name.equals(getName(n));
		else
			return name.equalsIgnoreCase(getName(n));
	}

	private List<Node> convert(final NodeList nodeList) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			if (n.getNodeType()==Node.ELEMENT_NODE)
				result.add(n);
		}

		return result;
	}


	@Override
	public Node getRoot() {
		if (root.getNodeType() == Node.DOCUMENT_NODE) {
			// Get the single element child of the document node.
			// There could be a doctype node and comment nodes that we must skip.
			NodeList children = root.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
				if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
					return children.item(i);
			Assert.isTrue(false, "there should be a root element!");
			return null;
		} else {
			Assert.isTrue(root.getNodeType() == Node.ELEMENT_NODE, "root must be a document or element node!");
			return root;
		}
	}

}
