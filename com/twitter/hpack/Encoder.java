/*
 * Copyright 2014 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.twitter.hpack;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import com.twitter.hpack.HpackUtil.IndexType;

/**
 * Encoder Class
 */
public final class Encoder {

  private static final int BUCKET_SIZE = 17;
  private static final byte[] EMPTY = {};

  // for testing
  private final boolean useIndexing;
  private final boolean forceHuffmanOn;
  private final boolean forceHuffmanOff;

  // a linked hash map of header fields
  private final HeaderEntry[] headerFields = new HeaderEntry[BUCKET_SIZE];
  private final HeaderEntry head = new HeaderEntry(-1, EMPTY, EMPTY, Integer.MAX_VALUE, null);
  private int size;
  private int capacity;

  /**
   * Creates a new encoder.
   * @param maxHeaderTableSize int
   */
  public Encoder(int maxHeaderTableSize) {
    this(maxHeaderTableSize, true, false, false);
  }

  /**
   * Constructor for testing only.
   * @param forceHuffmanOff  boolean
   * @param maxHeaderTableSize int
   * @param useIndexing boolean
   * @param forceHuffmanOn boolean
   */
  Encoder(
      int maxHeaderTableSize,
      boolean useIndexing,
      boolean forceHuffmanOn,
      boolean forceHuffmanOff
  ) {
    if (maxHeaderTableSize < 0) {
      throw new IllegalArgumentException("Illegal Capacity: " + maxHeaderTableSize);
    }
    this.useIndexing = useIndexing;
    this.forceHuffmanOn = forceHuffmanOn;
    this.forceHuffmanOff = forceHuffmanOff;
    this.capacity = maxHeaderTableSize;
    head.before = head.after = head;
  }

  /**
   * Encode the header field into the header block.
   * @param name byte array
   * @param out Outputstream
   * @param sensitive boolean
   * @param value byte array
   * @throws IOException if IOEXception
   */
  public void encodeHeader(OutputStream out, byte[] name, byte[] value, boolean sensitive) throws IOException {

    // If the header value is sensitive then it must never be indexed
    if (sensitive) {
      int nameIndex = getNameIndex(name);
      encodeLiteral(out, name, value, IndexType.NEVER, nameIndex);
      return;
    }

    // If the peer will only use the static table
    if (capacity == 0) {
      int staticTableIndex = StaticTable.getIndex(name, value);
      if (staticTableIndex == -1) {
        int nameIndex = StaticTable.getIndex(name);
        encodeLiteral(out, name, value, IndexType.NONE, nameIndex);
      } else {
        encodeInteger(out, 0x80, 7, staticTableIndex);
      }
      return;
    }

    int headerSize = HeaderField.sizeOf(name, value);

    // If the headerSize is greater than the max table size then it must be encoded literally
    if (headerSize > capacity) {
      int nameIndex = getNameIndex(name);
      encodeLiteral(out, name, value, IndexType.NONE, nameIndex);
      return;
    }

    HeaderEntry headerField = getEntry(name, value);
    if (headerField != null) {
      int index = getIndex(headerField.index) + StaticTable.length;
      // Section 6.1. Indexed Header Field Representation
      encodeInteger(out, 0x80, 7, index);
    } else {
      int staticTableIndex = StaticTable.getIndex(name, value);
      if (staticTableIndex != -1) {
        // Section 6.1. Indexed Header Field Representation
        encodeInteger(out, 0x80, 7, staticTableIndex);
      } else {
        int nameIndex = getNameIndex(name);
        if (useIndexing) {
          ensureCapacity(headerSize);
        }
        IndexType indexType = useIndexing ? IndexType.INCREMENTAL : IndexType.NONE;
        encodeLiteral(out, name, value, indexType, nameIndex);
        if (useIndexing) {
          add(name, value);
        }
      }
    }
  }

  /**
   * Set the maximum table size.
   * @param out OutputStream
   * @param maxHeaderTableSize int
   * @throws IOException if IOException
   */
  public void setMaxHeaderTableSize(OutputStream out, int maxHeaderTableSize) throws IOException {
    if (maxHeaderTableSize < 0) {
      throw new IllegalArgumentException("Illegal Capacity: " + maxHeaderTableSize);
    }
    if (capacity == maxHeaderTableSize) {
      return;
    }
    capacity = maxHeaderTableSize;
    ensureCapacity(0);
    encodeInteger(out, 0x20, 5, maxHeaderTableSize);
  }

