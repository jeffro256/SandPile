import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

/*
	This class is rather computational inefficient as it is optimized to use
	a very small amount of memory.
*/

public class BigTiffWriter {
	/*
		Classes that implement this interface should yield values
		"per row", i.e. with coordinates { (0, 0), (1, 0), (2, 0) ...
			(1, 0), (1, 1), (1, 2) ...
	*/
	public static interface PixelIterator extends Iterator<Color> {
		int getWidth();
		int getHeight();
		
		default boolean reset() {
			return false;
		}
	}

	public static enum TiffDataType {
		// TIFF 6.0
		BYTE		(1,  1),
		ASCII		(2,  1),
		SHORT		(3,  2),
		LONG		(4,  4),
		RATIONAL	(5,  8),
		SBYTE		(6,  1),
		UNDEFINED	(7,  1),
		SSHORT      (8,  2),
		SLONG       (9,  4),
		SRATIONAL   (10, 8),
		FLOAT       (11, 4),
		DOUBLE      (12, 8),

		// BigTIFF
		LONG8       (16, 8),
		SLONG8      (17, 8),
		IFD8        (18, 8);

		private int code;
		private int byteSize;

		private TiffDataType(int typeCode, int size) {
			this.code = typeCode;
			this.byteSize = size;
		}

		public int typeCode() {
			return this.code;
		}

		public int size() {
			return this.byteSize;
		}
	}

	private PixelIterator src;
	private ByteOrder order;

	public BigTiffWriter(PixelIterator src, ByteOrder byteOrder) {
		this.src = src;
		this.order = byteOrder;
	}

	public BigTiffWriter(PixelIterator src) {
		this(src, ByteOrder.BIG_ENDIAN);
	}

	public void write(OutputStream out) throws IOException {
		if (!src.hasNext()) {
			if (!src.reset()) {
				throw new IllegalStateException("source unavailable");
			}
		}

		this.writeHeader(out);
		this.writeBody(out);
		this.writeIdf(out);

		out.close();
	}

	public void write(String fileName) throws IOException, FileNotFoundException {
		this.write(new FileOutputStream(fileName));
	}

	private void writeHeader(OutputStream out) throws IOException {
		final short tiffVersion = 43;
		final short offsetSize = 8;
		final long numPixels = src.getWidth() * src.getHeight();
		final long idfOffset = numPixels * 3L + 16L;

		ByteBuffer buf = ByteBuffer.allocate(16);
		buf.order(this.order);

		final byte M = 77;
		final byte I = 73;

		if (this.order == ByteOrder.BIG_ENDIAN) {
			buf.put(M).put(M);
		}
		else {
			buf.put(I).put(I);
		}

		buf.putShort(tiffVersion);
		buf.putShort(offsetSize);
		buf.putShort((short) 0);
		buf.putLong(idfOffset);

		out.write(buf.array(), 0, 16); // In case buf.array().length != 16
	}

	private void writeBody(OutputStream out) throws IOException {
		final long numPixels = src.getWidth() * src.getHeight();
		
		// bufferSize must be divisble by 3
		int bufferSize = (int) Math.min(numPixels * 3, 120000L);
		ByteBuffer buf = ByteBuffer.allocate(bufferSize);
		buf.order(this.order);

		while (this.src.hasNext()) {
			if (buf.position() == buf.limit()) {
				out.write(buf.array(), 0, buf.position());
				buf.position(0);
			}

			Color c = this.src.next();

			this.putColor(c, buf);
		}

		if (buf.position() != 0) {
			out.write(buf.array(), 0, buf.position());
		}
	}

	// cant handle large IDFs (> 4GB)
	private void writeIdf(OutputStream out) throws IOException {
		final long numTags = 8;
		final long stripBytes = this.src.getWidth() * this.src.getHeight() * 3;

		// bad for scalability
		ByteBuffer buf = ByteBuffer.allocate(16 + (int) numTags * 20);
		buf.order(this.order);

		buf.putLong(numTags);
		this.putTag(buf, 0x0100, TiffDataType.LONG, this.src.getWidth());  // width
		this.putTag(buf, 0x0101, TiffDataType.LONG, this.src.getHeight()); // height
		this.putTag(buf, 0x0102, TiffDataType.SHORT, new long[]{8, 8, 8});           // bits per sample
		this.putTag(buf, 0x0106, TiffDataType.SHORT, 2L);                  // photometric interp.
		this.putTag(buf, 0x0111, TiffDataType.SHORT, 16L);                 // strip offset
		this.putTag(buf, 0x0115, TiffDataType.SHORT, 3L);                  // samples per pixel
		this.putTag(buf, 0x0116, TiffDataType.LONG, this.src.getHeight()); // rows per strip
		this.putTag(buf, 0x0117, TiffDataType.LONG, stripBytes);           // bytes in strip
		buf.putLong(0);

		out.write(buf.array(), 0, buf.position());
	}

	// doesnt support floating point or string values
	private void putTag(ByteBuffer buffer, int code, TiffDataType dataType, long[] vals) {
		final int payloadLength = dataType.size() * vals.length;
		if (payloadLength > 8) {
			throw new IllegalArgumentException("putTag() does not support indirect data yet.");
		}
		else if (dataType != TiffDataType.SHORT && dataType != TiffDataType.LONG) {
			throw new IllegalArgumentException("putTag() only supports types SHORT & LONG.");
		}

		buffer.putShort((short) code);
		buffer.putShort((short) dataType.typeCode());
		buffer.putLong(vals.length);

		for (long val: vals) {
			switch (dataType) {
				case SHORT:
					buffer.putShort((short) val);
					break;
				case LONG:
					buffer.putInt((int) val);
					break;
				default:
					System.err.println("what");
			}
		}

		final int padding = 8 - payloadLength;

		for (int i = 0; i < padding; i++) {
			buffer.put((byte) 0);
		}
	}

	private void putTag(ByteBuffer buffer, int code, TiffDataType dataType, long val) {
		this.putTag(buffer, code, dataType, new long[]{val});
	}

	private void putColor(Color color, ByteBuffer buffer) {
		byte r = (byte) color.getRed();
		byte g = (byte) color.getGreen();
		byte b = (byte) color.getBlue();

		buffer.put(r).put(g).put(b);
	}
}