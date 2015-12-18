package performance.load;

import rpc.io.Text;

public interface ServerProtocol {
    public Text echo(Text t);
}
