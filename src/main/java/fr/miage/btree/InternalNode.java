package fr.miage.btree;

import com.fasterxml.jackson.annotation.JsonView;

import java.util.ArrayList;
import java.util.List;

public class InternalNode <TKey extends Comparable<TKey>> extends Node<TKey> {
    @JsonView(Views.Public.class)
    protected List<Node> children;

    public InternalNode() {
        this.keys = new ArrayList<TKey>();
        this.children = new ArrayList<Node>();
    }

    @SuppressWarnings("unchecked")
    public Node<TKey> getChild(int index) {
        return (Node<TKey>)this.children.get(index);
    }

    public void addChild( Node<TKey> child) {
        this.children.add(child);
        if (child != null)
            child.setParent(this);
    }

    public void setChild(int index, Node<TKey> child) {
        this.children.add(index,child);
        if (child != null)
            child.setParent(this);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.InternalNode;
    }

    @Override
    public int search(TKey key) {
        int index = 0;
        for (index = 0; index < this.getKeyCount(); index++) {
            int cmp = this.getKey(index).compareTo(key);
            if (cmp == 0) {
                return index + 1;
            }
            else if (cmp > 0) {
                return index;
            }
        }

        return index;
    }


    /* The codes below are used to support insertion operation */

    private void insertAt(int index, TKey key, Node<TKey> leftChild, Node<TKey> rightChild) {
        // insert the new key
        this.setKey(index, key);
        if (!this.children.contains(leftChild))
            this.setChild(index, leftChild);
        this.setChild(index + 1, rightChild);
    }

    /**
     * When splits a internal node, the middle key is kicked out and be pushed to parent node.
     */
    @Override
    protected Node<TKey> split() {
        int midIndex = this.getMiddleIndex();

        InternalNode<TKey> newRNode = new InternalNode<TKey>();

        // move keys and children to the new right node
       while(this.keys.size() > midIndex) {
            TKey movingKey = this.getKey(midIndex);
            newRNode.addKey( movingKey);
            this.keys.remove(movingKey);

            Node<TKey> movingChild = this.getChild(midIndex + 1);
            movingChild.setParent(newRNode);
            newRNode.addChild(movingChild);
            this.children.remove(movingChild);
        }
        newRNode.keys.remove(0);

        return newRNode;
    }

    @Override
    protected Node<TKey> pushUpKey(TKey key, Node<TKey> leftChild, Node<TKey> rightNode) {
        // find the target position of the new key
        int index = this.search(key);

        // insert the new key
        this.insertAt(index, key, leftChild, rightNode);

        // check whether current node need to be split
        if (this.isOverflow()) {
            return this.dealOverflow();
        }
        else {
            return this.getParent() == null ? this : null;
        }
    }

    /* The codes below are used to support delete operation */

    private void deleteAt(int index) {
        this.keys.remove(index);
        this.children.remove(index + 1 );
    }


    @Override
    protected void processChildrenTransfer(Node<TKey> borrower, Node<TKey> lender, int borrowIndex) {
        int borrowerChildIndex = 0;
        while (borrowerChildIndex < this.getKeyCount() + 1 && this.getChild(borrowerChildIndex) != borrower)
            ++borrowerChildIndex;

        if (borrowIndex == 0) {
            // borrow a key from right sibling
            TKey upKey = borrower.transferFromSibling(this.getKey(borrowerChildIndex), lender, borrowIndex);
            this.setKey(borrowerChildIndex, upKey);
        }
        else {
            // borrow a key from left sibling
            TKey upKey = borrower.transferFromSibling(this.getKey(borrowerChildIndex - 1), lender, borrowIndex);
            this.setKey(borrowerChildIndex - 1, upKey);
        }
    }

    @Override
    protected Node<TKey> processChildrenFusion(Node<TKey> leftChild, Node<TKey> rightChild) {
        int index = 0;
        while (index < this.getKeyCount() && this.getChild(index) != leftChild)
            index++;
        TKey sinkKey = this.getKey(index);

        // merge two children and the sink key into the left child node
        leftChild.fusionWithSibling(sinkKey, rightChild);

        // remove the sink key, keep the left child and abandon the right child
        this.deleteAt(index);

        // check whether need to propagate borrow or fusion to parent
        if (this.isUnderflow()) {
            if (this.getParent() == null) {
                // current node is root, only remove keys or delete the whole root node
                if (this.getKeyCount() == 0) {
                    leftChild.setParent(null);
                    return leftChild;
                }
                else {
                    return null;
                }
            }

            return this.dealUnderflow();
        }

        return null;
    }


    @Override
    protected void fusionWithSibling(TKey sinkKey, Node<TKey> rightSibling) {
        InternalNode<TKey> rightSiblingNode = (InternalNode<TKey>)rightSibling;

        int j = this.getKeyCount();
        this.setKey(j++, sinkKey);

        for (int i = 0; i < rightSiblingNode.getKeyCount(); i++) {
            this.setKey(j + i, rightSiblingNode.getKey(i));
        }
        for (int i = 0; i < rightSiblingNode.getKeyCount() + 1; i++) {
            this.setChild(j + i, rightSiblingNode.getChild(i));
        }
        this.setRightSibling(rightSiblingNode.rightSibling);
        if (rightSiblingNode.rightSibling != null)
            rightSiblingNode.rightSibling.setLeftSibling(this);
    }

    @Override
    protected TKey transferFromSibling(TKey sinkKey, Node<TKey> sibling, int borrowIndex) {
        InternalNode<TKey> siblingNode = (InternalNode<TKey>)sibling;

        TKey upKey = null;
        if (borrowIndex == 0) {
            // borrow the first key from right sibling, append it to tail
            int index = this.getKeyCount();
            this.setKey(index, sinkKey);
            this.setChild(index + 1, siblingNode.getChild(borrowIndex));

            upKey = siblingNode.getKey(0);
            siblingNode.deleteAt(borrowIndex);
        }
        else {
            // borrow the last key from left sibling, insert it to head
            this.insertAt(0, sinkKey, siblingNode.getChild(borrowIndex + 1), this.getChild(0));
            upKey = siblingNode.getKey(borrowIndex);
            siblingNode.deleteAt(borrowIndex);
        }

        return upKey;
    }
}