package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler;

public class ByteBufferCompilerException extends Exception {

    private static final long serialVersionUID = 1L;

    public ByteBufferCompilerException(String message) {
        super(message);
    }

    public ByteBufferCompilerException(String message, Exception cause) {
        super(message, cause);
    }

}
