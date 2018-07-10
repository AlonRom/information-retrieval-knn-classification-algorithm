package Lucene;

public class SparseVector {
    private final int n;             // length
    private ST<Integer, Double> st;  // the vector, represented by index-value pairs

    // initialize the all 0s vector of length n
    public SparseVector(int n) {
        this.n  = n;
        this.st = new ST<Integer, Double>();
    }

    // put st[i] = value
    public void put(int i, double value) {
        if (i < 0 || i >= n) throw new RuntimeException("Illegal index");
        if (value == 0.0) st.remove(i);
        else              st.put(i, value);
    }

    // return st[i]
    public double get(int i) {
        if (i < 0 || i >= n) throw new RuntimeException("Illegal index");
        if (st.contains(i)) return st.get(i);
        else                return 0.0;
    }

    // return the number of nonzero entries
    public int nnz() {
        return st.size();
    }

    // return the size of the vector
    public int size() {
        return n;
    }

    // return the dot product of this vector with that vector
    public double dot(SparseVector that) {
        if (this.n != that.n) throw new IllegalArgumentException("Vector lengths disagree");
        double sum = 0.0;

        // iterate over the vector with the fewest nonzeros
        if (this.st.size() <= that.st.size()) {
            for (int i : this.st.keys())
                if (that.st.contains(i)) sum += this.get(i) * that.get(i);
        }
        else  {
            for (int i : that.st.keys())
                if (this.st.contains(i)) sum += this.get(i) * that.get(i);
        }
        return sum;
    }

    // return the 2-norm
    public double norm() {
        return Math.sqrt(this.dot(this));
    }

    // return alpha * this
    public SparseVector scale(double alpha) {
        SparseVector result = new SparseVector(n);
        for (int i : this.st.keys())
            result.put(i, alpha * this.get(i));
        return result;
    }

    // return this + that
    public SparseVector plus(SparseVector that) {
        if (this.n != that.n) throw new IllegalArgumentException("Vector lengths disagree");
        SparseVector result = new SparseVector(n);
        for (int i : this.st.keys()) result.put(i, this.get(i));
        for (int i : that.st.keys()) result.put(i, that.get(i) + result.get(i));
        return result;
    }

    // return a string representation
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i : st.keys()) {
            s.append("(" + i + ", " + st.get(i) + ") ");
        }
        return s.toString();
    }

}
