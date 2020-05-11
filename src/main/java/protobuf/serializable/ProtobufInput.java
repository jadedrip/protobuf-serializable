package protobuf.serializable;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;

import protobuf.serializable.annotation.CollectionType;
import protobuf.serializable.annotation.MapType;
import protobuf.serializable.annotation.TagValue;
import protobuf.serializable.exception.NoTagException;
import protobuf.serializable.exception.WrongWireTypeException;

public class ProtobufInput implements AutoCloseable {
	InputStream inputStream;
	Long next = null;

	public ProtobufInput(final InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public byte[] readBytes(final long tag) throws IOException {
		if (!readTag(2, tag))
			return null;
		final long sz = getVarint();
		final byte[] bytes = new byte[(int) sz];
		inputStream.read(bytes);
		return bytes;
	}

	public Boolean readBoolean(final long tag) throws IOException {
		if (!readTag(0, tag))
			return null;
		final int c = inputStream.read();
		if (c != 0 && c != 1)
			throw new WrongWireTypeException("Not Boolean");
		return c == 1;
	}

	public Long readVarint(final long tag) throws IOException {
		if (!readTag(0, tag))
			return null;
		return zagzig(getVarint());
	}

	public Long readUnsigned(final long tag) throws IOException {
		if (!readTag(0, tag))
			return null;
		return getVarint();
	}

	public Float readFloat(final long tag) throws IOException {
		if (!readTag(5, tag))
			return null;
		byte[] bytes = new byte[4];
		inputStream.read(bytes);
		int asInt = (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) | ((bytes[2] & 0xFF) << 16)
				| ((bytes[3] & 0xFF) << 24);
		return Float.intBitsToFloat(asInt);
	}

	public Double readDouble(final long tag) throws IOException {
		if (!readTag(1, tag))
			return null;
		byte[] bytes = new byte[8];
		inputStream.read(bytes);
		long asInt = (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) | ((bytes[2] & 0xFF) << 16)
				| ((bytes[3] & 0xFF) << 24) | ((bytes[4] & 0xFF) << 32) | ((bytes[5] & 0xFF) << 40)
				| ((bytes[6] & 0xFF) << 48) | ((bytes[7] & 0xFF) << 56);
		return Double.longBitsToDouble(asInt);
	}

	public String readString(final long tag) throws IOException {
		if (!readTag(2, tag))
			return null;
		final long sz = getVarint();
		final byte[] buf = new byte[(int) sz];
		inputStream.read(buf);
		return new String(buf, utf8);
	}

	static Charset utf8 = Charset.forName("utf-8");

	public int available() throws IOException {
		return inputStream.available();
	}

	@Override
	public void close() throws IOException {
		inputStream.close();
	}

	public long nextTag() throws IOException {
		if (next == null) {
			next = getVarint();
		}
		return next >>> 3;
	}

	public enum WireType {
		Varint, // 0
		Bit64, LengthDelimited, StartGroup, // deprecated
		EndGroup, Bit32
	}

	public WireType nextWireType() throws IOException {
		if (next == null) {
			next = getVarint();
		}
		long v = next & 0x7;
		switch ((int) v) {
			case 0:
				return WireType.Varint;
			case 1:
				return WireType.Bit64;
			case 2:
				return WireType.LengthDelimited;
			case 5:
				return WireType.Bit32;
		}
		throw new WrongWireTypeException();
	}

	public Pair<Long, Object> readNext() throws IOException {
		long tag = nextTag();
		return Pair.of(tag, read(tag));
	}

	public Object read(final long tag) throws IOException {
		long nextTag = nextTag();
		if (nextTag != tag)
			return null;
		WireType wire = nextWireType();
		byte[] bytes;
		switch (wire) {
			case Varint:
				next = null;
				return getVarint();
			case Bit64:
				return readDouble(tag);
			case LengthDelimited:
				next = null;
				bytes = new byte[(int) getVarint()];
				inputStream.read(bytes);
				return bytes;
			case Bit32:
				return readFloat(tag);
			default:
				throw new WrongWireTypeException();
		}
	}

	public <T> T readClass(Class<T> clz) throws IOException, InstantiationException, IllegalAccessException {
		TagValue annotation = clz.getAnnotation(TagValue.class);
		if (annotation == null)
			throw new NoTagException();
		return readClass(annotation.value(), clz);
	}

	public <T> void readCollection(Collection<T> collection, long tag, Class<T> clz)
			throws IOException, InstantiationException, IllegalAccessException {
		while (nextTag() == tag) {
			T obj = readClass(tag, clz);
			collection.add(obj);
		}
	}

	public <K, V> Map<K, V> readMap(long tag, Class<K> keyClz, Class<V> valClz)
			throws IOException, InstantiationException, IllegalAccessException {
		Map<K, V> map = new TreeMap<>();
		while (nextTag() == tag) {
			if (nextWireType() != WireType.LengthDelimited)
				throw new WrongWireTypeException();
			next = null;
			long sz = getVarint();
			if (sz == 0)
				continue;
			K key = readClass(1, keyClz);
			V val = readClass(2, valClz);
			map.put(key, val);
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public <T> T readClass(long clzTag, Class<T> clz)
			throws IOException, InstantiationException, IllegalAccessException {
		if (clz == null || clz.isAssignableFrom(Object.class)) {
			return (T) read(clzTag);
		}
		if (byte[].class.isAssignableFrom(clz)) {
			if (!readTag(2, clzTag))
				return null;
			long sz = getVarint();
			byte[] bytes = new byte[(int) sz];
			inputStream.read(bytes);
			return (T) bytes;
		}
		if (clz.isArray()) {
			ArrayList<Object> array = new ArrayList<>();
			readCollection(array, clzTag, null);
			return (T) array.toArray();
		}
		if (String.class.isAssignableFrom(clz)) {
			return (T) readString(clzTag);
		}
		if (Double.class.isAssignableFrom(clz) || double.class.isAssignableFrom(clz))
			return (T) readDouble(clzTag);
		if (Float.class.isAssignableFrom(clz) || float.class.isAssignableFrom(clz))
			return (T) readFloat(clzTag);
		if (Long.class.isAssignableFrom(clz) || long.class.isAssignableFrom(clz)) {
			Long zagzig = readVarint(clzTag);
			return (T) zagzig;
		}
		if (Integer.class.isAssignableFrom(clz) || int.class.isAssignableFrom(clz)) {
			Long zagzig = readVarint(clzTag);
			return (T) Integer.valueOf(zagzig.intValue());
		}
		if (Short.class.isAssignableFrom(clz) || short.class.isAssignableFrom(clz)) {
			Long zagzig = readVarint(clzTag);
			return (T) Short.valueOf(zagzig.shortValue());
		}
		if (Collection.class.isAssignableFrom(clz)) {
			Collection<?> array = (Collection<?>) clz.newInstance();
			readCollection(array, clzTag, null);
			return (T) array;
		}
		if (!readTag(2, clzTag))
			return null;
		long sz = getVarint();
		T newInstance = clz.newInstance();
		if (sz == 0)
			return newInstance;

		long tag = 0;
		for (Field field : clz.getDeclaredFields()) {
			field.setAccessible(false);
			TagValue annotation = field.getAnnotation(TagValue.class);
			if (annotation == null)
				tag++;
			else
				tag = annotation.value();
			if (tag != nextTag())
				continue;
			if (Map.class.isAssignableFrom(field.getType())) {
				MapType ann = field.getAnnotation(MapType.class);
				if (ann != null) {
					Map<?, ?> map = readMap(tag, ann.keyType(), ann.valueType());
					field.set(newInstance, map);
				} else {
					Map<?, ?> map = readMap(tag, null, null);
					field.set(newInstance, map);
				}
			} else if (Collection.class.isAssignableFrom(field.getType())) {
				Collection<?> co = new ArrayList<>();
				CollectionType ann = field.getAnnotation(CollectionType.class);
				if (ann != null) {
					readCollection(co, tag, (Class) ann.value());
				} else {
					readCollection(co, tag, null);
				}
				field.set(newInstance, co);
			} else {
				Object obj = readClass(tag, field.getType());
				if (obj != null)
					field.set(newInstance, obj);
			}
		}
		return newInstance;
	}

	private boolean readTag(final int wireType, final long tag) throws IOException {
		final long c = next == null ? getVarint() : next;
		next = null;
		if (tag != (c >>> 3)) { // 不是指定的 tag
			return false;
		}
		if ((c & 0x7) != wireType)
			throw new RuntimeException("Bad format");
		return true;
	}

	private long getVarint() throws IOException {
		int c = inputStream.read();
		long v = c;
		int off = 3;
		while (c >= 0x80) {
			c = inputStream.read();
			v |= (c << off);
			off += 3;
		}
		return v;
	}

	private static long zagzig(final long value) {
		return ((value & 1) == 1) ? -(value >>> 1) : (value >>> 1);
	}
}