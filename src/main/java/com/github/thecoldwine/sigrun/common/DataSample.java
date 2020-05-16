package com.github.thecoldwine.sigrun.common;

@SuppressWarnings("UnusedDeclaration")
public enum DataSample {
    IBM_FP((short) 1, "4 byte IBM floating point", 4),
    TC_4B_I((short) 2, "4 byte, two's complement integer", 4),
    TC_2B_I((short) 3, "2 byte, two's complement integer", 2),
    FP_G((short) 4, "4 byte fixed-point with gain", 4),
    IEEE_FP((short) 5, "4 byte fixed-point with gain", 4),
    TC_1B_I((short) 8, "1 byte, two's complement integer", 1),
	KSHA((short) 9, "ksha 1 byte, two's complement integer", 1);

    private final short code;
    private final String description;
    private final int size;

    DataSample(short code, String description, int size) {
        this.code = code;
        this.description = description;
        this.size = size;
    }

    /**
     * Returns a 2-byte code of a data sample format.
     *
     * @return code of a data sample format.
     */
    public short getCode() {
        return code;
    }

    /**
     * Returns a description of data sample format.
     *
     * @return description of a data sample format;
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a size of data sample in bytes
     *
     * @return size of a data sample
     */
    public int getSize() {
        return size;
    }

    /**
     * <p>
     * Creates a new instance of a DataSample class
     * from its code.
     * </p>
     * The dependency between code and other format parameters 
     * described in ``Binary File Header'' section of
     * a SegY Standard rev 1.
     * @param code - Code for sample type
     *
     * @return a new instance of DataSample class
     */
    public static DataSample create(short code) {
        switch (code) {
            case 1:
                return IBM_FP;
            case 2:
                return TC_4B_I;
            case 3:
                return TC_2B_I;
            case 4:
                return FP_G;
            case 5:
                return IEEE_FP;
            case 6: /* Fall through! */
            case 7:
                throw new IllegalArgumentException(
                		"This code is not currently supported by the SEG Y standard rev 1");
            case 8:
                return TC_1B_I;
            case 9:
            	return KSHA;
            default:
                throw new IllegalArgumentException(
                	"This code value is not supported by the SEG Y standard rev 1 : " 
                	+ code);
            	
        }
    }
}