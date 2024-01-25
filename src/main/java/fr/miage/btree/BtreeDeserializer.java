package fr.miage.btree;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class BtreeDeserializer extends JsonDeserializer<Btree<String, String>> {
	
	@Override
	public Btree<String, String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
		
		if (rootNode.isArray() && rootNode.size() == 1) {
			JsonNode treeNode = rootNode.get(0).get("root");
			
			Btree<String, String> bplustree = new Btree<>();
			Node<String> root = parseNode(treeNode);
			
			if (root != null) {
				bplustree.setRoot(root);
				return bplustree;
			}
		}
		
		throw new IOException("Invalid JSON format for Bplustree.");
	}
	
	private Node<String> parseNode(JsonNode node) {
		String nodeType = node.get("nodeType").asText();
		JsonNode keysNode = node.get("keys");
		
		Node<String> parsedNode = null;
		
		if (nodeType.equals("InternalNode"))
			parsedNode = new InternalNode<>();
		else if (nodeType.equals("LeafNode"))
			parsedNode = new LeafNode<>();
		
		if (parsedNode != null) {
			for (JsonNode keyNode : keysNode)
				parsedNode.addKey(keyNode.asText());
			
			if (node.has("children")) {
				JsonNode childrenNode = node.get("children");
				if (childrenNode.isArray()) {
					for (JsonNode childNode : childrenNode) {
						Node<String> child = parseNode(childNode);
						if (child != null) {
							if (nodeType.equals("LeafNode")) {
								JsonNode valuesNode = childNode.get("values");
								if (valuesNode.isArray()) {
									LeafNode<String, String> leafNode = (LeafNode<String, String>) child;
									for (JsonNode valueNode : valuesNode)
										leafNode.addValue(valueNode.asText());
								}
							}
							((InternalNode<String>) parsedNode).addChild(child);
						}
					}
				}
			}
		}
		return parsedNode;
	}
}
