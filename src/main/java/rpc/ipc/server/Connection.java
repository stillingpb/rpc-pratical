package rpc.ipc.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

import rpc.io.Writable;
import rpc.pool.ByteBuff;
import rpc.pool.ByteBuffPool;

/**
 * 负责维护一次远程调用的连接，并控制请求数据的读，结果数据的写
 *
 * @author pb
 */
class Connection {

    SocketChannel channel;
    /*********************************/
    // 接收调用请求相关参数
    private SelectionKey readSelectionKey;
    private ByteBuff lenBufferShell;
    private ByteBuff readBufferShell;
    private ByteBuffer lenBuffer;
    private ByteBuffer readBuffer;

    /*********************************/

    // 发送调用结果相关参数
    private SelectionKey writeSelectionKey;
    private ByteBuffer writeBuffer;

    /*********************************/

    public Connection(SocketChannel channel) {
        this.channel = channel;
    }

    /**
     * 将调用结果设置到connection对象中
     *
     * @param result
     */
    public void setResult(Writable result) throws IOException {
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(response);
        // 将结果序列化
        out.writeUTF(result.getClass().getName());
        result.write(out);
        byte[] res = response.toByteArray();
        writeBuffer = ByteBuffer.wrap(res);
    }

    /**
     * 从连接中读取数据，如果已经读完所有的数据，则将一个call对象压入到callQueue中.
     *
     * @param callQueue
     * @return 返回读到的字节数。如果返回-1，表示客户端已经关闭了连接
     * @throws IOException
     */
    public int readCall(BlockingQueue<Call> callQueue) throws IOException {
        int count = 0;
        if (lenBuffer == null) {
            lenBufferShell = ByteBuffPool.allocate(4);
            lenBuffer = lenBufferShell.getByteBuffer();
            lenBuffer.mark();
        }
        if (lenBuffer.remaining() > 0) {
            count = channel.read(lenBuffer);
            if (lenBuffer.remaining() > 0) {
                return count;
            }
        }

        if (readBuffer == null) {
            lenBuffer.reset();
            int dataLen = lenBuffer.getInt();
            readBufferShell = ByteBuffPool.allocate(dataLen);
            readBuffer = readBufferShell.getByteBuffer();
        }
        if (readBuffer.remaining() > 0) {
            count = channel.read(readBuffer);
        }

        if (readBuffer.remaining() == 0) {
            DataInput dis = new DataInputStream(new ByteArrayInputStream(
                    readBuffer.array(), readBufferShell.getOffset(), readBufferShell.getCapacity()));
            lenBufferShell.free();
            readBufferShell.free();
            lenBufferShell = null;
            readBufferShell = null;
            lenBuffer = null;
            readBuffer = null;
            Call call = new Call();
            call.setAttach(this);
            call.readFields(dis);
            try {
                callQueue.put(call);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return count;
    }

    /**
     * 将result写出到channel
     *
     * @return 是否写完结果，如果写完,返回true，如果未写完,返回false
     * @throws IOException
     */
    public boolean writeResult() throws IOException {
        if (writeBuffer.remaining() > 0)
            channel.write(writeBuffer);
        if (writeBuffer.remaining() <= 0)
            return true;
        else
            return false;
    }

    /**
     * 关闭连接，并将注册的read,write事件从readselectKey，writeselectKey的selector中移除
     */
    public void close() {
        if (readSelectionKey != null) {
            readSelectionKey.cancel();
            readSelectionKey = null;
        }
        if (writeSelectionKey != null) {
            writeSelectionKey.cancel();
            writeSelectionKey = null;
        }
        ServerContext.closeChannel(channel);
    }

    public void setWriteSelectionKey(SelectionKey key) {
        this.writeSelectionKey = key;
    }

    public void setReadSelectionKey(SelectionKey readSelectionKey) {
        this.readSelectionKey = readSelectionKey;
    }
}