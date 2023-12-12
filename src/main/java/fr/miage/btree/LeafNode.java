package fr.miage.btree;

import com.fasterxml.jackson.annotation.JsonView;

import java.util.ArrayList;
import java.util.List;

public class LeafNode<TKey extends Comparable<TKey>, TValue> extends Node<TKey> {
    @JsonView(Views.Public.class)
    private List<TValue> values;

    public LeafNode() {
        this.keys = new ArrayList<TKey>();
        this.values = new ArrayList<TValue>();
    }

    @SuppressWarnings("unchecked")
    public TValue getValue(int index) {
        return (TValue)this.values.get(index);
    }

    public void addValue(TValue value) {
        this.values.add(value);
    }

    public void setValue(int index, TValue value) {
        this.values.add(index, value);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.LeafNode;
    }

    @Override
    public int search(TKey key) {
        for (int i = 0; i < this.getKeyCount(); i++) {
            int cmp = this.getKey(i).compareTo(key);
            if (cmp == 0) {
                return i;
            }
            else if (cmp > 0) {
                return -1;
            }
        }

        return -1;
    }


    /* The codes below are used to support insertion operation */

    public void insertKey(TKey key, TValue value) {
        int index = 0;
        while (index < this.getKeyCount() && this.getKey(index).compareTo(key) < 0)
            index++;
        this.insertAt(index, key, value);
    }

    private void insertAt(int index, TKey key, TValue value) {
        this.setKey(index, key);
        this.setValue(index, value);
    }


    /**
     * When splits a leaf node, the middle key is kept on new node and be pushed to parent node.
     */
    @Override
    protected Node<TKey> split() {
        int midIndex = getMiddleIndex();

        LeafNode<TKey, TValue> newRNode = new LeafNode<TKey, TValue>();
        while (this.getKeyCount() > midIndex) {
            newRNode.insertKey(this.getKey(midIndex), this.getValue(midIndex));
            this.deleteAt(midIndex);
        }
        return newRNode;
    }

    @Override
    protected Node<TKey> pushUpKey(TKey key, Node<TKey> leftChild, Node<TKey> rightNode) {
        throw new UnsupportedOperationException();
    }

    /* The codes below are used to support deletion operation */

    public boolean delete(TKey key) {
        int index = this.search(key);
        if (index == -1)
            return false;

        this.deleteAt(index);
        return true;
    }

    private void deleteAt(int index) {
        this.keys.remove(index);
        this.values.remove(index);
    }

    @Override
    protected void processChildrenTransfer(Node<TKey> borrower, Node<TKey> lender, int borrowIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Node<TKey> processChildrenFusion(Node<TKey> leftChild, Node<TKey> rightChild) {
        throw new UnsupportedOperationException();
    }

    /**
     * Notice that the key sunk from parent is abandoned.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void fusionWithSibling(TKey sinkKey, Node<TKey> rightSibling) {
        LeafNode<TKey, TValue> siblingLeaf = (LeafNode<TKey, TValue>)rightSibling;

        for (int i = 0; i < siblingLeaf.getKeyCount(); i++) {
            this.addKey(siblingLeaf.getKey(i));
            this.addValue(siblingLeaf.getValue(i));
        }

        this.setRightSibling(siblingLeaf.rightSibling);
        if (siblingLeaf.rightSibling != null)
            siblingLeaf.rightSibling.setLeftSibling(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected TKey transferFromSibling(TKey sinkKey, Node<TKey> sibling, int borrowIndex) {
        LeafNode<TKey, TValue> siblingNode = (LeafNode<TKey, TValue>)sibling;

        this.insertKey(siblingNode.getKey(borrowIndex), siblingNode.getValue(borrowIndex));
        siblingNode.deleteAt(borrowIndex);

        return borrowIndex == 0 ? sibling.getKey(0) : this.getKey(0);
    }
}