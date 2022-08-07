package org.lengyu.algorithm.common;

import java.util.LinkedList;

public class TreeNode {
    public int val;
    public TreeNode left;
    public TreeNode right;

    public TreeNode() {}
    public TreeNode(int x) { val = x;}

    public TreeNode(int x, TreeNode left, TreeNode right) {
        val = x;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        LinkedList<TreeNode> queue = new LinkedList<>();
        StringBuilder s0 = new StringBuilder();

        s0.append('[');
        queue.add(this);

        while(!queue.isEmpty()) {
            int size = queue.size();
            boolean allNull = true;
            for(int i = 0; i < size; i++) {
                TreeNode t = queue.poll();
                if(t != null) {
                    s0.append(Integer.toString(t.val));
                    queue.add(t.left);
                    queue.add(t.right);
                    //此处主要是考虑最后一层都为null
                    if(allNull && ((t.left!=null||t.right!=null))) allNull=false;
                }else s0.append("null");
                if(i==size-1 && allNull) {
                    s0.append(']');
                    return s0.toString();
                }else s0.append(',');
            }
        }
        s0.append(']');
        return s0.toString();
    }

    public String toPrettyString() {
        return prettyPrintTree(this,  "", true);
    }

    private String prettyPrintTree(TreeNode node, String prefix, boolean isLeft) {
        String s = "";
        if (node == null) {
            s+="null";
        }

        if (node.right != null) {
            s += prettyPrintTree(node.right, prefix + (isLeft ? "│   " : "    "), false);
        }
        s += prefix + (isLeft ? "└── " : "┌── ") + node.val+'\n';
        if (node.left != null) {
            s += prettyPrintTree(node.left, prefix + (isLeft ? "    " : "│   "), true);
        }
        return s;
    }
}