  /**
   * Return the maximum table size.
   * @return capacity
   */
  public int getMaxHeaderTableSize() {
    return capacity;
  }

  /**
   * Encode integer according to Section 5.1.
   * @param out int
   * @param i int
   * @param mask int
   * @param n int
   */
  private static void encodeInteger(OutputStream out, int mask, int n, int i) throws IOException {
    if (n < 0 || n > 8) {
      throw new IllegalArgumentException("N: " + n);
    }
    int nbits = 0xFF >>> (8 - n);
    if (i < nbits) {
      out.write(mask | i);
    } else {
      out.write(mask | nbits);
      int length = i - nbits;
      while (true) {
        if ((length & ~0x7F) == 0) {
          out.write(length);
          return;
        } else {
          out.write((length & 0x7F) | 0x80);
          length >>>= 7;
        }
      }
    }
  }

  /**
   * Encode string literal according to Section 5.2.
   * @param out OutputStream
   * @param string byte array
   */
  private void encodeStringLiteral(OutputStream out, byte[] string) throws IOException {
    int huffmanLength = Huffman.ENCODER.getEncodedLength(string);
    if ((huffmanLength < string.length && !forceHuffmanOff) || forceHuffmanOn) {
      encodeInteger(out, 0x80, 7, huffmanLength);
      Huffman.ENCODER.encode(out, string);
    } else {
      encodeInteger(out, 0x00, 7, string.length);
      out.write(string, 0, string.length);
    }
  }

  /**
   * Encode literal header field according to Section 6.2.
   * @param out OutputStream
   * @param name byte array
   * @param indexType IndexType
   * @param nameIndex int
   * @param value byte array
   */
  private void encodeLiteral(OutputStream out, byte[] name, byte[] value, IndexType indexType, int nameIndex)
      throws IOException {
    int mask;
    int prefixBits;
    switch(indexType) {
    case INCREMENTAL:
      mask = 0x40;
      prefixBits = 6;
      break;
    case NONE:
      mask = 0x00;
      prefixBits = 4;
      break;
    case NEVER:
      mask = 0x10;
      prefixBits = 4;
      break;
    default:
      throw new IllegalStateException("should not reach here");
    }
    encodeInteger(out, mask, prefixBits, nameIndex == -1 ? 0 : nameIndex);
    if (nameIndex == -1) {
      encodeStringLiteral(out, name);
    }
    encodeStringLiteral(out, value);
  }

  /**
   * Returns Name
   * @param name byte array
   * @return int
   */
  private int getNameIndex(byte[] name) {
    int index = StaticTable.getIndex(name);
    if (index == -1) {
      index = getIndex(name);
      if (index >= 0) {
        index += StaticTable.length;
      }
    }
    return index;
  }

  /**
   * Ensure that the dynamic table has enough room to hold 'headerSize' more bytes.
   * Removes the oldest entry from the dynamic table until sufficient space is available.
   * @param headerSize  int
   * @throws IOException if IOException
   */
  private void ensureCapacity(int headerSize) throws IOException {
    while (size + headerSize > capacity) {
      int index = length();
      if (index == 0) {
        break;
      }
      remove();
    }
  }

  /**
   * Return the number of header fields in the dynamic table.
   * Exposed for testing.
   * @return int
   */
  int length() {
    return size == 0 ? 0 : head.after.index - head.before.index + 1;
  }

  /**
   * Return the size of the dynamic table.
   * Exposed for testing.
   * @return int
   */
  int size() {
    return size;
  }

  /**
   * Return the header field at the given index.
   * Exposed for testing.
   * @param index  int
   * @return   HeaderField
   */
  HeaderField getHeaderField(int index) {
    HeaderEntry entry = head;
    while(index-- >= 0) {
      entry = entry.before;
    }
    return entry;
  }

  /**
   * Returns the header entry with the lowest index value for the header field.
   * Returns null if header field is not in the dynamic table.
   * @param value byte array
   * @param name byte array
   * @return HeaderEntry
   */
  private HeaderEntry getEntry(byte[] name, byte[] value) {
    if (length() == 0 || name == null || value == null) {
      return null;
    }
    int h = hash(name);
    int i = index(h);
    for (HeaderEntry e = headerFields[i]; e != null; e = e.next) {
      if (e.hash == h &&
          HpackUtil.equals(name, e.name) &&
          HpackUtil.equals(value, e.value)) {
        return e;
      }
    }
    return null;
  }

