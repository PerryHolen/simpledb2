package simpledb;

import java.util.*;

/**
 * The Join operator implements the relational join operation.
 */
public class Join extends Operator {

    private static final long serialVersionUID = 1L;
    private JoinPredicate pred;
    private DbIterator child1;
    private DbIterator child2;
    private DbIterator[] children;
    private Tuple currLeft;

    /**
     * Constructor. Accepts to children to join and the predicate to join them
     * on
     *
     * @param p
     *            The predicate to use to join the children
     * @param child1_
     *            Iterator for the left(outer) relation to join
     * @param child2_
     *            Iterator for the right(inner) relation to join
     */
    public Join(JoinPredicate p, DbIterator child1_, DbIterator child2_) {
        pred = p;
        child1 = child1_;
        child2 = child2_;
        children = new DbIterator[2];
        children[0] = child1;
        children[1] = child2;
        currLeft = null;
    }

    public JoinPredicate getJoinPredicate() {
        return pred;
    }

    /**
     * @return
     *       the field name of join field1. Should be quantified by
     *       alias or table name.
     * */
    public String getJoinField1Name() {
        return child1.getTupleDesc().getFieldName(pred.getField1());
    }

    /**
     * @return
     *       the field name of join field2. Should be quantified by
     *       alias or table name.
     * */
    public String getJoinField2Name() {
        return child2.getTupleDesc().getFieldName(pred.getField2());
    }

    /**
     * @see simpledb.TupleDesc#merge(TupleDesc, TupleDesc) for possible
     *      implementation logic.
     */
    public TupleDesc getTupleDesc() {
        return TupleDesc.merge(child1.getTupleDesc(),child2.getTupleDesc());
    }

    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        child1.open();
        child2.open();
        super.open();
    }

    public void close() {
        child1.close();
        child2.close();
        super.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        child1.rewind();
        child2.rewind();
        currLeft = null;
    }

    /**
     * Returns the next tuple generated by the join, or null if there are no
     * more tuples. Logically, this is the next tuple in r1 cross r2 that
     * satisfies the join predicate. There are many possible implementations;
     * the simplest is a nested loops join.
     * <p>
     * Note that the tuples returned from this particular implementation of Join
     * are simply the concatenation of joining tuples from the left and right
     * relation. Therefore, if an equality predicate is used there will be two
     * copies of the join attribute in the results. (Removing such duplicate
     * columns can be done with an additional projection operator if needed.)
     * <p>
     * For example, if one tuple is {1,2,3} and the other tuple is {1,5,6},
     * joined on equality of the first column, then this returns {1,2,3,1,5,6}.
     *
     * @return The next matching tuple.
     * @see JoinPredicate#filter
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        Tuple left,right;
        while(currLeft!= null || child1.hasNext()){
            if (currLeft!=null){
                left = currLeft;
            }
            else{
                currLeft = child1.next();
                left = currLeft;
            }
            while(child2.hasNext()){
                right = child2.next();
                if (pred.filter(left,right)){
                    Tuple output = new Tuple(getTupleDesc());
                    int j = 0;
                    for (int i = 0; i < left.getTupleDesc().numFields(); i++) {
                        output.setField(j++,left.getField(i));
                    }
                    for (int i = 0; i < right.getTupleDesc().numFields(); i++) {
                        output.setField(j++,right.getField(i));
                    }
                    return output;
                }
            }
            currLeft = null;
            child2.rewind();
        }
        return null;
    }

    @Override
    public DbIterator[] getChildren() {
        return children;
    }

    @Override
    public void setChildren(DbIterator[] children_) {
        children = children_;
    }

}
