package newlayer;

/**
 * Created by jakob on 02-09-16.
 */
class NodeInfo {
    private int start;
    private int end;
    private int column;
    private int line;

    NodeInfo(int start, int end, int column, int line) {
        this.start = start;
        this.end = end;
        this.column = column;
        this.line = line;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getColumn() {
        return column;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
            "start=" + start +
            ", end=" + end +
            ", column=" + column +
            ", line=" + line +
            '}';
    }
}
