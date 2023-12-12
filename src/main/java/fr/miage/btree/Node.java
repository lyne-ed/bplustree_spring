package fr.miage.btree;

import com.fasterxml.jackson.annotation.JsonView;

import java.util.ArrayList;
import java.util.List;

public abstract class Node<TKey extends Comparable<TKey>> {
    protected final static int INNER_ORDER = 5;
    @JsonView(Views.Public.class)
    protected List<TKey> keys;
    protected Node<TKey> parentNode;
    protected Node<TKey> leftSibling;
    protected Node<TKey> rightSibling;


    protected Node() {
        this.keys = new ArrayList<TKey>();
        this.parentNode = null;
        this.leftSibling = null;
        this.rightSibling = null;
    }

    public int getKeyCount() {
        return this.keys.size();
    }

    @SuppressWarnings("unchecked")
    public TKey getKey(int index) {
        return (TKey)this.keys.get(index);
    }

    public void addKey(TKey key) {
        this.keys.add(key);
    }

    public void setKey(int index, TKey key) {
        this.keys.add(index,key);
    }

    public Node<TKey> getParent() {
        return this.parentNode;
    }

    public void setParent(Node<TKey> parent) {
        this.parentNode = parent;
    }

    @JsonView(Views.Public.class)
    public abstract NodeType getNodeType();


    /**
     * Search a key on current node, if found the key then return its position,
     * otherwise return -1 for a leaf node, 
     * return the child node index which should contain the key for a internal node.
     */
    public abstract int search(TKey key);


    public int getMiddleIndex() {
        return (INNER_ORDER - 1) / 2;
    }

    /* The codes below are used to support insertion operation */

    public boolean isOverflow() {
        return this.getKeyCount() > INNER_ORDER-1;
    }

    public Node<TKey> dealOverflow() {
        int midIndex = getMiddleIndex();
        TKey upKey = this.getKey(midIndex);

        Node<TKey> newRNode = this.split();

        // connect new sub-tree if new root is defined
        if (this.getParent() == null) {
            this.setParent(new InternalNode<TKey>());
        }

        // attach new right node to parent
        newRNode.setParent(this.getParent());

        // maintain links of sibling nodes
        newRNode.setLeftSibling(this);
        if (this.getRightSibling() != null) {
            newRNode.setRightSibling(this.rightSibling);
            this.getRightSibling().setLeftSibling(newRNode);
        }
        this.setRightSibling(newRNode);

        // push up a key to parent internal node
        return this.getParent().pushUpKey(upKey, this, newRNode);
    }

    protected abstract Node<TKey> split();

    protected abstract Node<TKey> pushUpKey(TKey key, Node<TKey> leftChild, Node<TKey> rightNode);


    /* The codes below are used to support deletion operation */

    public boolean isUnderflow() {
        return this.getKeyCount() < (INNER_ORDER / 2);
    }

    public boolean canLendAKey() {
        return this.getKeyCount() > (INNER_ORDER / 2);
    }

    public Node<TKey> getLeftSibling() {
        if (this.leftSibling != null && this.leftSibling.getParent() == this.getParent())
            return this.leftSibling;
        return null;
    }

    public void setLeftSibling(Node<TKey> sibling) {
        this.leftSibling = sibling;
    }

    public Node<TKey> getRightSibling() {
        if (this.rightSibling != null && this.rightSibling.getParent() == this.getParent())
            return this.rightSibling;
        return null;
    }

    public void setRightSibling(Node<TKey> silbling) {
        this.rightSibling = silbling;
    }

    public Node<TKey> dealUnderflow() {
        if (this.getParent() == null)
            return null;

        // try to borrow a key from sibling
        Node<TKey> leftSibling = this.getLeftSibling();
        if (leftSibling != null && leftSibling.canLendAKey()) {
            this.getParent().processChildrenTransfer(this, leftSibling, leftSibling.getKeyCount() - 1);
            return null;
        }

        Node<TKey> rightSibling = this.getRightSibling();
        if (rightSibling != null && rightSibling.canLendAKey()) {
            this.getParent().processChildrenTransfer(this, rightSibling, 0);
            return null;
        }

        // Can not borrow a key from any sibling, then do fusion with sibling
        if (leftSibling != null) {
            return this.getParent().processChildrenFusion(leftSibling, this);
        }
        else {
            return this.getParent().processChildrenFusion(this, rightSibling);
        }
    }


    protected abstract void processChildrenTransfer(Node<TKey> borrower, Node<TKey> lender, int borrowIndex);

    protected abstract Node<TKey> processChildrenFusion(Node<TKey> leftChild, Node<TKey> rightChild);

    protected abstract void fusionWithSibling(TKey sinkKey, Node<TKey> rightSibling);

    protected abstract TKey transferFromSibling(TKey sinkKey, Node<TKey> sibling, int borrowIndex);

}