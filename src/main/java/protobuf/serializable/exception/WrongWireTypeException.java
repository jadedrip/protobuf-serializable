package protobuf.serializable.exception;

import java.io.IOException;

/**
 * 数据类型错误
 */
public class WrongWireTypeException extends IOException {
	private static final long serialVersionUID = -7461933979366278366L;

	public WrongWireTypeException() {
		super("Wrong wire type");
	}

	public WrongWireTypeException(String message) {
		super(message);
	}

	public WrongWireTypeException(Throwable cause) {
		super(cause);
	}

	public WrongWireTypeException(String message, Throwable cause) {
		super(message, cause);
	}
}