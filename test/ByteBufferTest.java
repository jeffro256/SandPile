import java.nio.ByteBuffer;

public class ByteBufferTest {
	public static void main(String[] args) {
		ByteBuffer bb = ByteBuffer.allocate(10);

		bb.putInt(0x69420);

		printByteArray(bb.array());
		System.out.println(bb.array());
		System.out.println(bb.capacity());
		System.out.println(bb.hasArray());
		System.out.println(bb.isDirect());
		System.out.println(bb.isReadOnly());
		System.out.println(bb.limit());
		System.out.println(bb.order());
		System.out.println(bb.position());
		System.out.println(bb);
	}

	public static void printByteArray(byte[] array) {
		String hexChars = "0123456789ABCDEF";

		for (byte b: array) {
			System.out.print(hexChars.charAt((b & 0xF0) >>> 4));
			System.out.print(hexChars.charAt(b & 0x0F));
			System.out.print(' ');
		}

		System.out.println();
	}
}