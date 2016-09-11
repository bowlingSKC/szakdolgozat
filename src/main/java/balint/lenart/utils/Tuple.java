package balint.lenart.utils;

public class Tuple<X, Y> {

    private X first;
    private Y second;

    public Tuple() {
        this(null, null);
    }

    public Tuple(X first, Y second) {
        this.first = first;
        this.second = second;
    }

    public X getFirst() {
        return first;
    }

    public void setFirst(X first) {
        this.first = first;
    }

    public Y getSecond() {
        return second;
    }

    public void setSecond(Y second) {
        this.second = second;
    }
}
