package main;

import java.util.ArrayList;

/**
 * CCBoundingVolumeHierarchy.java
 * 
 * Bounding volume hierarchy collision checking algorithm.
 * 
 * @author Jared
 *
 */
public class CCBoundingVolumeHierarchy extends CollisionChecker{

    public class Node{

        AABB aabb;
        AABB aabbMargin;
        Collider coll;
        Node parent;
        Node left;
        Node right;
        boolean branchChecked;

        Node(){

            aabb = new AABB();
            aabbMargin = null;
            coll = null;
            parent = null;
            left = null;
            right = null;
            branchChecked = false;
        }

        boolean isLeaf() {
            return left == null;
        }
    }

    //Private variables and Functions
    private Node treeRoot;
    private ArrayList<Collision> overlapPairs;
    private ArrayList<Node> invalidNodes;
    private final int AABB_MARGIN = 80;
    
    
	public CCBoundingVolumeHierarchy(ArrayList<Collider> colliders){
		super(colliders);
        treeRoot = null;
        invalidNodes = new ArrayList<Node>();
        overlapPairs = new ArrayList<Collision>();
	}

	public ArrayList<Collision> checkCollisions(){
		
        overlapPairs.clear();

        // No root or root is leaf, return
        if(treeRoot == null || treeRoot.isLeaf()){
            return overlapPairs;
        }

        // Reset branch checked flags for entire tree
        resetBranchChecked(treeRoot);

        // Check tree recusively
        checkOverlap(treeRoot.left, treeRoot.right);

        return overlapPairs;
	}

    private void addNode(Node myNode, Node myParent){

        // Parent is leaf
        if(myParent.isLeaf()){

            // Create new branch node
            Node newNode = new Node();
            newNode.parent = myParent.parent;
            newNode.left = myNode;
            newNode.right = myParent;

            // Update grandparent child
            if(myParent.parent != null){
                if(myParent.parent.left == myParent)
                    myParent.parent.left = newNode;
                else
                    myParent.parent.right = newNode;
            }

            // No grandparent, parent is root
            else
                treeRoot = newNode;

            // Set child parents
            newNode.left.parent = newNode;
            newNode.right.parent = newNode;

            updateAABB(newNode);
        }

        // Parent is branch
        else{
            // Compute size difference if node is added to either child
            AABB box	= myNode.aabb;
            AABB boxLeft	= myParent.left.aabb;
            AABB boxRight	= myParent.right.aabb;

            //Original areas of left and right AABBs
            float boxLeftArea = (boxLeft.upperBoundX - boxLeft.lowerBoundX) * (boxLeft.upperBoundY - boxLeft.lowerBoundY);
            float boxRightArea = (boxRight.upperBoundX - boxRight.lowerBoundX) * (boxRight.upperBoundY - boxRight.lowerBoundY);

            //Calculate new potential areas using min max
            float boxLeftNewArea = (Math.max(box.upperBoundX, boxLeft.upperBoundX) - Math.min(box.lowerBoundX, boxLeft.lowerBoundX))
                    * (Math.max(box.upperBoundY, boxLeft.upperBoundY) - Math.min(box.lowerBoundY, boxLeft.lowerBoundY));

            float boxRightNewArea = (Math.max(box.upperBoundX, boxRight.upperBoundX) - Math.min(box.lowerBoundX, boxRight.lowerBoundX))
                    * (Math.max(box.upperBoundY, boxRight.upperBoundY) - Math.min(box.lowerBoundY, boxRight.lowerBoundY));

            //Find the difference between new potential area and old area.
            float areaDiff1 = boxLeftNewArea - boxLeftArea;
            float areaDiff2 = boxRightNewArea - boxRightArea;

            // Add node to child that has less area increase
            if(areaDiff1 < areaDiff2)
                addNode(myNode, myParent.left);
            else
                addNode(myNode, myParent.right);

            // Update parent
            //updateAABB(myParent);
        }
    }

