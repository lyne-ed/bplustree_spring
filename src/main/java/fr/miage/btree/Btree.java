package fr.miage.btree;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = BtreeDeserializer.class)
public class Btree <TKey extends Comparable<TKey>, TValue> {

    @JsonView(Views.Public.class)
    private Node<TKey> root;

    public Btree() {
        this.root = new LeafNode<TKey, TValue>();
    }

    /**
     * Insert a new key and its associated value into the B+ tree.
     */
    public void insert(TKey key, TValue value) {
        LeafNode<TKey, TValue> leaf = this.findLeafNodeShouldContainKey(key);
        leaf.insertKey(key, value);

        if (leaf.isOverflow()) {
            Node<TKey> n = leaf.dealOverflow();
            if (n != null)
                this.root = n;
        }
    }

    /**
     * Search a key value on the tree and return its associated value.
     */
    public TValue search(TKey key) {
        LeafNode<TKey, TValue> leaf = this.findLeafNodeShouldContainKey(key);

        int index = leaf.search(key);
        return (index == -1) ? null : leaf.getValue(index);
    }

    /**
     * Delete a key and its associated value from the tree.
     */
    public void delete(TKey key) {
        LeafNode<TKey, TValue> leaf = this.findLeafNodeShouldContainKey(key);

        if (leaf.delete(key) && leaf.isUnderflow()) {
            Node<TKey> n = leaf.dealUnderflow();
            if (n != null)
                this.root = n;
        }
    }

    /**
     * Search the leaf node which should contain the specified key
     */
    @SuppressWarnings("unchecked")
    private LeafNode<TKey, TValue> findLeafNodeShouldContainKey(TKey key) {
        Node<TKey> node = this.root;
        while (node.getNodeType() == NodeType.InternalNode) {
            node = ((InternalNode<TKey>)node).getChild( node.search(key) );
        }

        return (LeafNode<TKey, TValue>)node;
    }

    @Override
    public String toString() {
        return this.root.toString();
    }
    
    public void setRoot(Node<TKey> root) {
        this.root = root;
    }
}
