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


    /**
     * Insert a new key and its associated value into the B+ tree.
     * @param index the index of the child node, if index == -1, insert the key to the leftmost
     * @param key the new key
     * @param leftChild the child node contains keys less than the new key
     * @param rightChild the child node contains keys greater than the new key
     */
    private void insertAt(int index, TKey key, Node<TKey> leftChild, Node<TKey> rightChild) {
        // insert the new key
        this.setKey(index, key);
        if (!this.children.contains(leftChild))
            this.setChild(index, leftChild);
        this.setChild(index + 1, rightChild);
    }

    /**
     * When splits a internal node, the middle key is kicked out and be pushed to parent node.
     * @return
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

    /**
     * Push up the middle key of the current node after splitting to parent node.
     * @param key the middle key after splitting
     * @param leftChild the left node after splitting, it will be set as the child of current node's parent
     * @param rightNode the right node after splitting, it will be set as the child of current node's parent
     * @return
     */
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

    /**
     * When deletes a key from a internal node, the key is just removed from the node.
     * @param index the index of the key which should be deleted
     * @return
     */
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