    private void removeCollider(Collider myCollider, Node myNode){

        if(myNode.left.isLeaf()){
            if(myNode.left.coll == myCollider){
                removeNode(myNode.left);
                return;
            }
        }
        else
            removeCollider(myCollider, myNode.left);

        if(myNode.right.isLeaf()){
            if(myNode.right.coll == myCollider)
                removeNode(myNode.right);
            return;
        }
        else
            removeCollider(myCollider, myNode.right);
    }

    private void removeNode(Node myNode){

        //Tree is empty
        if (treeRoot == null){
            return;
        }

        // Node is root
        if(myNode.parent == null){
            treeRoot = null;

            return;
        }

        // Get sibling
        Node sibling;

        if(myNode.parent.left == myNode){
            sibling = myNode.parent.right;
        }
        else{
            sibling = myNode.parent.left;
        }


        // Node has grandparent
        if(myNode.parent.parent != null){
            sibling.parent = myNode.parent.parent;

            // Replace grandparent child
            if(sibling.parent.left == myNode.parent) {
                sibling.parent.left = sibling;
            }
            else{
                sibling.parent.right = sibling;
            }
        }

        // Parent is root
        else{
            treeRoot = sibling;
            sibling.parent = null;
        }

        myNode.parent = null;
    }

    private void updateAABB(Node myNode){
    	
        // Branch node, take min/max of child AABBs
        if(!myNode.isLeaf()) {
        	
            // Use enlarged AABB for children that have them

            //X
            float clX1 = myNode.left.aabbMargin		!= null	? myNode.left.aabbMargin.lowerBoundX	: myNode.left.aabb.lowerBoundX;
            float clX2 = myNode.right.aabbMargin	!= null	? myNode.right.aabbMargin.lowerBoundX	: myNode.right.aabb.lowerBoundX;
            float cuX1 = myNode.left.aabbMargin		!= null	? myNode.left.aabbMargin.upperBoundX	: myNode.left.aabb.upperBoundX;
            float cuX2 = myNode.right.aabbMargin	!= null	? myNode.right.aabbMargin.upperBoundX	: myNode.right.aabb.upperBoundX;

            myNode.aabb.lowerBoundX = Math.min(clX1, clX2);
            myNode.aabb.upperBoundX = Math.max(cuX1, cuX2);

            //Y
            float clY1 = myNode.left.aabbMargin		!= null	? myNode.left.aabbMargin.lowerBoundY	: myNode.left.aabb.lowerBoundY;
            float clY2 = myNode.right.aabbMargin	!= null ? myNode.right.aabbMargin.lowerBoundY	: myNode.right.aabb.lowerBoundY;
            float cuY1 = myNode.left.aabbMargin		!= null ? myNode.left.aabbMargin.upperBoundY	: myNode.left.aabb.upperBoundY;
            float cuY2 = myNode.right.aabbMargin	!= null ? myNode.right.aabbMargin.upperBoundY	: myNode.right.aabb.upperBoundY;

            myNode.aabb.lowerBoundY = Math.min(clY1, clY2);
            myNode.aabb.upperBoundY = Math.max(cuY1, cuY2);

        }
        
        if(myNode.parent != null)
        	updateAABB(myNode.parent);
    }

    private void resetBranchChecked(Node myNode){
        myNode.branchChecked = false;

        if(!myNode.isLeaf()){
            if(!myNode.left.isLeaf()) resetBranchChecked(myNode.left);
            if(!myNode.right.isLeaf()) resetBranchChecked(myNode.right);
        }
    }

