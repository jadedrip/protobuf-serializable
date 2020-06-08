package protobuf.serializable.exception;

/**
 * 缺少 Tag 注解
 */
public class NoTagException extends RuntimeException {
	private static final long serialVersionUID = -2469729687572457134L;

	public NoTagException() {
	}

	public NoTagException(String message) {
		super(message);
	}

	public NoTagException(Throwable cause) {
		super(cause);
	}

	public NoTagException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoTagException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}