package protobuf.serializable.exception;

import java.io.IOException;

/**
 * 数据不足的异常
 */
public class InsufficientDataException extends IOException {
	private static final long serialVersionUID = -563687620861773279L;

	public InsufficientDataException() {
		super("Insufficient data");
	}

	public InsufficientDataException(String message) {
		super(message);
	}

	public InsufficientDataException(Throwable cause) {
		super(cause);
	}

	public InsufficientDataException(String message, Throwable cause) {
		super(message, cause);
	}
}