    private void checkOverlap(Node node1, Node node2){

        if (!node1.isLeaf() && !node1.branchChecked){
            checkOverlap(node1.left, node1.right);
            node1.branchChecked = true;
        }

        if (!node2.isLeaf() && !node2.branchChecked){
            checkOverlap(node2.left, node2.right);
            node2.branchChecked = true;
        }

        //CheckOverlap
        if (!node1.aabb.isOverlapping(node2.aabb)){
            return;
        }

        // Two leaf nodes
        if(node1.isLeaf() && node2.isLeaf()){

            // Add to list if overlapping
            overlapPairs.add(new Collision(node1.coll, node2.coll));
            return;
        }

        // Check overlaps in children
        if(!node1.isLeaf() && !node1.branchChecked){
            checkOverlap(node1.left, node1.right);
            node1.branchChecked = true;
        }

        if(!node2.isLeaf() && !node2.branchChecked){
            checkOverlap(node2.left, node2.right);
            node2.branchChecked = true;
        }


        // Check overlaps between children
        if(!node1.isLeaf()){
            if(!node2.isLeaf()){
                // Both nodes are branches, check them against each other
                checkOverlap(node1.left, node2.left);
                checkOverlap(node1.left, node2.right);
                checkOverlap(node1.right, node2.left);
                checkOverlap(node1.right, node2.right);
            }
            else{
                checkOverlap(node1.left, node2);
                checkOverlap(node1.right, node2);
            }
        }
        else{
            checkOverlap(node1, node2.left);
            checkOverlap(node1, node2.right);
        }
    }
    
    public void update(){

        if(treeRoot == null)
            return;

        // Clear invalid nodes
        invalidNodes.clear();

        // Update tree
        updateNode(treeRoot);
        
        // Re-insert invaild nodes
        for(Node myNode : invalidNodes){

            // Update large AABB
            myNode.aabbMargin.lowerBoundX = myNode.aabb.lowerBoundX - AABB_MARGIN;
            myNode.aabbMargin.upperBoundX = myNode.aabb.upperBoundX + AABB_MARGIN;

            myNode.aabbMargin.lowerBoundY = myNode.aabb.lowerBoundY - AABB_MARGIN;
            myNode.aabbMargin.upperBoundY = myNode.aabb.upperBoundY + AABB_MARGIN;

            // Remove and add again
            removeNode(myNode);
            addNode(myNode, treeRoot);
        }
    }

    private void updateNode(Node myNode){

        // Node is leaf
        if(myNode.isLeaf()){

            //updateAABB(myNode);
            // If AABB has moved outside margin, mark it invalid
            if(		myNode.aabb.lowerBoundX < myNode.aabbMargin.lowerBoundX ||
                    myNode.aabb.lowerBoundY < myNode.aabbMargin.lowerBoundY ||
                    myNode.aabb.upperBoundX > myNode.aabbMargin.upperBoundX ||
                    myNode.aabb.upperBoundY > myNode.aabbMargin.upperBoundY){

                invalidNodes.add(myNode);
            }
        }

        // Node is branch, update children
        else{
            updateNode(myNode.left);
            updateNode(myNode.right);
        }
    }
    
    public void add(Collider myCollider){

        // Create node for this collider
        Node myNode = new Node();
        myNode.coll = myCollider;
        myNode.aabb = myCollider.getAABB();
        
        myNode.aabbMargin = new AABB();
        myNode.aabbMargin.lowerBoundX = myNode.aabb.lowerBoundX - AABB_MARGIN;
        myNode.aabbMargin.upperBoundX = myNode.aabb.upperBoundX + AABB_MARGIN;

        myNode.aabbMargin.lowerBoundX = myNode.aabb.lowerBoundY - AABB_MARGIN;
        myNode.aabbMargin.upperBoundX = myNode.aabb.upperBoundY + AABB_MARGIN;
        
        // First node
        if(treeRoot == null)
            treeRoot = myNode;
        else
            addNode(myNode, treeRoot);
    }

    public void remove(Collider myCollider) {

        if(treeRoot == null) {
            return;
        }

        // Root node contains collider
        if(treeRoot.isLeaf() && treeRoot.coll == myCollider)
            removeNode(treeRoot);

        // Search for and remove collider node
        else
            removeCollider(myCollider, treeRoot);
    }
    
    public Node getRoot(){
    	return treeRoot;
    }
}
