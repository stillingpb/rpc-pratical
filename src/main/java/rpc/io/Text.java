package rpc.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Text implements Writable {

    private String text;

    public Text() {
    }

    public Text(String text) {
        this.text = text;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(text);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        text = in.readUTF();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }
}
