package rpc.ipc.util;

public class RPCServerException extends Exception {
	public RPCServerException(Exception e){
		super(e);
	}
	
	public RPCServerException(String msg, Exception e) {
		super(msg, e);
	}
}
