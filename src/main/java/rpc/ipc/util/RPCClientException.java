package rpc.ipc.util;

public class RPCClientException extends Exception{

	public RPCClientException(String msg,Exception e) {
		super(msg,e);
	}

}
