package dev.anhcraft.vouchers.api.data;

public interface Modifiable {
    /**
     * Checks whether the data is dirty.
     * @return true if the data is dirty
     */
    boolean isDirty();
}
