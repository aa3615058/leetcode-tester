package org.lengyu.algorithm.common;

public class ListNode {
    public int val;
    public ListNode next;

    public ListNode() {}
    public ListNode(int x) { val = x; }

    public ListNode(int x, ListNode next) {this.val = x; this.next = next;};

    @Override
    public String toString() {
        ListNode node=this;
        StringBuilder s0 = new StringBuilder();
        s0.append('[');
        while(node != null) {
            s0.append(Integer.toString(node.val));
            node=node.next;
            if(node!=null) s0.append(',');
        }
        s0.append(']');
        return s0.toString();
    }

   public String toPrettyString() {
        ListNode node=this;
        StringBuilder s0 = new StringBuilder();
        while(node != null) {
            s0.append(Integer.toString(node.val));
            node=node.next;
            if(node!=null) s0.append("->");
        }
        return s0.toString();
    }
}