  /**
   * Returns the lowest index value for the header field name in the dynamic table.
   * Returns -1 if the header field name is not in the dynamic table.
   * @param name byte array
   * @return int
   */
  private int getIndex(byte[] name) {
    if (length() == 0 || name == null) {
      return -1;
    }
    int h = hash(name);
    int i = index(h);
    int index = -1;
    for (HeaderEntry e = headerFields[i]; e != null; e = e.next) {
      if (e.hash == h && HpackUtil.equals(name, e.name)) {
        index = e.index;
        break;
      }
    }
    return getIndex(index);
  }

  /**
   * Compute the index into the dynamic table given the index in the header entry.
   * @param index int
   * @return int
   */
  private int getIndex(int index) {
    if (index == -1) {
      return index;
    }
    return index - head.before.index + 1;
  }

  /**
   * Add the header field to the dynamic table.
   * Entries are evicted from the dynamic table until the size of the table
   * and the new header field is less than the table's capacity.
   * If the size of the new entry is larger than the table's capacity,
   * the dynamic table will be cleared.
   * @param name byte array
   * @param value byte array
   */
  private void add(byte[] name, byte[] value) {
    int headerSize = HeaderField.sizeOf(name, value);

    // Clear the table if the header field size is larger than the capacity.
    if (headerSize > capacity) {
      clear();
      return;
    }

    // Evict oldest entries until we have enough capacity.
    while (size + headerSize > capacity) {
      remove();
    }

    // Copy name and value that modifications of original do not affect the dynamic table.
    name = Arrays.copyOf(name, name.length);
    value = Arrays.copyOf(value, value.length);

    int h = hash(name);
    int i = index(h);
    HeaderEntry old = headerFields[i];
    HeaderEntry e = new HeaderEntry(h, name, value, head.before.index - 1, old);
    headerFields[i] = e;
    e.addBefore(head);
    size += headerSize;
  }

  /**
   * Remove and return the oldest header field from the dynamic table.
   * @return HeaderField
   */
  private HeaderField remove() {
    if (size == 0) {
      return null;
    }
    HeaderEntry eldest = head.after;
    int h = eldest.hash;
    int i = index(h);
    HeaderEntry prev = headerFields[i];
    HeaderEntry e = prev;
    while (e != null) {
      HeaderEntry next = e.next;
      if (e == eldest) {
        if (prev == eldest) {
          headerFields[i] = next;
        } else {
          prev.next = next;
        }
        eldest.remove();
        size -= eldest.size();
        return eldest;
      }
      prev = e;
      e = next;
    }
    return null;
  }

  /**
   * Remove all entries from the dynamic table.
   */
  private void clear() {
    Arrays.fill(headerFields, null);
    head.before = head.after = head;
    this.size = 0;
  }

  /**
   * Returns the hash code for the given header field name.
   * @param name byte array
   * @return int
   */
  private static int hash(byte[] name) {
    int h = 0;
    for (int i = 0; i < name.length; i++) {
      h = 31 * h + name[i];
    }
    if (h > 0) {
      return h;
    } else if (h == Integer.MIN_VALUE) {
      return Integer.MAX_VALUE;
    } else {
      return -h;
    }
  }

  /**
   * Returns the index into the hash table for the hash code h.
   * @param h int
   * @return int
   */
  private static int index(int h) {
    return h % BUCKET_SIZE;
  }

  /**
   * A linked hash map HeaderField entry.
   */
  private static class HeaderEntry extends HeaderField {
    // These fields comprise the doubly linked list used for iteration.
    HeaderEntry before, after;

    // These fields comprise the chained list for header fields with the same hash.
    HeaderEntry next;
    int hash;

    // This is used to compute the index in the dynamic table.
    int index;

    /**
     * Creates new entry.
     * @param name byte array
     * @param value byte array
     * @param index int
     * @param hash int
     * @param next HeaderEntry
     */
    HeaderEntry(int hash, byte[] name, byte[] value, int index, HeaderEntry next) {
      super(name, value);
      this.index = index;
      this.hash = hash;
      this.next = next;
    }

    /**
     * Removes this entry from the linked list.
     */
    private void remove() {
      before.after = after;
      after.before = before;
      before = null; // null reference to prevent nepotism with generational GC.
      after = null;  // null reference to prevent nepotism with generational GC.
      next = null;   // null reference to prevent nepotism with generational GC.
    }

    /**
     * Inserts this entry before the specified existing entry in the list.
     * @param existingEntry HeaderEntry
     */
    private void addBefore(HeaderEntry existingEntry) {
      after  = existingEntry;
      before = existingEntry.before;
      before.after = this;
      after.before = this;
    }
  }
}
