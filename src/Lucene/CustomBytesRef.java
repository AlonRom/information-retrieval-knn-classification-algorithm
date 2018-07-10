package Lucene;

import org.apache.lucene.util.BytesRef;

public class CustomBytesRef {
    private BytesRef bytesRef;

    public CustomBytesRef(BytesRef bytesRef){
        this.bytesRef = bytesRef;
    }

    public BytesRef getBytesRef(){
        return bytesRef;
    }

    @Override
    public boolean equals(Object obj) {
        // If the object is compared with itself then return true
        if (obj == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(obj instanceof CustomBytesRef)) {
            return false;
        }

        CustomBytesRef c = (CustomBytesRef) obj;
        return bytesRef.bytesEquals(c.getBytesRef());
    }

    @Override
    public int hashCode() {
        return bytesRef.hashCode();
    }
}
